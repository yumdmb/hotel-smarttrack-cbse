package com.hotel.smarttrack.reservation;

import com.hotel.smarttrack.entity.Reservation;
import com.hotel.smarttrack.entity.Guest;
import com.hotel.smarttrack.entity.RoomType;
import com.hotel.smarttrack.entity.Room;
import com.hotel.smarttrack.repository.ReservationRepository;
import com.hotel.smarttrack.repository.GuestRepository;
import com.hotel.smarttrack.repository.RoomRepository;
import com.hotel.smarttrack.repository.RoomTypeRepository;
import com.hotel.smarttrack.service.ReservationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * ReservationManager - Implementation of ReservationService.
 * Business logic for Reservation Management component.
 */
@Service
public class ReservationManager implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final GuestRepository guestRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RoomRepository roomRepository;

    public ReservationManager(ReservationRepository reservationRepository,
            GuestRepository guestRepository,
            RoomTypeRepository roomTypeRepository,
            RoomRepository roomRepository) {
        this.reservationRepository = reservationRepository;
        this.guestRepository = guestRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    @Transactional
    public Reservation createReservation(Long guestId, Long roomTypeId, LocalDate checkIn,
            LocalDate checkOut, int numberOfGuests, String specialRequests) {
        if (guestId == null || roomTypeId == null) {
            throw new IllegalArgumentException("guestId and roomTypeId cannot be null.");
        }
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("checkOut must be after checkIn.");
        }

        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new IllegalArgumentException("Guest not found: " + guestId));

        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new IllegalArgumentException("RoomType not found: " + roomTypeId));

        Reservation reservation = new Reservation();
        reservation.setGuest(guest);
        reservation.setRoomType(roomType);
        reservation.setCheckInDate(checkIn);
        reservation.setCheckOutDate(checkOut);
        reservation.setNumberOfGuests(numberOfGuests);
        reservation.setSpecialRequests(specialRequests);
        reservation.setStatus("Reserved");

        Reservation saved = reservationRepository.save(reservation);
        System.out.println("[ReservationManager] Created reservation ID=" + saved.getReservationId());
        return saved;
    }

    @Override
    @Transactional
    public Reservation modifyReservation(Long reservationId, LocalDate newCheckIn,
            LocalDate newCheckOut, int numberOfGuests) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));

        if (newCheckIn != null)
            reservation.setCheckInDate(newCheckIn);
        if (newCheckOut != null)
            reservation.setCheckOutDate(newCheckOut);
        if (numberOfGuests > 0)
            reservation.setNumberOfGuests(numberOfGuests);

        return reservationRepository.save(reservation);
    }

    @Override
    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));

        reservation.setStatus("Cancelled");
        reservationRepository.save(reservation);
        System.out.println("[ReservationManager] Cancelled reservation ID=" + reservationId);
    }

    @Override
    @Transactional
    public void confirmReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));

        reservation.setStatus("Confirmed");
        reservationRepository.save(reservation);
        System.out.println("[ReservationManager] Confirmed reservation ID=" + reservationId);
    }

    @Override
    public Optional<Reservation> getReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId);
    }

    @Override
    public List<Reservation> getReservationsByGuest(Long guestId) {
        return reservationRepository.findByGuestId(guestId);
    }

    @Override
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    @Override
    public List<Reservation> getReservationsByStatus(String status) {
        return reservationRepository.findByStatus(status);
    }

    @Override
    @Transactional
    public void assignRoom(Long reservationId, Long roomId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));

        reservation.setAssignedRoom(room);
        reservationRepository.save(reservation);
        System.out.println("[ReservationManager] Assigned room " + roomId + " to reservation " + reservationId);
    }

    @Override
    @Transactional
    public void reassignRoom(Long reservationId, Long newRoomId) {
        assignRoom(reservationId, newRoomId);
    }

    @Override
    public List<Long> searchAvailableRooms(LocalDate checkIn, LocalDate checkOut,
            Long roomTypeId, int occupancy) {
        // TODO: Implement availability search after merging with room-management
        throw new UnsupportedOperationException(
                "searchAvailableRooms not yet implemented - needs room-management integration");
    }

    @Override
    @Transactional
    public void markNoShow(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));

        reservation.setStatus("No-Show");
        reservationRepository.save(reservation);
    }

    @Override
    public String getReservationStatus(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .map(Reservation::getStatus)
                .orElse(null);
    }

    @Override
    public List<Reservation> getGuestReservationHistory(Long guestId) {
        return reservationRepository.findGuestReservationHistory(guestId);
    }
}
