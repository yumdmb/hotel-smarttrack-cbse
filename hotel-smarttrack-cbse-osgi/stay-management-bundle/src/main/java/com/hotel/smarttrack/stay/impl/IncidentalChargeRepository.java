package com.hotel.smarttrack.stay.impl;

import com.hotel.smarttrack.entity.IncidentalCharge;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * In-memory repository for IncidentalCharge entities.
 * Thread-safe using ConcurrentHashMap.
 * 
 * @author Elvis Sawing
 */
public class IncidentalChargeRepository {

    private final Map<Long, IncidentalCharge> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public IncidentalCharge save(IncidentalCharge entity) {
        if (entity.getChargeId() == null) {
            entity.setChargeId(idGenerator.getAndIncrement());
        }
        storage.put(entity.getChargeId(), entity);
        return entity;
    }

    public Optional<IncidentalCharge> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<IncidentalCharge> findAll() {
        return new ArrayList<>(storage.values());
    }

    public void delete(Long id) {
        storage.remove(id);
    }

    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }

    /**
     * Find all charges for a specific stay.
     */
    public List<IncidentalCharge> findByStayId(Long stayId) {
        return storage.values().stream()
                .filter(charge -> charge.getStay() != null
                        && stayId.equals(charge.getStay().getStayId()))
                .collect(Collectors.toList());
    }

    /**
     * Get total charges amount for a stay.
     */
    public BigDecimal getTotalChargesForStay(Long stayId) {
        return findByStayId(stayId).stream()
                .map(IncidentalCharge::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long count() {
        return storage.size();
    }
}
