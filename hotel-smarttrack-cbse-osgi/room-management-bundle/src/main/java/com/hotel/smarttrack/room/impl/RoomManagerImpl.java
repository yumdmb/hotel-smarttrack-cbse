package com.hotel.smarttrack.room.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.sql.DataSource;

import com.hotel.smarttrack.entity.Room;
import com.hotel.smarttrack.entity.RoomType;
import com.hotel.smarttrack.service.RoomService;

/**
 * RoomManagerImpl - OSGi Declarative Services implementation of RoomService.
 * Business logic for Room Management (UC5-UC8).
 * 
 * Uses JDBC-based repositories with DataSource from Karaf's pax-jdbc.
 *
 * @author Eisraq Rejab (refactored for JDBC)
 */
@Component(service = RoomService.class, immediate = true)
public class RoomManagerImpl implements RoomService {

    // ============ Room Status Constants (UPPERCASE) ============

    public static final String STATUS_AVAILABLE = "AVAILABLE";
    public static final String STATUS_OCCUPIED = "OCCUPIED";
    public static final String STATUS_UNDER_CLEANING = "UNDER_CLEANING";
    public static final String STATUS_OUT_OF_SERVICE = "OUT_OF_SERVICE";

    private static final List<String> VALID_STATUSES = Arrays.asList(
            STATUS_AVAILABLE, STATUS_OCCUPIED, STATUS_UNDER_CLEANING, STATUS_OUT_OF_SERVICE);

    // ============ DataSource and Repositories ============

    @Reference(target = "(osgi.jndi.service.name=jdbc/hoteldb)")
    private DataSource dataSource;

    private RoomRepository roomRepository;
    private RoomTypeRepository roomTypeRepository;

    // Mock reservation data for availability checking
    private final Map<Long, List<LocalDate[]>> roomReservations = new HashMap<>();

    // ============ OSGi Lifecycle Methods ============

    @Activate
    public void activate() {
        System.out.println("==============================================");
        System.out.println("[RoomManagerImpl] Bundle ACTIVATED ✅");
        System.out.println("  - Service Registered: RoomService");
        System.out.println("  - Using H2 Database via JDBC DataSource");
        System.out.println("==============================================");

        // Initialize repositories with DataSource
        this.roomTypeRepository = new RoomTypeRepository(dataSource);
        this.roomRepository = new RoomRepository(dataSource, roomTypeRepository);

        // Log counts from database
        System.out.println("[RoomManagerImpl] Found " + roomTypeRepository.count() + " room types in database");
        System.out.println("[RoomManagerImpl] Found " + roomRepository.count() + " rooms in database");
    }

    @Deactivate
    public void deactivate() {
        System.out.println("[RoomManagerImpl] Bundle DEACTIVATED");
    }

    // ============ Room Type Operations ============

    @Override
    public RoomType createRoomType(String typeName, String description, int maxOccupancy, BigDecimal basePrice) {
        if (typeName == null || typeName.trim().isEmpty()) {
            throw new IllegalArgumentException("Room type name cannot be null or empty");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Room type description cannot be null or empty");
        }
        if (maxOccupancy <= 0) {
            throw new IllegalArgumentException("Max occupancy must be greater than 0");
        }
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Base price must be greater than 0");
        }

        if (roomTypeRepository.existsByTypeNameIgnoreCase(typeName.trim())) {
            throw new IllegalArgumentException("Room type with name '" + typeName + "' already exists");
        }

        RoomType roomType = new RoomType();
        roomType.setTypeName(typeName.trim());
        roomType.setDescription(description.trim());
        roomType.setMaxOccupancy(maxOccupancy);
        roomType.setBasePrice(basePrice);
        roomType.setTaxRate(BigDecimal.valueOf(0.10));

        RoomType saved = roomTypeRepository.save(roomType);
        System.out.println("[RoomManagerImpl] Created room type: " + typeName);
        return saved;
    }

    @Override
    public RoomType updateRoomType(RoomType roomType) {
        if (roomType == null) {
            throw new IllegalArgumentException("Room type cannot be null");
        }
        if (roomType.getRoomTypeId() == null) {
            throw new IllegalArgumentException("Room type ID cannot be null");
        }

        roomTypeRepository.findById(roomType.getRoomTypeId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Room type with ID " + roomType.getRoomTypeId() + " not found"));

        RoomType saved = roomTypeRepository.save(roomType);
        System.out.println("[RoomManagerImpl] Updated room type: " + roomType.getTypeName());
        return saved;
    }

    @Override
    public RoomType updateRoomPricing(Long roomTypeId, BigDecimal newPrice, BigDecimal newTaxRate) {
        if (roomTypeId == null) {
            throw new IllegalArgumentException("Room type ID cannot be null");
        }
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("New price must be greater than 0");
        }
        if (newTaxRate != null && (newTaxRate.compareTo(BigDecimal.ZERO) < 0 ||
                newTaxRate.compareTo(BigDecimal.ONE) >= 1)) {
            throw new IllegalArgumentException("Tax rate must be between 0 and 1 (exclusive)");
        }

        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Room type with ID " + roomTypeId + " not found"));

        roomType.setBasePrice(newPrice);
        if (newTaxRate != null) {
            roomType.setTaxRate(newTaxRate);
        }

        RoomType saved = roomTypeRepository.save(roomType);
        System.out.println("[RoomManagerImpl] Updated pricing for: " + saved.getTypeName() +
                " - New price: $" + newPrice + ", Tax: " +
                saved.getTaxRate().multiply(new BigDecimal("100")) + "%");
        return saved;
    }

    @Override
    public List<RoomType> getAllRoomTypes() {
        return roomTypeRepository.findAll();
    }

    @Override
    public Optional<RoomType> getRoomTypeById(Long roomTypeId) {
        return roomTypeRepository.findById(roomTypeId);
    }

    @Override
    public void deleteRoomType(Long roomTypeId) {
        if (roomTypeId == null) {
            throw new IllegalArgumentException("Room type ID cannot be null");
        }

        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Room type with ID " + roomTypeId + " not found"));

        List<Room> roomsWithType = roomRepository.findByRoomTypeId(roomTypeId);
        if (!roomsWithType.isEmpty()) {
            throw new IllegalStateException("Cannot delete room type '" + roomType.getTypeName() +
                    "' - " + roomsWithType.size() + " room(s) exist with this type");
        }

        roomTypeRepository.delete(roomTypeId);
        System.out.println("[RoomManagerImpl] Deleted room type: " + roomType.getTypeName());
    }

    // ============ Room CRUD Operations ============

    @Override
    public Room createRoom(String roomNumber, int floorNumber, Long roomTypeId) {
        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Room number cannot be null or empty");
        }
        if (floorNumber < 1) {
            throw new IllegalArgumentException("Floor number must be at least 1");
        }
        if (roomTypeId == null) {
            throw new IllegalArgumentException("Room type ID cannot be null");
        }

        if (roomRepository.existsByRoomNumber(roomNumber.trim())) {
            throw new IllegalArgumentException("Room with number '" + roomNumber + "' already exists");
        }

        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Room type with ID " + roomTypeId + " not found"));

        Room room = new Room();
        room.setRoomNumber(roomNumber.trim());
        room.setFloorNumber(floorNumber);
        room.setRoomType(roomType);
        room.setStatus(STATUS_AVAILABLE);

        Room saved = roomRepository.save(room);
        System.out.println("[RoomManagerImpl] Created room: " + roomNumber +
                " (Type: " + roomType.getTypeName() + ", Floor: " + floorNumber + ")");
        return saved;
    }

    @Override
    public Room updateRoom(Room room) {
        if (room == null) {
            throw new IllegalArgumentException("Room cannot be null");
        }
        if (room.getRoomId() == null) {
            throw new IllegalArgumentException("Room ID cannot be null");
        }
        if (room.getRoomNumber() == null || room.getRoomNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Room number cannot be null or empty");
        }
        if (room.getFloorNumber() < 1) {
            throw new IllegalArgumentException("Floor number must be at least 1");
        }

        Room existing = roomRepository.findById(room.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Room with ID " + room.getRoomId() + " not found"));

        if (!existing.getRoomNumber().equals(room.getRoomNumber())) {
            if (roomRepository.existsByRoomNumber(room.getRoomNumber())) {
                throw new IllegalArgumentException("Room number '" + room.getRoomNumber() + "' is already in use");
            }
        }

        Room saved = roomRepository.save(room);
        System.out.println("[RoomManagerImpl] Updated room: " + room.getRoomNumber());
        return saved;
    }

    @Override
    public Optional<Room> getRoomById(Long roomId) {
        return roomRepository.findById(roomId);
    }

    @Override
    public Optional<Room> getRoomByNumber(String roomNumber) {
        return roomRepository.findByRoomNumber(roomNumber);
    }

    @Override
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @Override
    public List<Room> getRoomsByFloor(int floorNumber) {
        if (floorNumber < 1) {
            throw new IllegalArgumentException("Floor number must be at least 1");
        }
        return roomRepository.findByFloorNumber(floorNumber);
    }

    @Override
    public List<Room> getRoomsByType(Long roomTypeId) {
        if (roomTypeId == null) {
            throw new IllegalArgumentException("Room type ID cannot be null");
        }
        return roomRepository.findByRoomTypeId(roomTypeId);
    }

    @Override
    public void deleteRoom(Long roomId) {
        if (roomId == null) {
            throw new IllegalArgumentException("Room ID cannot be null");
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room with ID " + roomId + " not found"));

        if (STATUS_OCCUPIED.equals(room.getStatus())) {
            throw new IllegalStateException("Cannot delete room " + room.getRoomNumber() +
                    " - room is currently occupied");
        }

        roomRepository.delete(roomId);
        System.out.println("[RoomManagerImpl] Deleted room: " + room.getRoomNumber() +
                " (ID: " + roomId + ")");
    }

    // ============ Room Status Operations ============

    @Override
    public void updateRoomStatus(Long roomId, String status) {
        if (roomId == null) {
            throw new IllegalArgumentException("Room ID cannot be null");
        }
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Room status cannot be null or empty");
        }

        if (!VALID_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Invalid room status: " + status +
                    ". Valid statuses are: " + String.join(", ", VALID_STATUSES));
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room with ID " + roomId + " not found"));

        String oldStatus = room.getStatus();
        room.setStatus(status);
        roomRepository.save(room);

        System.out.println("[RoomManagerImpl] Updated room " + room.getRoomNumber() +
                " status from '" + oldStatus + "' to '" + status + "'");
    }

    @Override
    public List<Room> getRoomsByStatus(String status) {
        if (status == null || !VALID_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Invalid room status: " + status);
        }
        return roomRepository.findByStatus(status);
    }

    // ============ Room Availability Operations ============

    @Override
    public List<Room> getAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        validateDateRange(checkIn, checkOut);

        return roomRepository.findAllAvailable().stream()
                .filter(room -> isRoomAvailableInternal(room, checkIn, checkOut))
                .collect(Collectors.toList());
    }

    @Override
    public List<Room> getAvailableRoomsByType(Long roomTypeId, LocalDate checkIn, LocalDate checkOut) {
        if (roomTypeId == null) {
            throw new IllegalArgumentException("Room type ID cannot be null");
        }

        validateDateRange(checkIn, checkOut);

        return roomRepository.findByRoomTypeId(roomTypeId).stream()
                .filter(room -> isRoomAvailableInternal(room, checkIn, checkOut))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        if (roomId == null) {
            throw new IllegalArgumentException("Room ID cannot be null");
        }

        validateDateRange(checkIn, checkOut);

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room with ID " + roomId + " not found"));

        return isRoomAvailableInternal(room, checkIn, checkOut);
    }

    // ============ Helper Methods ============

    private boolean isRoomAvailableInternal(Room room, LocalDate checkIn, LocalDate checkOut) {
        if (!STATUS_AVAILABLE.equals(room.getStatus())) {
            return false;
        }

        List<LocalDate[]> reservations = roomReservations.getOrDefault(room.getRoomId(), new ArrayList<>());

        for (LocalDate[] reserved : reservations) {
            LocalDate reservedStart = reserved[0];
            LocalDate reservedEnd = reserved[1];

            if (!checkOut.isBefore(reservedStart) && !reservedEnd.isBefore(checkIn)) {
                return false;
            }
        }

        return true;
    }

    public void blockRoomDates(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        roomReservations.computeIfAbsent(roomId, k -> new ArrayList<>())
                .add(new LocalDate[] { checkIn, checkOut });
    }

    private void validateDateRange(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null) {
            throw new IllegalArgumentException("Check-in date cannot be null");
        }
        if (checkOut == null) {
            throw new IllegalArgumentException("Check-out date cannot be null");
        }
        if (checkIn.isAfter(checkOut)) {
            throw new IllegalArgumentException("Check-in date (" + checkIn +
                    ") must be before check-out date (" + checkOut + ")");
        }
        if (checkIn.equals(checkOut)) {
            throw new IllegalArgumentException("Check-in and check-out dates cannot be the same");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the past");
        }
    }
}
