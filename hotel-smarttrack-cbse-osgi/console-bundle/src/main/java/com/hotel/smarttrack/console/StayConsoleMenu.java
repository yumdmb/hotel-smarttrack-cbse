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
import java.util.Scanner;

/**
 * Stay Management Console Menu.
 * Provides check-in, check-out, and incidental charge operations via terminal UI.
 */
public class StayConsoleMenu {

    private final StayService stayService;
    private final GuestService guestService;
    private final RoomService roomService;
    private final ReservationService reservationService;

    public StayConsoleMenu(StayService stayService,
                           GuestService guestService,
                           RoomService roomService,
                           ReservationService reservationService) {
        this.stayService = stayService;
        this.guestService = guestService;
        this.roomService = roomService;
        this.reservationService = reservationService;
    }

    public void showMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("\n==============================");
            System.out.println("       STAY MANAGEMENT        ");
            System.out.println("==============================");
            System.out.println("1. View All Active Stays");
            System.out.println("2. View Stay by ID");
            System.out.println("3. Check-In (from Reservation)");
            System.out.println("4. Check-In (Walk-In)");
            System.out.println("5. Check-Out");
            System.out.println("6. Record Incidental Charge");
            System.out.println("7. View Charges for Stay");
            System.out.println("8. View Outstanding Balance");
            System.out.println("9. View Guest Stay History");
            System.out.println("0. Back to Main Menu");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> viewActiveStays();
                    case "2" -> viewStayById(scanner);
                    case "3" -> checkInFromReservation(scanner);
                    case "4" -> checkInWalkIn(scanner);
                    case "5" -> checkOut(scanner);
                    case "6" -> recordCharge(scanner);
                    case "7" -> viewChargesForStay(scanner);
                    case "8" -> viewOutstandingBalance(scanner);
                    case "9" -> viewGuestStayHistory(scanner);
                    case "0" -> running = false;
                    default -> System.out.println("Invalid option.");
                }
            } catch (IllegalArgumentException | IllegalStateException ex) {
                System.out.println("❌ Error: " + ex.getMessage());
            } catch (Exception ex) {
                System.out.println("❌ Unexpected error: " + ex.getMessage());
            }
        }
    }

    private void viewActiveStays() {
        List<Stay> stays = stayService.getActiveStays();
        if (stays.isEmpty()) {
            System.out.println("No active stays.");
            return;
        }
        System.out.println("\n--- Active Stays ---");
        stays.forEach(this::printStay);
    }

    private void viewStayById(Scanner scanner) {
        Long id = readLong(scanner, "Stay ID: ");
        Optional<Stay> stay = stayService.getStayById(id);
        stay.ifPresentOrElse(
            this::printStayDetails,
            () -> System.out.println("Stay not found.")
        );
    }

    private void checkInFromReservation(Scanner scanner) {
        // Show confirmed reservations
        System.out.println("\n--- Confirmed Reservations ---");
        reservationService.getAllReservations().stream()
            .filter(r -> "CONFIRMED".equalsIgnoreCase(r.getStatus()) || "Confirmed".equals(r.getStatus()))
            .forEach(r -> System.out.printf("ID=%d | Guest: %s | Room: %s | %s%n",
                r.getReservationId(),
                r.getGuest() != null ? r.getGuest().getName() : "N/A",
                r.getAssignedRoom() != null ? r.getAssignedRoom().getRoomNumber() : "Not assigned",
                r.getCheckInDate()));
        
        Long reservationId = readLong(scanner, "Reservation ID to check in: ");
        
        Stay stay = stayService.checkInGuest(reservationId);
        System.out.println("✅ Guest checked in successfully!");
        printStayDetails(stay);
    }

    private void checkInWalkIn(Scanner scanner) {
        // Show available guests
        System.out.println("\n--- Guests ---");
        guestService.getAllGuests().forEach(g ->
            System.out.printf("ID=%d | %s | %s%n", g.getGuestId(), g.getName(), g.getEmail()));
        
        Long guestId = readLong(scanner, "Guest ID: ");
        
        // Show available rooms for today
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        System.out.println("\n--- Available Rooms ---");
        List<Room> availableRooms = roomService.getAvailableRooms(today, tomorrow);
        availableRooms.forEach(r ->
            System.out.printf("ID=%d | Room %s | %s | $%s/night%n",
                r.getRoomId(), r.getRoomNumber(),
                r.getRoomType() != null ? r.getRoomType().getTypeName() : "N/A",
                r.getRoomType() != null ? r.getRoomType().getBasePrice() : "N/A"));
        
        if (availableRooms.isEmpty()) {
            System.out.println("No available rooms.");
            return;
        }
        
        Long roomId = readLong(scanner, "Room ID: ");
        
        Stay stay = stayService.checkInWalkIn(guestId, roomId);
        System.out.println("✅ Walk-in guest checked in successfully!");
        printStayDetails(stay);
    }

    private void checkOut(Scanner scanner) {
        // Show active stays
        System.out.println("\n--- Active Stays ---");
        stayService.getActiveStays().forEach(s ->
            System.out.printf("ID=%d | Guest: %s | Room: %s%n",
                s.getStayId(),
                s.getGuest() != null ? s.getGuest().getName() : "N/A",
                s.getRoom() != null ? s.getRoom().getRoomNumber() : "N/A"));
        
        Long stayId = readLong(scanner, "Stay ID to check out: ");
        
        stayService.checkOutGuest(stayId);
        System.out.println("✅ Guest checked out successfully!");
    }

    private void recordCharge(Scanner scanner) {
        Long stayId = readLong(scanner, "Stay ID: ");
        System.out.print("Service Type (Minibar/Room Service/Laundry/Spa/Phone/Other): ");
        String serviceType = scanner.nextLine();
        System.out.print("Description: ");
        String description = scanner.nextLine();
        BigDecimal amount = readBigDecimal(scanner, "Amount: ");
        
        IncidentalCharge charge = stayService.recordCharge(stayId, serviceType, description, amount);
        System.out.println("✅ Charge recorded: $" + charge.getAmount());
    }

    private void viewChargesForStay(Scanner scanner) {
        Long stayId = readLong(scanner, "Stay ID: ");
        List<IncidentalCharge> charges = stayService.getChargesForStay(stayId);
        if (charges.isEmpty()) {
            System.out.println("No charges for this stay.");
            return;
        }
        System.out.println("\n--- Incidental Charges ---");
        BigDecimal total = BigDecimal.ZERO;
        for (IncidentalCharge c : charges) {
            System.out.printf("  %s - %s: $%s (%s)%n",
                c.getServiceType(), c.getDescription(), c.getAmount(), c.getChargeTime());
            total = total.add(c.getAmount());
        }
        System.out.println("Total: $" + total);
    }

    private void viewOutstandingBalance(Scanner scanner) {
        Long stayId = readLong(scanner, "Stay ID: ");
        BigDecimal balance = stayService.getOutstandingBalance(stayId);
        System.out.println("Outstanding Balance: $" + balance);
    }

    private void viewGuestStayHistory(Scanner scanner) {
        Long guestId = readLong(scanner, "Guest ID: ");
        List<Stay> stays = stayService.getGuestStayHistory(guestId);
        if (stays.isEmpty()) {
            System.out.println("No stay history for this guest.");
            return;
        }
        System.out.println("\n--- Guest Stay History ---");
        stays.forEach(this::printStay);
    }

    private void printStay(Stay stay) {
        System.out.printf("ID=%d | Guest: %s | Room: %s | Status: %s | In: %s | Out: %s%n",
            stay.getStayId(),
            stay.getGuest() != null ? stay.getGuest().getName() : "N/A",
            stay.getRoom() != null ? stay.getRoom().getRoomNumber() : "N/A",
            stay.getStatus(),
            stay.getCheckInTime(),
            stay.getCheckOutTime() != null ? stay.getCheckOutTime() : "Still checked in");
    }

    private void printStayDetails(Stay stay) {
        System.out.println("\n--- Stay Details ---");
        System.out.println("Stay ID:        " + stay.getStayId());
        System.out.println("Guest:          " + (stay.getGuest() != null ? stay.getGuest().getName() : "N/A"));
        System.out.println("Room:           " + (stay.getRoom() != null ? stay.getRoom().getRoomNumber() : "N/A"));
        System.out.println("Check-In Time:  " + stay.getCheckInTime());
        System.out.println("Check-Out Time: " + (stay.getCheckOutTime() != null ? stay.getCheckOutTime() : "Still checked in"));
        System.out.println("Status:         " + stay.getStatus());
        System.out.println("Key Card:       " + (stay.getKeyCardNumber() != null ? stay.getKeyCardNumber() : "Not assigned"));
    }

    // ============ Utility Methods ============

    private Long readLong(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = scanner.nextLine().trim();
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private BigDecimal readBigDecimal(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = scanner.nextLine().trim();
            try {
                return new BigDecimal(s);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid decimal number.");
            }
        }
    }
}
