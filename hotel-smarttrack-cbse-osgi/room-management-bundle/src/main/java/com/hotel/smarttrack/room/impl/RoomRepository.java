package com.hotel.smarttrack.room.impl;

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

    public Room save(Room entity) {
        if (entity.getRoomId() == null) {
            entity.setRoomId(idGenerator.getAndIncrement());
        }
        storage.put(entity.getRoomId(), entity);
        return entity;
    }

    public Optional<Room> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public Optional<Room> findByRoomNumber(String roomNumber) {
        return storage.values().stream()
                .filter(room -> roomNumber.equals(room.getRoomNumber()))
                .findFirst();
    }

    public List<Room> findAll() {
        return new ArrayList<>(storage.values());
    }

    public List<Room> findByStatus(String status) {
        return storage.values().stream()
                .filter(room -> status.equals(room.getStatus()))
                .collect(Collectors.toList());
    }

    public List<Room> findByFloorNumber(int floorNumber) {
        return storage.values().stream()
                .filter(room -> room.getFloorNumber() == floorNumber)
                .collect(Collectors.toList());
    }

    public List<Room> findByRoomTypeId(Long roomTypeId) {
        return storage.values().stream()
                .filter(room -> room.getRoomType() != null
                        && roomTypeId.equals(room.getRoomType().getRoomTypeId()))
                .collect(Collectors.toList());
    }

    public List<Room> findAllAvailable() {
        return findByStatus("AVAILABLE");
    }

    public void delete(Long id) {
        storage.remove(id);
    }

    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }

    public boolean existsByRoomNumber(String roomNumber) {
        return findByRoomNumber(roomNumber).isPresent();
    }

    public long count() {
        return storage.size();
    }
}
