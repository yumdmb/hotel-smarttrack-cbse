package com.hotel.smarttrack.room;

import com.hotel.smarttrack.entity.RoomType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory repository for RoomType entities.
 * Thread-safe using ConcurrentHashMap.
 * 
 * @author Eisraq Rejab
 */
public class RoomTypeRepository {

    private final Map<Long, RoomType> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * Save a room type entity.
     * Generates ID if null.
     * 
     * @param entity RoomType entity
     * @return Saved room type with ID
     */
    public RoomType save(RoomType entity) {
        if (entity.getRoomTypeId() == null) {
            entity.setRoomTypeId(idGenerator.getAndIncrement());
        }
        storage.put(entity.getRoomTypeId(), entity);
        return entity;
    }

    /**
     * Find room type by ID.
     * 
     * @param id Room type ID
     * @return Optional containing room type if found
     */
    public Optional<RoomType> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    /**
     * Find room type by name (case-insensitive).
     * 
     * @param typeName Room type name
     * @return Optional containing room type if found
     */
    public Optional<RoomType> findByTypeNameIgnoreCase(String typeName) {
        return storage.values().stream()
                .filter(type -> typeName.equalsIgnoreCase(type.getTypeName()))
                .findFirst();
    }

    /**
     * Find all room types.
     * 
     * @return List of all room types
     */
    public List<RoomType> findAll() {
        return new ArrayList<>(storage.values());
    }

    /**
     * Delete room type by ID.
     * 
     * @param id Room type ID
     */
    public void delete(Long id) {
        storage.remove(id);
    }

    /**
     * Check if room type exists by ID.
     * 
     * @param id Room type ID
     * @return true if exists
     */
    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }

    /**
     * Check if room type exists by name (case-insensitive).
     * 
     * @param typeName Room type name
     * @return true if exists
     */
    public boolean existsByTypeNameIgnoreCase(String typeName) {
        return findByTypeNameIgnoreCase(typeName).isPresent();
    }

    /**
     * Count total room types.
     * 
     * @return Total number of room types
     */
    public long count() {
        return storage.size();
    }
}
