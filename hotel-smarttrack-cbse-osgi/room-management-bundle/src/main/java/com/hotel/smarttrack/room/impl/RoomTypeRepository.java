package com.hotel.smarttrack.room.impl;

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

    public RoomType save(RoomType entity) {
        if (entity.getRoomTypeId() == null) {
            entity.setRoomTypeId(idGenerator.getAndIncrement());
        }
        storage.put(entity.getRoomTypeId(), entity);
        return entity;
    }

    public Optional<RoomType> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public Optional<RoomType> findByTypeNameIgnoreCase(String typeName) {
        return storage.values().stream()
                .filter(type -> typeName.equalsIgnoreCase(type.getTypeName()))
                .findFirst();
    }

    public List<RoomType> findAll() {
        return new ArrayList<>(storage.values());
    }

    public void delete(Long id) {
        storage.remove(id);
    }

    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }

    public boolean existsByTypeNameIgnoreCase(String typeName) {
        return findByTypeNameIgnoreCase(typeName).isPresent();
    }

    public long count() {
        return storage.size();
    }
}
