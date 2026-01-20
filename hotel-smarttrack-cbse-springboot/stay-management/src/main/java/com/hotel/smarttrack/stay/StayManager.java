package com.hotel.smarttrack.stay;

import com.hotel.smarttrack.entity.*;
import com.hotel.smarttrack.repository.IncidentalChargeRepository;
import com.hotel.smarttrack.repository.StayRepository;
import com.hotel.smarttrack.service.GuestService;
import com.hotel.smarttrack.service.ReservationService;
import com.hotel.smarttrack.service.RoomService;
import com.hotel.smarttrack.service.StayService;
// TODO: Uncomment when Billing module is ready
// import com.hotel.smarttrack.service.BillingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * StayManager - Implementation of StayService.
 * Business logic for Check-In/Check-Out Management (Rule 2 & 3).
 * 
 * Handles UC13-UC16:
 * - UC13: Check-In Guest
 * - UC14: Assign Room and Access Credentials
 * - UC15: Record Incidental Charges
 * - UC16: Check-Out Guest
 * 
 * Cross-module dependencies (via service interfaces):
 * - RoomService: Update room status during check-in/out
 * - ReservationService: Verify and update reservation status
 * - GuestService: Validate walk-in guests
 * - BillingService: Generate invoice at checkout (TODO: future integration)
 * 
 * @author Elvis Sawing
 */
@Service
public class StayManager implements StayService {

    // Status constants for Stay
    private static final String STATUS_CHECKED_IN = "CHECKED_IN";
    private static final String STATUS_CHECKED_OUT = "CHECKED_OUT";

    // Status constants for Room
    private static final String ROOM_OCCUPIED = "Occupied";
    private static final String ROOM_AVAILABLE = "Available";
    private static final String ROOM_CLEANING = "Under Cleaning";

    // Own repositories
    private final StayRepository stayRepository;
    private final IncidentalChargeRepository chargeRepository;

    // Cross-module service dependencies
    private final RoomService roomService;
    private final ReservationService reservationService;
    private final GuestService guestService;

    // TODO: Uncomment when Billing module is ready (for UC16 invoice generation)
    // private final BillingService billingService;

    public StayManager(StayRepository stayRepository,
            IncidentalChargeRepository chargeRepository,
            RoomService roomService,
            ReservationService reservationService,
            GuestService guestService
    // TODO: Uncomment when Billing module is ready
    // , BillingService billingService
    ) {
        this.stayRepository = stayRepository;
        this.chargeRepository = chargeRepository;
        this.roomService = roomService;
        this.reservationService = reservationService;
        this.guestService = guestService;
        // this.billingService = billingService;
    }

    // ============ UC13: Check-In Operations ============

    @Override
    @Transactional
    public Stay checkInGuest(Long reservationId) {
        // Get reservation via service
        Reservation reservation = reservationService.getReservationById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));

        // Validate reservation status
        String status = reservation.getStatus();
        if ("Cancelled".equals(status) || "No-Show".equals(status)) {
            throw new IllegalStateException("Cannot check-in: Reservation is " + status);
        }

        // Get assigned room
        Room room = reservation.getAssignedRoom();
        if (room == null) {
            throw new IllegalStateException("No room assigned to reservation. Please assign a room first.");
        }

        // Create stay record
        Stay stay = new Stay();
        stay.setReservation(reservation);
        stay.setGuest(reservation.getGuest());
        stay.setRoom(room);
        stay.setCheckInTime(LocalDateTime.now());
        stay.setStatus(STATUS_CHECKED_IN);

        // Update room status via RoomService
        roomService.updateRoomStatus(room.getRoomId(), ROOM_OCCUPIED);

        // Update reservation status - note: ideally ReservationService should have
        // updateStatus method
        // For now, we'll update directly but this should be changed when
        // ReservationService is implemented
        reservation.setStatus("Checked-In");
        reservationService.confirmReservation(reservationId); // Using existing method as proxy

        Stay saved = stayRepository.save(stay);
        System.out.println("[StayManager] Checked in guest " + reservation.getGuest().getName()
                + " to room " + room.getRoomNumber());
        return saved;
    }

    @Override
    @Transactional
    public Stay checkInWalkIn(Long guestId, Long roomId) {
        // Get guest via GuestService
        Guest guest = guestService.getGuestById(guestId)
                .orElseThrow(() -> new IllegalArgumentException("Guest not found: " + guestId));

        // Get room via RoomService
        Room room = roomService.getRoomById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));

        // Validate room is available
        if (!ROOM_AVAILABLE.equals(room.getStatus())) {
            throw new IllegalStateException("Room is not available. Current status: " + room.getStatus());
        }

        // Create stay without reservation linkage
        Stay stay = new Stay();
        stay.setGuest(guest);
        stay.setRoom(room);
        stay.setCheckInTime(LocalDateTime.now());
        stay.setStatus(STATUS_CHECKED_IN);

        // Update room status via RoomService
        roomService.updateRoomStatus(roomId, ROOM_OCCUPIED);

        Stay saved = stayRepository.save(stay);
        System.out.println("[StayManager] Walk-in check-in: " + guest.getName()
                + " to room " + room.getRoomNumber());
        return saved;
    }

    // ============ UC14: Assign Room and Access Credentials ============

    @Override
    @Transactional
    public void assignRoomAndCredentials(Long stayId, Long roomId, String keyCardNumber) {
        Stay stay = stayRepository.findById(stayId)
                .orElseThrow(() -> new IllegalArgumentException("Stay not found: " + stayId));

        // Get new room via RoomService
        Room newRoom = roomService.getRoomById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));

        // Release old room if different
        Room oldRoom = stay.getRoom();
        if (oldRoom != null && !oldRoom.getRoomId().equals(roomId)) {
            // Update old room status via RoomService
            roomService.updateRoomStatus(oldRoom.getRoomId(), ROOM_AVAILABLE);
            System.out.println("[StayManager] Released room " + oldRoom.getRoomNumber());
        }

        // Assign new room
        stay.setRoom(newRoom);
        stay.setKeyCardNumber(keyCardNumber);

        // Update new room status via RoomService
        roomService.updateRoomStatus(roomId, ROOM_OCCUPIED);
        stayRepository.save(stay);

        System.out.println("[StayManager] Assigned room " + newRoom.getRoomNumber()
                + " with key card " + keyCardNumber);
    }

    // ============ UC15: Record Incidental Charges ============

    @Override
    @Transactional
    public IncidentalCharge recordCharge(Long stayId, String serviceType,
            String description, BigDecimal amount) {
        Stay stay = stayRepository.findById(stayId)
                .orElseThrow(() -> new IllegalArgumentException("Stay not found: " + stayId));

        // Validate stay is active
        if (!STATUS_CHECKED_IN.equals(stay.getStatus())) {
            throw new IllegalStateException("Cannot record charge: Guest is not currently checked in");
        }

        // Create incidental charge
        IncidentalCharge charge = new IncidentalCharge();
        charge.setStay(stay);
        charge.setServiceType(serviceType);
        charge.setDescription(description);
        charge.setAmount(amount);
        charge.setChargeTime(LocalDateTime.now());

        IncidentalCharge saved = chargeRepository.save(charge);
        System.out.println("[StayManager] Recorded " + serviceType + " charge: $" + amount
                + " - " + description);
        return saved;
    }

    @Override
    public List<IncidentalCharge> getChargesForStay(Long stayId) {
        return chargeRepository.findByStayId(stayId);
    }

    // ============ UC16: Check-Out Operations ============

    @Override
    @Transactional
    public void checkOutGuest(Long stayId) {
        Stay stay = stayRepository.findById(stayId)
                .orElseThrow(() -> new IllegalArgumentException("Stay not found: " + stayId));

        // Validate guest is checked in
        if (!STATUS_CHECKED_IN.equals(stay.getStatus())) {
            throw new IllegalStateException("Guest is not currently checked in. Status: " + stay.getStatus());
        }

        // Finalize stay
        stay.setCheckOutTime(LocalDateTime.now());
        stay.setStatus(STATUS_CHECKED_OUT);

        // Update room to cleaning status via RoomService
        Room room = stay.getRoom();
        if (room != null) {
            roomService.updateRoomStatus(room.getRoomId(), ROOM_CLEANING);
        }

        // Update reservation if linked
        // Note: ReservationService should have a method to update status
        // This is a workaround - proper implementation would be
        // reservationService.updateStatus()
        Reservation reservation = stay.getReservation();
        if (reservation != null) {
            // TODO: Use reservationService.updateReservationStatus() when available
            reservation.setStatus("Checked-Out");
        }

        stayRepository.save(stay);

        // TODO: Generate invoice via BillingService when module is ready (UC16)
        // BigDecimal roomCharges = calculateRoomCharges(stay);
        // List<IncidentalCharge> charges = chargeRepository.findByStayId(stayId);
        // billingService.generateInvoice(stay, roomCharges, charges);

        System.out.println("[StayManager] Checked out from room "
                + (room != null ? room.getRoomNumber() : "N/A"));
    }

    @Override
    public BigDecimal getOutstandingBalance(Long stayId) {
        Stay stay = stayRepository.findById(stayId).orElse(null);
        if (stay == null) {
            return BigDecimal.ZERO;
        }

        // Calculate room charges
        BigDecimal roomCharges = calculateRoomCharges(stay);

        // Get incidental charges total
        BigDecimal incidentalTotal = chargeRepository.getTotalChargesForStay(stayId);
        if (incidentalTotal == null) {
            incidentalTotal = BigDecimal.ZERO;
        }

        // Apply 10% tax
        BigDecimal subtotal = roomCharges.add(incidentalTotal);
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.10));

        BigDecimal total = subtotal.add(tax);
        System.out.println("[StayManager] Outstanding balance for stay " + stayId + ": $" + total
                + " (Room: $" + roomCharges + ", Incidentals: $" + incidentalTotal + ", Tax: $" + tax + ")");
        return total;
    }

    /**
     * Calculate room charges based on nights stayed and room rate.
     */
    private BigDecimal calculateRoomCharges(Stay stay) {
        if (stay.getRoom() == null || stay.getRoom().getRoomType() == null) {
            return BigDecimal.ZERO;
        }

        LocalDateTime checkIn = stay.getCheckInTime();
        LocalDateTime checkOut = stay.getCheckOutTime() != null
                ? stay.getCheckOutTime()
                : LocalDateTime.now();

        long nights = ChronoUnit.DAYS.between(checkIn.toLocalDate(), checkOut.toLocalDate());
        if (nights < 1) {
            nights = 1; // Minimum 1 night charge
        }

        BigDecimal rate = stay.getRoom().getRoomType().getBasePrice();
        if (rate == null) {
            rate = BigDecimal.valueOf(100.00); // Default rate
        }

        return rate.multiply(BigDecimal.valueOf(nights));
    }

    // ============ Query Operations ============

    @Override
    public Optional<Stay> getStayById(Long stayId) {
        return stayRepository.findById(stayId);
    }

    @Override
    public Optional<Stay> getActiveStayByRoom(String roomNumber) {
        return stayRepository.findActiveByRoomNumber(roomNumber);
    }

    @Override
    public List<Stay> getActiveStays() {
        return stayRepository.findAllActive();
    }

    @Override
    public List<Stay> getGuestStayHistory(Long guestId) {
        return stayRepository.findGuestStayHistory(guestId);
    }
}
