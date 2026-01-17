package com.hotel.smarttrack.room;

import com.hotel.smarttrack.entity.Room;
import com.hotel.smarttrack.entity.RoomType;
import com.hotel.smarttrack.repository.RoomRepository;
import com.hotel.smarttrack.repository.RoomTypeRepository;
import com.hotel.smarttrack.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * RoomManager - Implementation of RoomService.
 * Business logic for Room Management (Rule 2 & 3).
 * This class is private to the room-management component.
 * 
 * Enhanced Features:
 * - Input validation at service layer
 * - Date range validation
 * - Status value validation using RoomStatus enum
 * - Enhanced availability checking with reservation integration
 * - JPA-based persistence
 */
@Service
@Transactional
public class RoomManager implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;

    // Helper component for availability checking with reservation integration
    private final RoomAvailabilityChecker availabilityChecker;

    @Autowired
    public RoomManager(RoomRepository roomRepository,
            RoomTypeRepository roomTypeRepository,
            RoomAvailabilityChecker availabilityChecker) {
        this.roomRepository = roomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.availabilityChecker = availabilityChecker;
    }

    /**
     * Initialize sample data for demonstration purposes.
     * Only runs if no room types exist in the database.
     */
    @PostConstruct
    public void initializeSampleData() {
        if (roomTypeRepository.count() == 0) {
            createRoomType("Standard", "Standard room with basic amenities", 2, new BigDecimal("100.00"));
            createRoomType("Deluxe", "Deluxe room with premium amenities", 3, new BigDecimal("200.00"));
            createRoomType("Suite", "Luxury suite with separate living area", 4, new BigDecimal("350.00"));

            System.out.println("[RoomManager] Initialized with " + roomTypeRepository.count() + " room types");
        }
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
    public RoomType updateRoomPricing(Long roomTypeId, BigDecimal newPrice, BigDecimal newTaxRate) {
        // Input validation
        if (roomTypeId == null) {
            throw new IllegalArgumentException("Room type ID cannot be null");
        }
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("New price must be greater than 0");
        }
        if (newTaxRate == null || newTaxRate.compareTo(BigDecimal.ZERO) < 0 ||
                newTaxRate.compareTo(BigDecimal.ONE) >= 1) {
            throw new IllegalArgumentException("Tax rate must be between 0 and 1 (exclusive)");
        }

        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Room type with ID " + roomTypeId + " not found"));

        roomType.setBasePrice(newPrice);
        roomType.setTaxRate(newTaxRate);

        RoomType saved = roomTypeRepository.save(roomType);
        System.out.println("[RoomManager] Updated pricing for: " + saved.getTypeName() +
                " - New price: $" + newPrice + ", Tax: " +
                newTaxRate.multiply(new BigDecimal("100")) + "%");
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomType> getAllRoomTypes() {
        return roomTypeRepository.findAll();
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
        room.setStatus(RoomStatus.AVAILABLE.getDisplayName());

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
    @Transactional(readOnly = true)
    public Optional<Room> getRoomById(Long roomId) {
        return roomRepository.findById(roomId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Room> getRoomByNumber(String roomNumber) {
        return roomRepository.findByRoomNumber(roomNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
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
        if (RoomStatus.OCCUPIED.getDisplayName().equals(room.getStatus())) {
            throw new IllegalStateException("Cannot delete room " + room.getRoomNumber() +
                    " - room is currently occupied");
        }

        roomRepository.delete(room);
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

        // Validate status using RoomStatus enum
        RoomStatus newStatus;
        try {
            newStatus = RoomStatus.fromDisplayName(status);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid room status. " + e.getMessage());
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room with ID " + roomId + " not found"));

        String oldStatus = room.getStatus();
        room.setStatus(newStatus.getDisplayName());
        roomRepository.save(room);

        System.out.println("[RoomManager] Updated room " + room.getRoomNumber() +
                " status from '" + oldStatus + "' to '" + newStatus.getDisplayName() + "'");
    }

    // ============ Room Availability Operations ============

    @Override
    @Transactional(readOnly = true)
    public List<Room> getAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        // Date range validation
        validateDateRange(checkIn, checkOut);

        // Filter rooms using enhanced availability checking
        return roomRepository.findAllAvailable().stream()
                .filter(room -> availabilityChecker.isRoomAvailable(room, checkIn, checkOut))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Room> getAvailableRoomsByType(Long roomTypeId, LocalDate checkIn, LocalDate checkOut) {
        // Input validation
        if (roomTypeId == null) {
            throw new IllegalArgumentException("Room type ID cannot be null");
        }

        // Date range validation
        validateDateRange(checkIn, checkOut);

        // Validate room type exists
        if (!roomTypeRepository.existsById(roomTypeId)) {
            throw new IllegalArgumentException("Room type with ID " + roomTypeId + " not found");
        }

        return roomRepository.findAvailableByRoomTypeId(roomTypeId).stream()
                .filter(room -> availabilityChecker.isRoomAvailable(room, checkIn, checkOut))
                .collect(Collectors.toList());
    }

    // ============ Helper Methods ============

    /**
     * Validate date range for room availability checks.
     * 
     * @param checkIn  Check-in date
     * @param checkOut Check-out date
     * @throws IllegalArgumentException if dates are invalid
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

    /**
     * Get the availability checker for external access if needed.
     * This allows other components (like Reservation) to block/unblock dates.
     * 
     * @return The room availability checker
     */
    public RoomAvailabilityChecker getAvailabilityChecker() {
        return availabilityChecker;
    }

    /**
     * Get rooms by status (additional helper method).
     * 
     * @param status The room status to filter by
     * @return List of rooms with the specified status
     */
    @Transactional(readOnly = true)
    public List<Room> getRoomsByStatus(String status) {
        // Validate status
        RoomStatus.fromDisplayName(status); // This will throw if invalid

        return roomRepository.findByStatus(status);
    }

    /**
     * Get rooms by floor number (additional helper method).
     * 
     * @param floorNumber The floor number
     * @return List of rooms on the specified floor
     */
    @Transactional(readOnly = true)
    public List<Room> getRoomsByFloor(int floorNumber) {
        if (floorNumber < 1) {
            throw new IllegalArgumentException("Floor number must be at least 1");
        }

        return roomRepository.findByFloorNumber(floorNumber);
    }

    /**
     * Get rooms by room type (additional helper method).
     * 
     * @param roomTypeId The room type ID
     * @return List of rooms of the specified type
     */
    @Transactional(readOnly = true)
    public List<Room> getRoomsByType(Long roomTypeId) {
        if (roomTypeId == null) {
            throw new IllegalArgumentException("Room type ID cannot be null");
        }

        return roomRepository.findByRoomTypeId(roomTypeId);
    }
}
