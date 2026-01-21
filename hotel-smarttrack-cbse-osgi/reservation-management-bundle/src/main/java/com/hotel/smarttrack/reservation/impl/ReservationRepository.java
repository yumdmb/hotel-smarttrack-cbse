package com.hotel.smarttrack.reservation.impl;

import com.hotel.smarttrack.entity.Reservation;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ReservationRepository {

    private final Map<Long, Reservation> reservations = new ConcurrentHashMap<>();
    private final Map<Long, Long> assignedRoomByReservationId = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    public Reservation save(Reservation r) {
        if (r.getReservationId() == null) {
            r.setReservationId(idGen.getAndIncrement());
        }
        reservations.put(r.getReservationId(), r);
        return r;
    }

    public Optional<Reservation> findById(Long id) {
        return Optional.ofNullable(reservations.get(id));
    }

    public List<Reservation> findAll() {
        return new ArrayList<>(reservations.values());
    }

    public List<Reservation> findByStatus(String status) {
        if (status == null) return List.of();
        List<Reservation> out = new ArrayList<>();
        for (Reservation r : reservations.values()) {
            if (r.getStatus() != null && status.equalsIgnoreCase(r.getStatus())) {
                out.add(r);
            }
        }
        return out;
    }

    public List<Reservation> findByDateRange(LocalDate start, LocalDate end) {
        if (start == null || end == null) return List.of();
        List<Reservation> out = new ArrayList<>();
        for (Reservation r : reservations.values()) {
            LocalDate in = r.getCheckInDate();
            LocalDate outDate = r.getCheckOutDate();
            if (in == null || outDate == null) continue;

            boolean overlap = !outDate.isBefore(start) && !in.isAfter(end);
            if (overlap) out.add(r);
        }
        return out;
    }

    public void delete(Long id) {
        reservations.remove(id);
        assignedRoomByReservationId.remove(id);
    }

    public void assignRoom(Long reservationId, Long roomId) {
        assignedRoomByReservationId.put(reservationId, roomId);
    }

    public Optional<Long> getAssignedRoomId(Long reservationId) {
        return Optional.ofNullable(assignedRoomByReservationId.get(reservationId));
    }

    public void unassignRoom(Long reservationId) {
        assignedRoomByReservationId.remove(reservationId);
    }
}