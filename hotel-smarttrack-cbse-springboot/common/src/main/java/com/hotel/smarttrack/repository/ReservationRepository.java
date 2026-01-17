package com.hotel.smarttrack.repository;

import com.hotel.smarttrack.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA Repository for Reservation entity.
 * Part of Base Library - provides data access for reservation-related
 * operations.
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * Find reservations by guest ID.
     */
    @Query("SELECT r FROM Reservation r WHERE r.guest.guestId = :guestId")
    List<Reservation> findByGuestId(@Param("guestId") Long guestId);

    /**
     * Find reservations by status.
     */
    List<Reservation> findByStatus(String status);

    /**
     * Find reservations by room type ID.
     */
    @Query("SELECT r FROM Reservation r WHERE r.roomType.roomTypeId = :roomTypeId")
    List<Reservation> findByRoomTypeId(@Param("roomTypeId") Long roomTypeId);

    /**
     * Find reservations with check-in date in range.
     */
    List<Reservation> findByCheckInDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Find reservations arriving today.
     */
    @Query("SELECT r FROM Reservation r WHERE r.checkInDate = :today AND r.status = 'CONFIRMED'")
    List<Reservation> findArrivingToday(@Param("today") LocalDate today);

    /**
     * Find reservations departing today.
     */
    @Query("SELECT r FROM Reservation r WHERE r.checkOutDate = :today AND r.status = 'CHECKED_IN'")
    List<Reservation> findDepartingToday(@Param("today") LocalDate today);

    /**
     * Find overlapping reservations for availability check.
     */
    @Query("SELECT r FROM Reservation r WHERE r.roomType.roomTypeId = :roomTypeId " +
            "AND r.status NOT IN ('CANCELLED', 'NO_SHOW') " +
            "AND r.checkInDate < :checkOut AND r.checkOutDate > :checkIn")
    List<Reservation> findOverlapping(
            @Param("roomTypeId") Long roomTypeId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);

    /**
     * Get guest reservation history ordered by check-in date.
     */
    @Query("SELECT r FROM Reservation r WHERE r.guest.guestId = :guestId ORDER BY r.checkInDate DESC")
    List<Reservation> findGuestReservationHistory(@Param("guestId") Long guestId);
}
