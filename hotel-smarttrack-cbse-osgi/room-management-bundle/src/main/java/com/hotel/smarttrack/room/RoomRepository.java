package com.hotel.smarttrack.room;

import com.hotel.smarttrack.entity.Room;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * In-memory repository for Room entities.
 * Thread-safe using ConcurrentHashMap.
 * 
 * @author Eisraq Rejab
 */
public class RoomRepository {

    private final Map<Long, Room> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * Save a room entity.
     * Generates ID if null.
     * 
     * @param entity Room entity
     * @return Saved room with ID
     */
    public Room save(Room entity) {
        if (entity.getRoomId() == null) {
            entity.setRoomId(idGenerator.getAndIncrement());
        }
        storage.put(entity.getRoomId(), entity);
        return entity;
    }

    /**
     * Find room by ID.
     * 
     * @param id Room ID
     * @return Optional containing room if found
     */
    public Optional<Room> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    /**
     * Find room by room number.
     * 
     * @param roomNumber Room number
     * @return Optional containing room if found
     */
    public Optional<Room> findByRoomNumber(String roomNumber) {
        return storage.values().stream()
                .filter(room -> roomNumber.equals(room.getRoomNumber()))
                .findFirst();
    }

    /**
     * Find all rooms.
     * 
     * @return List of all rooms
     */
    public List<Room> findAll() {
        return new ArrayList<>(storage.values());
    }

    /**
     * Find rooms by status.
     * 
     * @param status Room status
     * @return List of rooms with the specified status
     */
    public List<Room> findByStatus(String status) {
        return storage.values().stream()
                .filter(room -> status.equals(room.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * Find rooms by floor number.
     * 
     * @param floorNumber Floor number
     * @return List of rooms on the specified floor
     */
    public List<Room> findByFloorNumber(int floorNumber) {
        return storage.values().stream()
                .filter(room -> room.getFloorNumber() == floorNumber)
                .collect(Collectors.toList());
    }

    /**
     * Find rooms by room type ID.
     * 
     * @param roomTypeId Room type ID
     * @return List of rooms of the specified type
     */
    public List<Room> findByRoomTypeId(Long roomTypeId) {
        return storage.values().stream()
                .filter(room -> room.getRoomType() != null 
                        && roomTypeId.equals(room.getRoomType().getRoomTypeId()))
                .collect(Collectors.toList());
    }

    /**
     * Find all available rooms (status = "Available").
     * 
     * @return List of available rooms
     */
    public List<Room> findAllAvailable() {
        return findByStatus("Available");
    }

    /**
     * Delete room by ID.
     * 
     * @param id Room ID
     */
    public void delete(Long id) {
        storage.remove(id);
    }

    /**
     * Check if room exists by ID.
     * 
     * @param id Room ID
     * @return true if exists
     */
    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }

    /**
     * Check if room exists by room number.
     * 
     * @param roomNumber Room number
     * @return true if exists
     */
    public boolean existsByRoomNumber(String roomNumber) {
        return findByRoomNumber(roomNumber).isPresent();
    }

    /**
     * Count total rooms.
     * 
     * @return Total number of rooms
     */
    public long count() {
        return storage.size();
    }
}
