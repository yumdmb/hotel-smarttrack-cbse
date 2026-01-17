package com.hotel.smarttrack.repository;

import com.hotel.smarttrack.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for Room entity.
 * Part of Base Library - provides data access for room-related operations.
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    /**
     * Find room by room number.
     */
    Optional<Room> findByRoomNumber(String roomNumber);

    /**
     * Find all rooms by status.
     */
    List<Room> findByStatus(String status);

    /**
     * Find all rooms on a specific floor.
     */
    List<Room> findByFloorNumber(int floorNumber);

    /**
     * Find all rooms of a specific room type.
     */
    @Query("SELECT r FROM Room r WHERE r.roomType.roomTypeId = :roomTypeId")
    List<Room> findByRoomTypeId(@Param("roomTypeId") Long roomTypeId);

    /**
     * Find all available rooms (status = 'Available').
     */
    @Query("SELECT r FROM Room r WHERE r.status = 'Available'")
    List<Room> findAllAvailable();

    /**
     * Find all available rooms of a specific type.
     */
    @Query("SELECT r FROM Room r WHERE r.status = 'Available' AND r.roomType.roomTypeId = :roomTypeId")
    List<Room> findAvailableByRoomTypeId(@Param("roomTypeId") Long roomTypeId);

    /**
     * Check if a room number already exists.
     */
    boolean existsByRoomNumber(String roomNumber);
}
