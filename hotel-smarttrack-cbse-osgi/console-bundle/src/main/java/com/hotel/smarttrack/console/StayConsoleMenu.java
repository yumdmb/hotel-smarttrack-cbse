package com.hotel.smarttrack.console;

import com.hotel.smarttrack.entity.IncidentalCharge;
import com.hotel.smarttrack.entity.Room;
import com.hotel.smarttrack.entity.Stay;
import com.hotel.smarttrack.service.GuestService;
import com.hotel.smarttrack.service.ReservationService;
import com.hotel.smarttrack.service.RoomService;
import com.hotel.smarttrack.service.StayService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Stay Management Console Menu.
 * Provides check-in, check-out, and incidental charge operations via terminal UI.
 */
public class StayConsoleMenu {

    private final StayService stayService;
    private final GuestService guestService;
    private final RoomService roomService;
    private final ReservationService reservationService;
    private final ConsoleInputHelper input;

    public StayConsoleMenu(StayService stayService,
                           GuestService guestService,
                           RoomService roomService,
                           ReservationService reservationService,
                           ConsoleInputHelper input) {
        this.stayService = stayService;
        this.guestService = guestService;
        this.roomService = roomService;
        this.reservationService = reservationService;
        this.input = input;
    }

    public void showMenu() {
        boolean running = true;
        while (running) {
            input.println("\n==============================");
            input.println("       STAY MANAGEMENT        ");
            input.println("==============================");
            input.println("1. View All Active Stays");
            input.println("2. View Stay by ID");
            input.println("3. Check-In (from Reservation)");
            input.println("4. Check-In (Walk-In)");
            input.println("5. Check-Out");
            input.println("6. Record Incidental Charge");
            input.println("7. View Charges for Stay");
            input.println("8. View Outstanding Balance");
            input.println("9. View Guest Stay History");
            input.println("0. Back to Main Menu");

            String choice = input.readLine("Choose: ");
            try {
                switch (choice) {
                    case "1" -> viewActiveStays();
                    case "2" -> viewStayById();
                    case "3" -> checkInFromReservation();
                    case "4" -> checkInWalkIn();
                    case "5" -> checkOut();
                    case "6" -> recordCharge();
                    case "7" -> viewChargesForStay();
                    case "8" -> viewOutstandingBalance();
                    case "9" -> viewGuestStayHistory();
                    case "0" -> running = false;
                    default -> input.println("Invalid option.");
                }
            } catch (IllegalArgumentException | IllegalStateException ex) {
                input.println("❌ Error: " + ex.getMessage());
            } catch (Exception ex) {
                input.println("❌ Unexpected error: " + ex.getMessage());
            }
        }
    }

    private void viewActiveStays() {
        List<Stay> stays = stayService.getActiveStays();
        if (stays.isEmpty()) {
            input.println("No active stays.");
            return;
        }
        input.println("\n--- Active Stays ---");
        stays.forEach(this::printStay);
    }

    private void viewStayById() {
        Long id = input.readLong("Stay ID: ");
        Optional<Stay> stay = stayService.getStayById(id);
        stay.ifPresentOrElse(
            this::printStayDetails,
            () -> input.println("Stay not found.")
        );
    }

    private void checkInFromReservation() {
        // Show confirmed reservations
        input.println("\n--- Confirmed Reservations ---");
        reservationService.getAllReservations().stream()
            .filter(r -> "CONFIRMED".equalsIgnoreCase(r.getStatus()) || "Confirmed".equals(r.getStatus()))
            .forEach(r -> input.println(String.format("ID=%d | Guest: %s | Room: %s | %s",
                r.getReservationId(),
                r.getGuest() != null ? r.getGuest().getName() : "N/A",
                r.getAssignedRoom() != null ? r.getAssignedRoom().getRoomNumber() : "Not assigned",
                r.getCheckInDate())));
        
        Long reservationId = input.readLong("Reservation ID to check in: ");
        
        Stay stay = stayService.checkInGuest(reservationId);
        input.println("✅ Guest checked in successfully!");
        printStayDetails(stay);
    }

    private void checkInWalkIn() {
        // Show available guests
        input.println("\n--- Guests ---");
        guestService.getAllGuests().forEach(g ->
            input.println(String.format("ID=%d | %s | %s", g.getGuestId(), g.getName(), g.getEmail())));
        
        Long guestId = input.readLong("Guest ID: ");
        
        // Show available rooms for today
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        input.println("\n--- Available Rooms ---");
        List<Room> availableRooms = roomService.getAvailableRooms(today, tomorrow);
        availableRooms.forEach(r ->
            input.println(String.format("ID=%d | Room %s | %s | $%s/night",
                r.getRoomId(), r.getRoomNumber(),
                r.getRoomType() != null ? r.getRoomType().getTypeName() : "N/A",
                r.getRoomType() != null ? r.getRoomType().getBasePrice() : "N/A")));
        
        if (availableRooms.isEmpty()) {
            input.println("No available rooms.");
            return;
        }
        
        Long roomId = input.readLong("Room ID: ");
        
        Stay stay = stayService.checkInWalkIn(guestId, roomId);
        input.println("✅ Walk-in guest checked in successfully!");
        printStayDetails(stay);
    }

    private void checkOut() {
        // Show active stays
        input.println("\n--- Active Stays ---");
        stayService.getActiveStays().forEach(s ->
            input.println(String.format("ID=%d | Guest: %s | Room: %s",
                s.getStayId(),
                s.getGuest() != null ? s.getGuest().getName() : "N/A",
                s.getRoom() != null ? s.getRoom().getRoomNumber() : "N/A")));
        
        Long stayId = input.readLong("Stay ID to check out: ");
        
        stayService.checkOutGuest(stayId);
        input.println("✅ Guest checked out successfully!");
    }

    private void recordCharge() {
        Long stayId = input.readLong("Stay ID: ");
        String serviceType = input.readLine("Service Type (Minibar/Room Service/Laundry/Spa/Phone/Other): ");
        String description = input.readLine("Description: ");
        BigDecimal amount = readBigDecimal("Amount: ");
        
        IncidentalCharge charge = stayService.recordCharge(stayId, serviceType, description, amount);
        input.println("✅ Charge recorded: $" + charge.getAmount());
    }

    private void viewChargesForStay() {
        Long stayId = input.readLong("Stay ID: ");
        List<IncidentalCharge> charges = stayService.getChargesForStay(stayId);
        if (charges.isEmpty()) {
            input.println("No charges for this stay.");
            return;
        }
        input.println("\n--- Incidental Charges ---");
        BigDecimal total = BigDecimal.ZERO;
        for (IncidentalCharge c : charges) {
            input.println(String.format("  %s - %s: $%s (%s)",
                c.getServiceType(), c.getDescription(), c.getAmount(), c.getChargeTime()));
            total = total.add(c.getAmount());
        }
        input.println("Total: $" + total);
    }

    private void viewOutstandingBalance() {
        Long stayId = input.readLong("Stay ID: ");
        BigDecimal balance = stayService.getOutstandingBalance(stayId);
        input.println("Outstanding Balance: $" + balance);
    }

    private void viewGuestStayHistory() {
        Long guestId = input.readLong("Guest ID: ");
        List<Stay> stays = stayService.getGuestStayHistory(guestId);
        if (stays.isEmpty()) {
            input.println("No stay history for this guest.");
            return;
        }
        input.println("\n--- Guest Stay History ---");
        stays.forEach(this::printStay);
    }

    private void printStay(Stay stay) {
        input.println(String.format("ID=%d | Guest: %s | Room: %s | Status: %s | In: %s | Out: %s",
            stay.getStayId(),
            stay.getGuest() != null ? stay.getGuest().getName() : "N/A",
            stay.getRoom() != null ? stay.getRoom().getRoomNumber() : "N/A",
            stay.getStatus(),
            stay.getCheckInTime(),
            stay.getCheckOutTime() != null ? stay.getCheckOutTime() : "Still checked in"));
    }

    private void printStayDetails(Stay stay) {
        input.println("\n--- Stay Details ---");
        input.println("Stay ID:        " + stay.getStayId());
        input.println("Guest:          " + (stay.getGuest() != null ? stay.getGuest().getName() : "N/A"));
        input.println("Room:           " + (stay.getRoom() != null ? stay.getRoom().getRoomNumber() : "N/A"));
        input.println("Check-In Time:  " + stay.getCheckInTime());
        input.println("Check-Out Time: " + (stay.getCheckOutTime() != null ? stay.getCheckOutTime() : "Still checked in"));
        input.println("Status:         " + stay.getStatus());
        input.println("Key Card:       " + (stay.getKeyCardNumber() != null ? stay.getKeyCardNumber() : "Not assigned"));
    }

    // ============ Utility Methods ============

    private BigDecimal readBigDecimal(String prompt) {
        while (true) {
            String s = input.readLine(prompt);
            try {
                return new BigDecimal(s);
            } catch (NumberFormatException e) {
                input.println("Please enter a valid decimal number.");
            }
        }
    }
}
