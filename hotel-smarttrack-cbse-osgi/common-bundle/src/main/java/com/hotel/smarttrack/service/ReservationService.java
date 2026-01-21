package com.hotel.smarttrack.service;

import com.hotel.smarttrack.entity.Reservation;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * ReservationService interface - exposes Reservation Management functionality.
 * Part of Base Library (CBSE Rule 5) - interface in common library.
 * 
 * <p>
 * Implemented by ReservationManager in reservation-management-bundle.
 * </p>
 * 
 * <p>
 * Handles UC9-UC12:
 * </p>
 * <ul>
 * <li>UC9: Perform Reservation Operations</li>
 * <li>UC10: Search for Available Rooms</li>
 * <li>UC11: Manage Reservation Allocation</li>
 * <li>UC12: Track Reservation Status</li>
 * </ul>
 * 
 * @author Li Yuhang
 */
public interface ReservationService {

    // ============ Reservation CRUD Operations (UC9) ============

    /**
     * Create a new reservation.
     * 
     * @param guestId         Guest ID
     * @param roomTypeId      Room type ID
     * @param checkIn         Check-in date
     * @param checkOut        Check-out date
     * @param numberOfGuests  Number of guests
     * @param specialRequests Special requests (optional)
     * @return Created reservation with generated ID
     * @throws IllegalArgumentException if guest or room type not found
     */
    Reservation createReservation(Long guestId, Long roomTypeId, LocalDate checkIn,
            LocalDate checkOut, int numberOfGuests, String specialRequests);

    /**
     * Modify an existing reservation.
     * 
     * @param reservationId  Reservation ID
     * @param newCheckIn     New check-in date
     * @param newCheckOut    New check-out date
     * @param numberOfGuests Number of guests
     * @return Updated reservation
     * @throws IllegalArgumentException if reservation not found
     * @throws IllegalStateException    if reservation is already checked-in or
     *                                  cancelled
     */
    Reservation modifyReservation(Long reservationId, LocalDate newCheckIn,
            LocalDate newCheckOut, int numberOfGuests);

    /**
     * Cancel a reservation.
     * 
     * @param reservationId Reservation ID
     * @throws IllegalArgumentException if reservation not found
     * @throws IllegalStateException    if reservation is already checked-in or
     *                                  already cancelled
     */
    void cancelReservation(Long reservationId);

    /**
     * Confirm a reservation.
     * 
     * @param reservationId Reservation ID
     * @throws IllegalArgumentException if reservation not found
     */
    void confirmReservation(Long reservationId);

    /**
     * Update reservation status.
     * 
     * @param reservationId Reservation ID
     * @param status        New status
     * @throws IllegalArgumentException if reservation not found or invalid status
     */
    void updateReservationStatus(Long reservationId, String status);

    // ============ Reservation Queries (UC12) ============

    /**
     * Get reservation by ID.
     * 
     * @param reservationId Reservation ID
     * @return Reservation if found
     */
    Optional<Reservation> getReservationById(Long reservationId);

    /**
     * Get reservations by guest.
     * 
     * @param guestId Guest ID
     * @return List of reservations for the guest
     */
    List<Reservation> getReservationsByGuest(Long guestId);

    /**
     * Get all reservations.
     * 
     * @return List of all reservations
     */
    List<Reservation> getAllReservations();

    /**
     * Get reservations by status.
     * 
     * @param status Reservation status
     * @return List of reservations with the specified status
     */
    List<Reservation> getReservationsByStatus(String status);

    /**
     * Get reservations for a date range.
     * 
     * @param startDate Start date
     * @param endDate   End date
     * @return List of reservations within the date range
     */
    List<Reservation> getReservationsByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Get reservation status.
     * 
     * @param reservationId Reservation ID
     * @return Reservation status
     * @throws IllegalArgumentException if reservation not found
     */
    String getReservationStatus(Long reservationId);

    /**
     * Get guest reservation history (including past reservations).
     * 
     * @param guestId Guest ID
     * @return List of all reservations for the guest
     */
    List<Reservation> getGuestReservationHistory(Long guestId);

    // ============ Room Allocation (UC11) ============

    /**
     * Assign a specific room to a reservation.
     * 
     * @param reservationId Reservation ID
     * @param roomId        Room ID
     * @throws IllegalArgumentException if reservation or room not found
     * @throws IllegalStateException    if room is not available
     */
    void assignRoom(Long reservationId, Long roomId);

    /**
     * Reassign a room to a reservation (change room assignment).
     * 
     * @param reservationId Reservation ID
     * @param newRoomId     New room ID
     * @throws IllegalArgumentException if reservation or room not found
     * @throws IllegalStateException    if new room is not available
     */
    void reassignRoom(Long reservationId, Long newRoomId);

    /**
     * Remove room assignment from a reservation.
     * 
     * @param reservationId Reservation ID
     * @throws IllegalArgumentException if reservation not found
     */
    void unassignRoom(Long reservationId);

    // ============ Room Search (UC10) ============

    /**
     * Search available rooms for reservation criteria.
     * 
     * @param checkIn    Check-in date
     * @param checkOut   Check-out date
     * @param roomTypeId Room type ID (optional, null for any type)
     * @param occupancy  Required occupancy
     * @return List of available room IDs
     */
    List<Long> searchAvailableRooms(LocalDate checkIn, LocalDate checkOut,
            Long roomTypeId, int occupancy);

    // ============ Status Updates ============

    /**
     * Mark reservation as no-show.
     * 
     * @param reservationId Reservation ID
     * @throws IllegalArgumentException if reservation not found
     */
    void markNoShow(Long reservationId);

    /**
     * Get today's expected arrivals.
     * 
     * @return List of reservations with today's check-in date
     */
    List<Reservation> getTodayArrivals();

    /**
     * Get today's expected departures.
     * 
     * @return List of reservations with today's check-out date
     */
    List<Reservation> getTodayDepartures();
}
