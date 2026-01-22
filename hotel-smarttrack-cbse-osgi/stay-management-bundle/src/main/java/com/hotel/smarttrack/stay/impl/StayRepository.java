package com.hotel.smarttrack.stay.impl;

import com.hotel.smarttrack.entity.Stay;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory repository for Stay entities.
 * Thread-safe using ConcurrentHashMap.
 * 
 * @author Elvis Sawing
 */
public class StayRepository {

    private final Map<Long, Stay> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Stay save(Stay entity) {
        if (entity.getStayId() == null) {
            entity.setStayId(idGenerator.getAndIncrement());
        }
        storage.put(entity.getStayId(), entity);
        return entity;
    }

    public Optional<Stay> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<Stay> findAll() {
        return new ArrayList<>(storage.values());
    }

    public void delete(Long id) {
        storage.remove(id);
    }

    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }

    public long count() {
        return storage.size();
    }
}
