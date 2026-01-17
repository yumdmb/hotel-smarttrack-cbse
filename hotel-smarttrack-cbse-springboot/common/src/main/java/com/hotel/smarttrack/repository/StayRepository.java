package com.hotel.smarttrack.repository;

import com.hotel.smarttrack.entity.Stay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for Stay entity.
 * Part of Base Library - provides data access for stay-related operations.
 */
@Repository
public interface StayRepository extends JpaRepository<Stay, Long> {

    /**
     * Find stays by guest ID.
     */
    @Query("SELECT s FROM Stay s WHERE s.guest.guestId = :guestId")
    List<Stay> findByGuestId(@Param("guestId") Long guestId);

    /**
     * Find stays by room ID.
     */
    @Query("SELECT s FROM Stay s WHERE s.room.roomId = :roomId")
    List<Stay> findByRoomId(@Param("roomId") Long roomId);

    /**
     * Find stay by reservation ID.
     */
    @Query("SELECT s FROM Stay s WHERE s.reservation.reservationId = :reservationId")
    Optional<Stay> findByReservationId(@Param("reservationId") Long reservationId);

    /**
     * Find stays by status.
     */
    List<Stay> findByStatus(String status);

    /**
     * Find all active stays (checked in but not checked out).
     */
    @Query("SELECT s FROM Stay s WHERE s.status = 'CHECKED_IN'")
    List<Stay> findAllActive();

    /**
     * Find active stay by room number.
     */
    @Query("SELECT s FROM Stay s WHERE s.room.roomNumber = :roomNumber AND s.status = 'CHECKED_IN'")
    Optional<Stay> findActiveByRoomNumber(@Param("roomNumber") String roomNumber);

    /**
     * Get guest stay history ordered by check-in time.
     */
    @Query("SELECT s FROM Stay s WHERE s.guest.guestId = :guestId ORDER BY s.checkInTime DESC")
    List<Stay> findGuestStayHistory(@Param("guestId") Long guestId);
}
