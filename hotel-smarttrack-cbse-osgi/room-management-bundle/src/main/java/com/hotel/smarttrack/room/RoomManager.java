package com.hotel.smarttrack.room;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import com.hotel.smarttrack.entity.Room;
import com.hotel.smarttrack.entity.RoomType;
import com.hotel.smarttrack.service.RoomService;

/**
 * RoomManager - OSGi Declarative Services implementation of RoomService.
 * Business logic for Room Management (UC5-UC8).
 * 
 * Key OSGi Annotations:
 * - @Component: Registers this as an OSGi DS component
 * - @Activate: Called when bundle starts
 * - @Deactivate: Called when bundle stops
 * 
 * Note: Room bundle has NO dependencies on other bundles!
 * It can activate independently.
 *
 */
@Component(
    service = RoomService.class,  // Register as RoomService interface
    immediate = true               // Activate immediately when bundle starts
)
public class RoomManager implements RoomService {

    // ============ Room Status Constants ============
    
    public static final String STATUS_AVAILABLE = "Available";
    public static final String STATUS_OCCUPIED = "Occupied";
    public static final String STATUS_UNDER_CLEANING = "Under Cleaning";
    public static final String STATUS_OUT_OF_SERVICE = "Out of Service";

    private static final List<String> VALID_STATUSES = Arrays.asList(
            STATUS_AVAILABLE, STATUS_OCCUPIED, STATUS_UNDER_CLEANING, STATUS_OUT_OF_SERVICE);

    // ============ In-Memory Repositories ============
    
    private final RoomRepository roomRepository = new RoomRepository();
    private final RoomTypeRepository roomTypeRepository = new RoomTypeRepository();

    // Mock reservation data for availability checking
    // In production, this would query the Reservation service
    // Map: roomId -> List of [checkIn, checkOut] date pairs
    private final Map<Long, List<LocalDate[]>> roomReservations = new HashMap<>();

    // ============ OSGi Lifecycle Methods ============

    /**
     * Called when the bundle is activated.
     * Loads seed data according to SEED_DATA_SPEC.md
     */
    @Activate
    public void activate() {
        System.out.println("[RoomManager] Bundle ACTIVATING...");
        loadSeedData();
        System.out.println("[RoomManager] Loaded " + roomTypeRepository.count() + " room types");
        System.out.println("[RoomManager] Loaded " + roomRepository.count() + " rooms");
        System.out.println("[RoomManager] Bundle ACTIVATED ✓");
    }

    /**
     * Called when the bundle is deactivated.
     */
    @Deactivate
    public void deactivate() {
        System.out.println("[RoomManager] Bundle DEACTIVATED");
    }

    /**
     * Load seed data according to SEED_DATA_SPEC.md
     * 
     * Room Types:
     * - ID 1: Standard (2 guests, $100)
     * - ID 2: Deluxe (2 guests, $150)
     * - ID 3: Suite (4 guests, $250)
     * 
     * Rooms:
     * - ID 1: Room 101 (Floor 1, Standard)
     * - ID 2: Room 102 (Floor 1, Standard)
     * - ID 3: Room 201 (Floor 2, Deluxe)
     * - ID 4: Room 202 (Floor 2, Deluxe)
     * - ID 5: Room 301 (Floor 3, Suite)
     */
    private void loadSeedData() {
        // Create room types (these IDs must match SEED_DATA_SPEC.md)
        RoomType standard = roomTypeRepository.save(new RoomType(null, "Standard",
            "Standard room with queen bed", 2, new BigDecimal("100.00"), new BigDecimal("0.10")));
        
        RoomType deluxe = roomTypeRepository.save(new RoomType(null, "Deluxe",
            "Deluxe room with king bed and city view", 2, new BigDecimal("150.00"), new BigDecimal("0.10")));
        
        RoomType suite = roomTypeRepository.save(new RoomType(null, "Suite",
            "Executive suite with separate living area", 4, new BigDecimal("250.00"), new BigDecimal("0.10")));

        // Create rooms (these IDs must match SEED_DATA_SPEC.md)
        roomRepository.save(new Room(null, "101", 1, standard, STATUS_AVAILABLE));
        roomRepository.save(new Room(null, "102", 1, standard, STATUS_AVAILABLE));
        roomRepository.save(new Room(null, "201", 2, deluxe, STATUS_AVAILABLE));
        roomRepository.save(new Room(null, "202", 2, deluxe, STATUS_AVAILABLE));
        roomRepository.save(new Room(null, "301", 3, suite, STATUS_AVAILABLE));
    }

    // ============ Room Type Operations ============

    @Override
    public RoomType createRoomType(String typeName, String description, int maxOccupancy, BigDecimal basePrice) {
        // Input validation
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

        // Check for duplicate type name
        if (roomTypeRepository.existsByTypeNameIgnoreCase(typeName.trim())) {
            throw new IllegalArgumentException("Room type with name '" + typeName + "' already exists");
        }

        RoomType roomType = new RoomType();
        roomType.setTypeName(typeName.trim());
        roomType.setDescription(description.trim());
        roomType.setMaxOccupancy(maxOccupancy);
        roomType.setBasePrice(basePrice);
        roomType.setTaxRate(BigDecimal.valueOf(0.10)); // Default 10% tax

        RoomType saved = roomTypeRepository.save(roomType);
        System.out.println("[RoomManager] Created room type: " + typeName);
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

        RoomType existing = roomTypeRepository.findById(roomType.getRoomTypeId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Room type with ID " + roomType.getRoomTypeId() + " not found"));

        RoomType saved = roomTypeRepository.save(roomType);
        System.out.println("[RoomManager] Updated room type: " + roomType.getTypeName());
        return saved;
    }

    @Override
    public RoomType updateRoomPricing(Long roomTypeId, BigDecimal newPrice, BigDecimal newTaxRate) {
        // Input validation
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
        System.out.println("[RoomManager] Updated pricing for: " + saved.getTypeName() +
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

        // Business rule: Can't delete if rooms exist with this type
        List<Room> roomsWithType = roomRepository.findByRoomTypeId(roomTypeId);
        if (!roomsWithType.isEmpty()) {
            throw new IllegalStateException("Cannot delete room type '" + roomType.getTypeName() +
                    "' - " + roomsWithType.size() + " room(s) exist with this type");
        }

        roomTypeRepository.delete(roomTypeId);
        System.out.println("[RoomManager] Deleted room type: " + roomType.getTypeName());
    }

    // ============ Room CRUD Operations ============

    @Override
    public Room createRoom(String roomNumber, int floorNumber, Long roomTypeId) {
        // Input validation
        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Room number cannot be null or empty");
        }
        if (floorNumber < 1) {
            throw new IllegalArgumentException("Floor number must be at least 1");
        }
        if (roomTypeId == null) {
            throw new IllegalArgumentException("Room type ID cannot be null");
        }

        // Check for duplicate room number
        if (roomRepository.existsByRoomNumber(roomNumber.trim())) {
            throw new IllegalArgumentException("Room with number '" + roomNumber + "' already exists");
        }

        // Validate room type exists
        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Room type with ID " + roomTypeId + " not found"));

        Room room = new Room();
        room.setRoomNumber(roomNumber.trim());
        room.setFloorNumber(floorNumber);
        room.setRoomType(roomType);
        room.setStatus(STATUS_AVAILABLE);

        Room saved = roomRepository.save(room);
        System.out.println("[RoomManager] Created room: " + roomNumber +
                " (Type: " + roomType.getTypeName() + ", Floor: " + floorNumber + ")");
        return saved;
    }

    @Override
    public Room updateRoom(Room room) {
        // Input validation
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

        // Check if room number is being changed and if new number already exists
        if (!existing.getRoomNumber().equals(room.getRoomNumber())) {
            if (roomRepository.existsByRoomNumber(room.getRoomNumber())) {
                throw new IllegalArgumentException("Room number '" + room.getRoomNumber() + "' is already in use");
            }
        }

        Room saved = roomRepository.save(room);
        System.out.println("[RoomManager] Updated room: " + room.getRoomNumber());
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
        // Input validation
        if (roomId == null) {
            throw new IllegalArgumentException("Room ID cannot be null");
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room with ID " + roomId + " not found"));

        // Business rule: Can't delete occupied rooms
        if (STATUS_OCCUPIED.equals(room.getStatus())) {
            throw new IllegalStateException("Cannot delete room " + room.getRoomNumber() +
                    " - room is currently occupied");
        }

        roomRepository.delete(roomId);
        System.out.println("[RoomManager] Deleted room: " + room.getRoomNumber() +
                " (ID: " + roomId + ")");
    }

    // ============ Room Status Operations ============

    @Override
    public void updateRoomStatus(Long roomId, String status) {
        // Input validation
        if (roomId == null) {
            throw new IllegalArgumentException("Room ID cannot be null");
        }
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Room status cannot be null or empty");
        }

        // Validate status
        if (!VALID_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Invalid room status: " + status +
                    ". Valid statuses are: " + String.join(", ", VALID_STATUSES));
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room with ID " + roomId + " not found"));

        String oldStatus = room.getStatus();
        room.setStatus(status);
        roomRepository.save(room);

        System.out.println("[RoomManager] Updated room " + room.getRoomNumber() +
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
        // Date range validation
        validateDateRange(checkIn, checkOut);

        // Filter rooms using enhanced availability checking
        return roomRepository.findAllAvailable().stream()
                .filter(room -> isRoomAvailableInternal(room, checkIn, checkOut))
                .collect(Collectors.toList());
    }

    @Override
    public List<Room> getAvailableRoomsByType(Long roomTypeId, LocalDate checkIn, LocalDate checkOut) {
        // Input validation
        if (roomTypeId == null) {
            throw new IllegalArgumentException("Room type ID cannot be null");
        }

        // Date range validation
        validateDateRange(checkIn, checkOut);

        // Filter by room type and availability
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

    /**
     * Check if a room is available for a specific date range.
     */
    private boolean isRoomAvailableInternal(Room room, LocalDate checkIn, LocalDate checkOut) {
        // First check the room status - only Available rooms can be booked
        if (!STATUS_AVAILABLE.equals(room.getStatus())) {
            return false;
        }

        // Check against existing reservations
        List<LocalDate[]> reservations = roomReservations.getOrDefault(room.getRoomId(), new ArrayList<>());

        for (LocalDate[] reserved : reservations) {
            LocalDate reservedStart = reserved[0];
            LocalDate reservedEnd = reserved[1];

            // Check if date ranges overlap
            if (!checkOut.isBefore(reservedStart) && !reservedEnd.isBefore(checkIn)) {
                return false; // Conflict found
            }
        }

        return true;
    }

    /**
     * Block a room for a specific date range (simulating a reservation).
     * In production, this would be handled by the Reservation service.
     */
    public void blockRoomDates(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        roomReservations.computeIfAbsent(roomId, k -> new ArrayList<>())
                .add(new LocalDate[] { checkIn, checkOut });
    }

    /**
     * Validates date range for room availability checks.
     */
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
