package com.hotel.smarttrack.service;

import com.hotel.smarttrack.entity.Room;
import com.hotel.smarttrack.entity.RoomType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * RoomService interface - exposes Room Management functionality.
 * Part of Base Library (CBSE Rule 5) - interface in common library.
 * 
 * <p>
 * Implemented by RoomManager in room-management-bundle.
 * </p>
 * 
 * <p>
 * Handles UC5-UC8:
 * </p>
 * <ul>
 * <li>UC5: Manage Room Records</li>
 * <li>UC6: Manage Room Operational Status</li>
 * <li>UC7: Manage Room Pricing</li>
 * <li>UC8: Display Room Availability</li>
 * </ul>
 * 
 * @author Eisraq Rejab
 */
public interface RoomService {

    // ============ Room Type Operations (UC5, UC7) ============

    /**
     * Create a new room type.
     * 
     * @param typeName     Type name (e.g., Standard, Deluxe, Suite)
     * @param description  Type description
     * @param maxOccupancy Maximum occupancy
     * @param basePrice    Base price per night
     * @return Created room type with generated ID
     */
    RoomType createRoomType(String typeName, String description, int maxOccupancy, BigDecimal basePrice);

    /**
     * Update room type information.
     * 
     * @param roomType Room type with updated information
     * @return Updated room type
     * @throws IllegalArgumentException if room type not found
     */
    RoomType updateRoomType(RoomType roomType);

    /**
     * Update room type pricing (UC7).
     * 
     * @param roomTypeId Room type ID
     * @param newPrice   New base price
     * @param newTaxRate New tax rate (can be null to keep unchanged)
     * @return Updated room type
     * @throws IllegalArgumentException if room type not found
     */
    RoomType updateRoomPricing(Long roomTypeId, BigDecimal newPrice, BigDecimal newTaxRate);

    /**
     * Get all room types.
     * 
     * @return List of all room types
     */
    List<RoomType> getAllRoomTypes();

    /**
     * Get room type by ID.
     * 
     * @param roomTypeId Room type ID
     * @return Room type if found
     */
    Optional<RoomType> getRoomTypeById(Long roomTypeId);

    /**
     * Delete a room type.
     * 
     * @param roomTypeId Room type ID
     * @throws IllegalArgumentException if room type not found
     * @throws IllegalStateException    if rooms exist with this type
     */
    void deleteRoomType(Long roomTypeId);

    // ============ Room CRUD Operations (UC5) ============

    /**
     * Create a new room.
     * 
     * @param roomNumber  Room number (e.g., "101", "202A")
     * @param floorNumber Floor number
     * @param roomTypeId  Room type ID
     * @return Created room with generated ID
     * @throws IllegalArgumentException if room type not found
     */
    Room createRoom(String roomNumber, int floorNumber, Long roomTypeId);

    /**
     * Update room information.
     * 
     * @param room Room with updated information
     * @return Updated room
     * @throws IllegalArgumentException if room not found
     */
    Room updateRoom(Room room);

    /**
     * Get room by ID.
     * 
     * @param roomId Room ID
     * @return Room if found
     */
    Optional<Room> getRoomById(Long roomId);

    /**
     * Get room by room number.
     * 
     * @param roomNumber Room number
     * @return Room if found
     */
    Optional<Room> getRoomByNumber(String roomNumber);

    /**
     * Get all rooms.
     * 
     * @return List of all rooms
     */
    List<Room> getAllRooms();

    /**
     * Get rooms by floor.
     * 
     * @param floorNumber Floor number
     * @return List of rooms on the floor
     */
    List<Room> getRoomsByFloor(int floorNumber);

    /**
     * Get rooms by type.
     * 
     * @param roomTypeId Room type ID
     * @return List of rooms of the specified type
     */
    List<Room> getRoomsByType(Long roomTypeId);

    /**
     * Delete a room.
     * 
     * @param roomId Room ID
     * @throws IllegalArgumentException if room not found
     * @throws IllegalStateException    if room is currently occupied
     */
    void deleteRoom(Long roomId);

    // ============ Room Status Operations (UC6) ============

    /**
     * Update room operational status.
     * 
     * @param roomId Room ID
     * @param status New status (Available, Occupied, Under Cleaning, Out of
     *               Service)
     * @throws IllegalArgumentException if room not found or invalid status
     */
    void updateRoomStatus(Long roomId, String status);

    /**
     * Get rooms by status.
     * 
     * @param status Room status
     * @return List of rooms with the specified status
     */
    List<Room> getRoomsByStatus(String status);

    // ============ Room Availability (UC8) ============

    /**
     * Get available rooms for date range.
     * 
     * @param checkIn  Check-in date
     * @param checkOut Check-out date
     * @return List of available rooms
     */
    List<Room> getAvailableRooms(LocalDate checkIn, LocalDate checkOut);

    /**
     * Get available rooms by type for date range.
     * 
     * @param roomTypeId Room type ID
     * @param checkIn    Check-in date
     * @param checkOut   Check-out date
     * @return List of available rooms of the specified type
     */
    List<Room> getAvailableRoomsByType(Long roomTypeId, LocalDate checkIn, LocalDate checkOut);

    /**
     * Check if a specific room is available for date range.
     * 
     * @param roomId   Room ID
     * @param checkIn  Check-in date
     * @param checkOut Check-out date
     * @return true if available, false otherwise
     */
    boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut);
}
