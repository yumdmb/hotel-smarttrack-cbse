package com.hotel.smarttrack.service;

import com.hotel.smarttrack.entity.IncidentalCharge;
import com.hotel.smarttrack.entity.Stay;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * StayService interface - exposes Check-In/Check-Out Management functionality.
 * Part of Base Library (CBSE Rule 5) - interface in common library.
 * 
 * <p>
 * Implemented by StayManager in stay-management-bundle.
 * </p>
 * 
 * <p>
 * Handles UC13-UC16:
 * </p>
 * <ul>
 * <li>UC13: Check-In Guest</li>
 * <li>UC14: Assign Room and Access Credentials</li>
 * <li>UC15: Record Incidental Charges</li>
 * <li>UC16: Check-Out Guest</li>
 * </ul>
 * 
 * @author Elvis Sawing
 */
public interface StayService {

    // ============ Check-In Operations (UC13) ============

    /**
     * Check in a guest with a reservation.
     * 
     * @param reservationId Reservation ID
     * @return Created stay record
     * @throws IllegalArgumentException if reservation not found
     * @throws IllegalStateException    if reservation is cancelled or already
     *                                  checked-in
     */
    Stay checkInGuest(Long reservationId);

    /**
     * Check in a walk-in guest (no prior reservation).
     * 
     * @param guestId Guest ID
     * @param roomId  Room ID
     * @return Created stay record
     * @throws IllegalArgumentException if guest or room not found
     * @throws IllegalStateException    if room is not available
     */
    Stay checkInWalkIn(Long guestId, Long roomId);

    // ============ Room Assignment (UC14) ============

    /**
     * Assign room and issue key card.
     * 
     * @param stayId        Stay ID
     * @param roomId        Room ID
     * @param keyCardNumber Key card number
     * @throws IllegalArgumentException if stay or room not found
     */
    void assignRoomAndCredentials(Long stayId, Long roomId, String keyCardNumber);

    /**
     * Issue new key card for a stay.
     * 
     * @param stayId        Stay ID
     * @param keyCardNumber New key card number
     * @throws IllegalArgumentException if stay not found
     */
    void issueNewKeyCard(Long stayId, String keyCardNumber);

    // ============ Incidental Charges (UC15) ============

    /**
     * Record an incidental charge during stay.
     * 
     * @param stayId      Stay ID
     * @param serviceType Type of service (F&B, Laundry, Minibar, Room Service, Spa,
     *                    etc.)
     * @param description Charge description
     * @param amount      Charge amount
     * @return Created charge record
     * @throws IllegalArgumentException if stay not found
     * @throws IllegalStateException    if guest is not currently checked in
     */
    IncidentalCharge recordCharge(Long stayId, String serviceType, String description, BigDecimal amount);

    /**
     * Get all charges for a stay.
     * 
     * @param stayId Stay ID
     * @return List of incidental charges
     */
    List<IncidentalCharge> getChargesForStay(Long stayId);

    /**
     * Get total incidental charges for a stay.
     * 
     * @param stayId Stay ID
     * @return Total amount of incidental charges
     */
    BigDecimal getTotalIncidentalCharges(Long stayId);

    /**
     * Void/cancel an incidental charge.
     * 
     * @param chargeId Charge ID
     * @throws IllegalArgumentException if charge not found
     */
    void voidCharge(Long chargeId);

    // ============ Check-Out Operations (UC16) ============

    /**
     * Check out a guest.
     * 
     * @param stayId Stay ID
     * @throws IllegalArgumentException if stay not found
     * @throws IllegalStateException    if guest is not currently checked in
     */
    void checkOutGuest(Long stayId);

    /**
     * Get outstanding balance for a stay.
     * 
     * @param stayId Stay ID
     * @return Outstanding balance (room charges + incidentals + tax - payments)
     */
    BigDecimal getOutstandingBalance(Long stayId);

    /**
     * Calculate room charges for a stay.
     * 
     * @param stayId Stay ID
     * @return Room charges based on nights and room rate
     */
    BigDecimal calculateRoomCharges(Long stayId);

    // ============ Stay Queries ============

    /**
     * Get stay by ID.
     * 
     * @param stayId Stay ID
     * @return Stay if found
     */
    Optional<Stay> getStayById(Long stayId);

    /**
     * Get active stay by room number.
     * 
     * @param roomNumber Room number
     * @return Active stay if found
     */
    Optional<Stay> getActiveStayByRoom(String roomNumber);

    /**
     * Get active stay by room ID.
     * 
     * @param roomId Room ID
     * @return Active stay if found
     */
    Optional<Stay> getActiveStayByRoomId(Long roomId);

    /**
     * Get all active stays (currently checked-in guests).
     * 
     * @return List of active stays
     */
    List<Stay> getActiveStays();

    /**
     * Get stay history for a guest.
     * 
     * @param guestId Guest ID
     * @return List of stays for the guest
     */
    List<Stay> getGuestStayHistory(Long guestId);

    /**
     * Get all stays.
     * 
     * @return List of all stays
     */
    List<Stay> getAllStays();

    /**
     * Check if a guest is currently checked in.
     * 
     * @param guestId Guest ID
     * @return true if guest has an active stay
     */
    boolean isGuestCheckedIn(Long guestId);

    /**
     * Get active stay for a guest.
     * 
     * @param guestId Guest ID
     * @return Active stay if found
     */
    Optional<Stay> getActiveStayByGuest(Long guestId);
}
