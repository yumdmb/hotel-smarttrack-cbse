package com.hotel.smarttrack.reservation.impl;

import com.hotel.smarttrack.entity.Reservation;
import com.hotel.smarttrack.service.ReservationService;
import org.osgi.service.component.annotations.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component(service = ReservationService.class, immediate = true)
public class ReservationManager implements ReservationService {

    private final ReservationRepository repo = new ReservationRepository();

    @Activate
    public void activate() {
        System.out.println("==============================================");
        System.out.println("[ReservationManager] Bundle ACTIVATED ✅");
        System.out.println("  - Service Registered: ReservationService");
        System.out.println("  - UC9  Reservation Operations (CRUD)");
        System.out.println("  - UC10 Search Available Rooms (stub)");
        System.out.println("  - UC11 Manage Reservation Allocation");
        System.out.println("  - UC12 Track Reservation Status");
        System.out.println("==============================================");

        Reservation r = new Reservation();
        r.setStatus("Reserved");
        r.setNumberOfGuests(1);
        r.setCheckInDate(LocalDate.now().plusDays(1));
        r.setCheckOutDate(LocalDate.now().plusDays(2));
        r.setSpecialRequests("Seed reservation (safe)");
        repo.save(r);

        System.out.println("[ReservationManager] Seed: created 1 reservation ✅");
    }

    @Deactivate
    public void deactivate() {
        System.out.println("[ReservationManager] Deactivated");
    }

    @Override
    public Reservation createReservation(Long guestId, Long roomTypeId, LocalDate checkIn,
                                         LocalDate checkOut, int numberOfGuests, String specialRequests) {
        if (checkIn == null || checkOut == null) throw new IllegalArgumentException("checkIn/checkOut required");
        if (!checkOut.isAfter(checkIn)) throw new IllegalArgumentException("checkOut must be after checkIn");
        if (numberOfGuests <= 0) throw new IllegalArgumentException("numberOfGuests must be > 0");

        Reservation r = new Reservation();
        r.setCheckInDate(checkIn);
        r.setCheckOutDate(checkOut);
        r.setNumberOfGuests(numberOfGuests);
        r.setStatus("Reserved");
        r.setSpecialRequests(specialRequests);

        return repo.save(r);
    }

    @Override
    public Reservation modifyReservation(Long reservationId, LocalDate newCheckIn,
                                         LocalDate newCheckOut, int numberOfGuests) {
        Reservation r = repo.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));

        String status = r.getStatus() == null ? "" : r.getStatus();
        if ("Checked-In".equalsIgnoreCase(status) || "Cancelled".equalsIgnoreCase(status)) {
            throw new IllegalStateException("Cannot modify reservation in status: " + status);
        }

        if (newCheckIn == null || newCheckOut == null) throw new IllegalArgumentException("newCheckIn/newCheckOut required");
        if (!newCheckOut.isAfter(newCheckIn)) throw new IllegalArgumentException("newCheckOut must be after newCheckIn");
        if (numberOfGuests <= 0) throw new IllegalArgumentException("numberOfGuests must be > 0");

        r.setCheckInDate(newCheckIn);
        r.setCheckOutDate(newCheckOut);
        r.setNumberOfGuests(numberOfGuests);
        return repo.save(r);
    }

    @Override
    public void cancelReservation(Long reservationId) {
        Reservation r = repo.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));

        String status = r.getStatus() == null ? "" : r.getStatus();
        if ("Checked-In".equalsIgnoreCase(status) || "Cancelled".equalsIgnoreCase(status)) {
            throw new IllegalStateException("Cannot cancel reservation in status: " + status);
        }

        r.setStatus("Cancelled");
        repo.save(r);
    }

    @Override
    public void confirmReservation(Long reservationId) {
        Reservation r = repo.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));
        r.setStatus("Confirmed");
        repo.save(r);
    }

    @Override
    public void updateReservationStatus(Long reservationId, String status) {
        if (status == null || status.isBlank()) throw new IllegalArgumentException("status required");
        Reservation r = repo.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));
        r.setStatus(status);
        repo.save(r);
    }

    @Override
    public Optional<Reservation> getReservationById(Long reservationId) {
        return repo.findById(reservationId);
    }

    @Override
    public List<Reservation> getReservationsByGuest(Long guestId) {
        return List.of();
    }

    @Override
    public List<Reservation> getAllReservations() {
        return repo.findAll();
    }

    @Override
    public List<Reservation> getReservationsByStatus(String status) {
        return repo.findByStatus(status);
    }

    @Override
    public List<Reservation> getReservationsByDateRange(LocalDate startDate, LocalDate endDate) {
        return repo.findByDateRange(startDate, endDate);
    }

    @Override
    public String getReservationStatus(Long reservationId) {
        Reservation r = repo.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));
        return r.getStatus();
    }

    @Override
    public List<Reservation> getGuestReservationHistory(Long guestId) {
        return List.of();
    }

    @Override
    public void assignRoom(Long reservationId, Long roomId) {
        repo.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));
        if (roomId == null) throw new IllegalArgumentException("roomId required");
        repo.assignRoom(reservationId, roomId);
    }

    @Override
    public void reassignRoom(Long reservationId, Long newRoomId) {
        if (newRoomId == null) throw new IllegalArgumentException("newRoomId required");
        repo.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));
        repo.assignRoom(reservationId, newRoomId);
    }

    @Override
    public void unassignRoom(Long reservationId) {
        repo.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));
        repo.unassignRoom(reservationId);
    }

    @Override
    public List<Long> searchAvailableRooms(LocalDate checkIn, LocalDate checkOut, Long roomTypeId, int occupancy) {
        return List.of();
    }

    @Override
    public void markNoShow(Long reservationId) {
        Reservation r = repo.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));
        r.setStatus("No-Show");
        repo.save(r);
    }

    @Override
    public List<Reservation> getTodayArrivals() {
        LocalDate today = LocalDate.now();
        List<Reservation> out = new ArrayList<>();
        for (Reservation r : repo.findAll()) {
            if (today.equals(r.getCheckInDate())) out.add(r);
        }
        return out;
    }

    @Override
    public List<Reservation> getTodayDepartures() {
        LocalDate today = LocalDate.now();
        List<Reservation> out = new ArrayList<>();
        for (Reservation r : repo.findAll()) {
            if (today.equals(r.getCheckOutDate())) out.add(r);
        }
        return out;
    }
}