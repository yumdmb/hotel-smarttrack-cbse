package com.hotel.smarttrack.stay.impl;

import com.hotel.smarttrack.entity.*;
import com.hotel.smarttrack.service.*;
import org.osgi.service.component.annotations.*;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * StayManagerImpl - OSGi Declarative Services implementation of StayService.
 * Uses JDBC-based repositories with DataSource from Karaf's pax-jdbc.
 * 
 * Handles UC13-UC16:
 * - UC13: Check-In Guest
 * - UC14: Assign Room and Access Credentials
 * - UC15: Record Incidental Charges
 * - UC16: Check-Out Guest
 * 
 * @author Elvis Sawing (refactored for JDBC)
 */
@Component(service = StayService.class, immediate = true)
public class StayManagerImpl implements StayService {

    // Status constants
    private static final String STATUS_CHECKED_IN = "CHECKED_IN";
    private static final String STATUS_CHECKED_OUT = "CHECKED_OUT";
    private static final String ROOM_OCCUPIED = "OCCUPIED";
    private static final String ROOM_AVAILABLE = "AVAILABLE";
    private static final String ROOM_CLEANING = "UNDER_CLEANING";

    // ============ DataSource and Repositories ============

    @Reference(target = "(osgi.jndi.service.name=jdbc/hoteldb)")
    private DataSource dataSource;

    private StayRepository stayRepository;
    private IncidentalChargeRepository chargeRepository;

    // ============ OSGi Service References ============

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private volatile GuestService guestService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private volatile RoomService roomService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private volatile ReservationService reservationService;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    private volatile BillingService billingService;

    // ============ Lifecycle Methods ============

    @Activate
    public void activate() {
        System.out.println("==============================================");
        System.out.println("[StayManagerImpl] Bundle ACTIVATED ✅");
        System.out.println("  - Service Registered: StayService");
        System.out.println("  - Using H2 Database via JDBC DataSource");
        System.out.println("  - GuestService: " + (guestService != null ? "available" : "missing"));
        System.out.println("  - RoomService: " + (roomService != null ? "available" : "missing"));
        System.out.println("  - ReservationService: " + (reservationService != null ? "available" : "missing"));
        System.out.println("  - BillingService: " + (billingService != null ? "available" : "not yet"));
        System.out.println("==============================================");

        // Initialize repositories with DataSource
        this.stayRepository = new StayRepository(dataSource, guestService, roomService, reservationService);
        this.chargeRepository = new IncidentalChargeRepository(dataSource, stayRepository);

        System.out.println("[StayManagerImpl] Found " + stayRepository.count() + " stays in database");
        System.out.println("[StayManagerImpl] Found " + chargeRepository.count() + " incidental charges in database");
    }

    @Deactivate
    public void deactivate() {
        System.out.println("[StayManagerImpl] Bundle DEACTIVATED");
    }

    // ============ UC13: Check-In Operations ============

    @Override
    public Stay checkInGuest(Long reservationId) {
        Reservation reservation = reservationService.getReservationById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));

        String status = reservation.getStatus();
        if ("CANCELLED".equals(status) || "NO_SHOW".equals(status)) {
            throw new IllegalStateException("Cannot check-in: Reservation is " + status);
        }

        Room room = reservation.getAssignedRoom();
        if (room == null) {
            throw new IllegalStateException("No room assigned to reservation. Please assign a room first.");
        }

        Stay stay = new Stay();
        stay.setReservation(reservation);
        stay.setGuest(reservation.getGuest());
        stay.setRoom(room);
        stay.setCheckInTime(LocalDateTime.now());
        stay.setStatus(STATUS_CHECKED_IN);

        roomService.updateRoomStatus(room.getRoomId(), ROOM_OCCUPIED);

        Stay saved = stayRepository.save(stay);
        System.out.println("[StayManager] Checked in guest " + reservation.getGuest().getName()
                + " to room " + room.getRoomNumber());
        return saved;
    }

    @Override
    public Stay checkInWalkIn(Long guestId, Long roomId) {
        Guest guest = guestService.getGuestById(guestId)
                .orElseThrow(() -> new IllegalArgumentException("Guest not found: " + guestId));

        Room room = roomService.getRoomById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));

        if (!ROOM_AVAILABLE.equals(room.getStatus())) {
            throw new IllegalStateException("Room is not available. Current status: " + room.getStatus());
        }

        Stay stay = new Stay();
        stay.setGuest(guest);
        stay.setRoom(room);
        stay.setCheckInTime(LocalDateTime.now());
        stay.setStatus(STATUS_CHECKED_IN);

        roomService.updateRoomStatus(roomId, ROOM_OCCUPIED);

        Stay saved = stayRepository.save(stay);
        System.out.println("[StayManager] Walk-in check-in: " + guest.getName()
                + " to room " + room.getRoomNumber());
        return saved;
    }

    // ============ UC14: Assign Room and Access Credentials ============

    @Override
    public void assignRoomAndCredentials(Long stayId, Long roomId, String keyCardNumber) {
        Stay stay = stayRepository.findById(stayId)
                .orElseThrow(() -> new IllegalArgumentException("Stay not found: " + stayId));

        Room newRoom = roomService.getRoomById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));

        Room oldRoom = stay.getRoom();
        if (oldRoom != null && !oldRoom.getRoomId().equals(roomId)) {
            roomService.updateRoomStatus(oldRoom.getRoomId(), ROOM_AVAILABLE);
            System.out.println("[StayManager] Released room " + oldRoom.getRoomNumber());
        }

        stay.setRoom(newRoom);
        stay.setKeyCardNumber(keyCardNumber);
        roomService.updateRoomStatus(roomId, ROOM_OCCUPIED);
        stayRepository.save(stay);

        System.out.println("[StayManager] Assigned room " + newRoom.getRoomNumber()
                + " with key card " + keyCardNumber);
    }

    @Override
    public void issueNewKeyCard(Long stayId, String keyCardNumber) {
        Stay stay = stayRepository.findById(stayId)
                .orElseThrow(() -> new IllegalArgumentException("Stay not found: " + stayId));

        stay.setKeyCardNumber(keyCardNumber);
        stayRepository.save(stay);

        System.out.println("[StayManager] Issued new key card " + keyCardNumber
                + " for stay " + stayId);
    }

    // ============ UC15: Record Incidental Charges ============

    @Override
    public IncidentalCharge recordCharge(Long stayId, String serviceType,
            String description, BigDecimal amount) {
        Stay stay = stayRepository.findById(stayId)
                .orElseThrow(() -> new IllegalArgumentException("Stay not found: " + stayId));

        if (!STATUS_CHECKED_IN.equals(stay.getStatus())) {
            throw new IllegalStateException("Cannot record charge: Guest is not currently checked in");
        }

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

    @Override
    public BigDecimal getTotalIncidentalCharges(Long stayId) {
        return chargeRepository.getTotalChargesForStay(stayId);
    }

    @Override
    public void voidCharge(Long chargeId) {
        if (!chargeRepository.existsById(chargeId)) {
            throw new IllegalArgumentException("Charge not found: " + chargeId);
        }
        chargeRepository.delete(chargeId);
        System.out.println("[StayManager] Voided charge " + chargeId);
    }

    // ============ UC16: Check-Out Operations ============

    @Override
    public void checkOutGuest(Long stayId) {
        Stay stay = stayRepository.findById(stayId)
                .orElseThrow(() -> new IllegalArgumentException("Stay not found: " + stayId));

        if (!STATUS_CHECKED_IN.equals(stay.getStatus())) {
            throw new IllegalStateException("Guest is not currently checked in. Status: " + stay.getStatus());
        }

        stay.setCheckOutTime(LocalDateTime.now());
        stay.setStatus(STATUS_CHECKED_OUT);

        Room room = stay.getRoom();
        if (room != null) {
            roomService.updateRoomStatus(room.getRoomId(), ROOM_CLEANING);
        }

        stayRepository.save(stay);

        // Generate invoice if billing service available
        if (billingService != null) {
            try {
                billingService.generateInvoice(stayId);
            } catch (Exception e) {
                System.out.println("[StayManager] Warning: Could not generate invoice - " + e.getMessage());
            }
        }

        System.out.println("[StayManager] Checked out from room "
                + (room != null ? room.getRoomNumber() : "N/A"));
    }

    @Override
    public BigDecimal getOutstandingBalance(Long stayId) {
        Stay stay = stayRepository.findById(stayId).orElse(null);
        if (stay == null) {
            return BigDecimal.ZERO;
        }

        // If billing service is available and an invoice exists, use billing's
        // outstanding balance
        if (billingService != null) {
            var invoiceOpt = billingService.getInvoiceByStay(stayId);
            if (invoiceOpt.isPresent()) {
                Long invoiceId = invoiceOpt.get().getInvoiceId();
                return billingService.getOutstandingBalance(invoiceId);
            }
        }

        // No invoice yet - calculate raw charges
        BigDecimal roomCharges = calculateRoomCharges(stayId);
        BigDecimal incidentalTotal = chargeRepository.getTotalChargesForStay(stayId);

        BigDecimal subtotal = roomCharges.add(incidentalTotal);
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.10));

        return subtotal.add(tax);
    }

    @Override
    public BigDecimal calculateRoomCharges(Long stayId) {
        Stay stay = stayRepository.findById(stayId).orElse(null);
        if (stay == null || stay.getRoom() == null || stay.getRoom().getRoomType() == null) {
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

    // ============ Stay Queries ============

    @Override
    public Optional<Stay> getStayById(Long stayId) {
        return stayRepository.findById(stayId);
    }

    @Override
    public Optional<Stay> getActiveStayByRoom(String roomNumber) {
        return stayRepository.findAll().stream()
                .filter(s -> STATUS_CHECKED_IN.equals(s.getStatus()))
                .filter(s -> s.getRoom() != null && roomNumber.equals(s.getRoom().getRoomNumber()))
                .findFirst();
    }

    @Override
    public Optional<Stay> getActiveStayByRoomId(Long roomId) {
        return stayRepository.findAll().stream()
                .filter(s -> STATUS_CHECKED_IN.equals(s.getStatus()))
                .filter(s -> s.getRoom() != null && roomId.equals(s.getRoom().getRoomId()))
                .findFirst();
    }

    @Override
    public List<Stay> getActiveStays() {
        return stayRepository.findAll().stream()
                .filter(s -> STATUS_CHECKED_IN.equals(s.getStatus()))
                .toList();
    }

    @Override
    public List<Stay> getGuestStayHistory(Long guestId) {
        return stayRepository.findAll().stream()
                .filter(s -> s.getGuest() != null && guestId.equals(s.getGuest().getGuestId()))
                .toList();
    }

    @Override
    public List<Stay> getAllStays() {
        return stayRepository.findAll();
    }

    @Override
    public boolean isGuestCheckedIn(Long guestId) {
        return stayRepository.findAll().stream()
                .anyMatch(s -> STATUS_CHECKED_IN.equals(s.getStatus())
                        && s.getGuest() != null
                        && guestId.equals(s.getGuest().getGuestId()));
    }

    @Override
    public Optional<Stay> getActiveStayByGuest(Long guestId) {
        return stayRepository.findAll().stream()
                .filter(s -> STATUS_CHECKED_IN.equals(s.getStatus()))
                .filter(s -> s.getGuest() != null && guestId.equals(s.getGuest().getGuestId()))
                .findFirst();
    }
}
