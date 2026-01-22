package com.hotel.smarttrack.reservation.impl;

import com.hotel.smarttrack.entity.Guest;
import com.hotel.smarttrack.entity.Reservation;
import com.hotel.smarttrack.entity.Room;
import com.hotel.smarttrack.entity.RoomType;
import com.hotel.smarttrack.service.GuestService;
import com.hotel.smarttrack.service.ReservationService;
import com.hotel.smarttrack.service.RoomService;
import org.osgi.service.component.annotations.*;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ReservationManagerImpl - OSGi DS implementation of ReservationService.
 * Uses JDBC-based repository with DataSource from Karaf's pax-jdbc.
 */
@Component(service = ReservationService.class, immediate = true)
public class ReservationManagerImpl implements ReservationService {

    // ============ DataSource and Repository ============

    @Reference(target = "(osgi.jndi.service.name=jdbc/hoteldb)")
    private DataSource dataSource;

    private ReservationRepository repo;

    // ============ OSGi Service References ============

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private volatile GuestService guestService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private volatile RoomService roomService;

    @Activate
    public void activate() {
        System.out.println("==============================================");
        System.out.println("[ReservationManagerImpl] Bundle ACTIVATED ✅");
        System.out.println("  - Service Registered: ReservationService");
        System.out.println("  - Using H2 Database via JDBC DataSource");
        System.out.println("  - GuestService: " + (guestService != null ? "available" : "missing"));
        System.out.println("  - RoomService: " + (roomService != null ? "available" : "missing"));
        System.out.println("  - UC9  Reservation Operations (CRUD)");
        System.out.println("  - UC10 Search Available Rooms");
        System.out.println("  - UC11 Manage Reservation Allocation");
        System.out.println("  - UC12 Track Reservation Status");
        System.out.println("==============================================");

        // Initialize repository with DataSource and services
        this.repo = new ReservationRepository(dataSource, guestService, roomService);
        System.out.println("[ReservationManagerImpl] Found " + repo.count() + " reservations in database");
    }

    @Deactivate
    public void deactivate() {
        System.out.println("[ReservationManagerImpl] Deactivated");
    }

    @Override
    public Reservation createReservation(Long guestId, Long roomTypeId, LocalDate checkIn,
            LocalDate checkOut, int numberOfGuests, String specialRequests) {
        if (checkIn == null || checkOut == null)
            throw new IllegalArgumentException("checkIn/checkOut required");
        if (!checkOut.isAfter(checkIn))
            throw new IllegalArgumentException("checkOut must be after checkIn");
        if (numberOfGuests <= 0)
            throw new IllegalArgumentException("numberOfGuests must be > 0");

        Guest guest = guestService.getGuestById(guestId)
                .orElseThrow(() -> new IllegalArgumentException("Guest not found: " + guestId));
        RoomType roomType = roomService.getRoomTypeById(roomTypeId)
                .orElseThrow(() -> new IllegalArgumentException("RoomType not found: " + roomTypeId));

        Reservation r = new Reservation();
        r.setGuest(guest);
        r.setRoomType(roomType);
        r.setCheckInDate(checkIn);
        r.setCheckOutDate(checkOut);
        r.setNumberOfGuests(numberOfGuests);
        r.setStatus("RESERVED");
        r.setSpecialRequests(specialRequests);

        return repo.save(r);
    }

    @Override
    public Reservation modifyReservation(Long reservationId, LocalDate newCheckIn,
            LocalDate newCheckOut, int numberOfGuests) {
        Reservation r = repo.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));

        String status = r.getStatus() == null ? "" : r.getStatus();
        if ("CHECKED_IN".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status)) {
            throw new IllegalStateException("Cannot modify reservation in status: " + status);
        }

        if (newCheckIn == null || newCheckOut == null)
            throw new IllegalArgumentException("newCheckIn/newCheckOut required");
        if (!newCheckOut.isAfter(newCheckIn))
            throw new IllegalArgumentException("newCheckOut must be after newCheckIn");
        if (numberOfGuests <= 0)
            throw new IllegalArgumentException("numberOfGuests must be > 0");

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
        if ("CHECKED_IN".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status)) {
            throw new IllegalStateException("Cannot cancel reservation in status: " + status);
        }

        r.setStatus("CANCELLED");
        repo.save(r);
    }

    @Override
    public void confirmReservation(Long reservationId) {
        Reservation r = repo.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));
        r.setStatus("CONFIRMED");
        repo.save(r);
    }

    @Override
    public void updateReservationStatus(Long reservationId, String status) {
        if (status == null || status.isBlank())
            throw new IllegalArgumentException("status required");
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
        List<Reservation> result = new ArrayList<>();
        for (Reservation r : repo.findAll()) {
            if (r.getGuest() != null && guestId.equals(r.getGuest().getGuestId())) {
                result.add(r);
            }
        }
        return result;
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
        return getReservationsByGuest(guestId);
    }

    @Override
    public void assignRoom(Long reservationId, Long roomId) {
        Reservation r = repo.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));
        if (roomId == null)
            throw new IllegalArgumentException("roomId required");

        Room room = roomService.getRoomById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));
        r.setAssignedRoom(room);
        repo.save(r);
    }

    @Override
    public void reassignRoom(Long reservationId, Long newRoomId) {
        assignRoom(reservationId, newRoomId);
    }

    @Override
    public void unassignRoom(Long reservationId) {
        Reservation r = repo.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));
        r.setAssignedRoom(null);
        repo.save(r);
    }

    @Override
    public List<Long> searchAvailableRooms(LocalDate checkIn, LocalDate checkOut, Long roomTypeId, int occupancy) {
        List<Long> result = new ArrayList<>();
        List<Room> available = roomService.getAvailableRooms(checkIn, checkOut);
        for (Room room : available) {
            if (roomTypeId == null || room.getRoomType().getRoomTypeId().equals(roomTypeId)) {
                if (room.getRoomType().getMaxOccupancy() >= occupancy) {
                    result.add(room.getRoomId());
                }
            }
        }
        return result;
    }

    @Override
    public void markNoShow(Long reservationId) {
        Reservation r = repo.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));
        r.setStatus("NO_SHOW");
        repo.save(r);
    }

    @Override
    public List<Reservation> getTodayArrivals() {
        LocalDate today = LocalDate.now();
        List<Reservation> out = new ArrayList<>();
        for (Reservation r : repo.findAll()) {
            if (today.equals(r.getCheckInDate()))
                out.add(r);
        }
        return out;
    }

    @Override
    public List<Reservation> getTodayDepartures() {
        LocalDate today = LocalDate.now();
        List<Reservation> out = new ArrayList<>();
        for (Reservation r : repo.findAll()) {
            if (today.equals(r.getCheckOutDate()))
                out.add(r);
        }
        return out;
    }
}
