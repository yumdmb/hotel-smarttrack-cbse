package com.hotel.smarttrack.stay;

import com.hotel.smarttrack.entity.IncidentalCharge;
import com.hotel.smarttrack.entity.Stay;
import com.hotel.smarttrack.service.StayService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * StayManagementConsole - Interactive Terminal UI for Stay Management.
 * Provides a menu-driven interface for check-in/check-out operations.
 * Called from MainMenuConsole in application module.
 * 
 * @author Elvis Sawing
 */
@Component
public class StayManagementConsole {

    private final StayService stayService;
    private Scanner scanner;

    public StayManagementConsole(StayService stayService) {
        this.stayService = stayService;
    }

    /**
     * Display the Stay Management menu and handle user input.
     * Called from MainMenuConsole.
     */
    public void showMenu(Scanner scanner) {
        this.scanner = scanner;

        System.out.println("\n");
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║       STAY MANAGEMENT - CHECK-IN/CHECK-OUT               ║");
        System.out.println("║                    (Elvis Sawing)                        ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");

        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("\nEnter choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> checkInWithReservation();
                case "2" -> checkInWalkIn();
                case "3" -> assignRoom();
                case "4" -> recordCharge();
                case "5" -> viewCharges();
                case "6" -> viewBalance();
                case "7" -> checkOut();
                case "8" -> viewActiveStays();
                case "9" -> viewStayByRoom();
                case "10" -> viewGuestHistory();
                case "0" -> {
                    System.out.println("\nReturning to Main Menu...");
                    running = false;
                }
                default -> System.out.println("\n⚠ Invalid choice. Please try again.");
            }
        }
    }

    private void printMenu() {
        System.out.println("\n┌─────────────────────────────────────┐");
        System.out.println("│         STAY MANAGEMENT MENU        │");
        System.out.println("├─────────────────────────────────────┤");
        System.out.println("│  CHECK-IN                           │");
        System.out.println("│  1. Check-in with Reservation       │");
        System.out.println("│  2. Walk-in Check-in                │");
        System.out.println("│  3. Assign/Change Room              │");
        System.out.println("├─────────────────────────────────────┤");
        System.out.println("│  DURING STAY                        │");
        System.out.println("│  4. Record Incidental Charge        │");
        System.out.println("│  5. View Stay Charges               │");
        System.out.println("│  6. View Outstanding Balance        │");
        System.out.println("├─────────────────────────────────────┤");
        System.out.println("│  CHECK-OUT                          │");
        System.out.println("│  7. Check-out Guest                 │");
        System.out.println("├─────────────────────────────────────┤");
        System.out.println("│  QUERIES                            │");
        System.out.println("│  8. View All Active Stays           │");
        System.out.println("│  9. Find Stay by Room Number        │");
        System.out.println("│  10. View Guest Stay History        │");
        System.out.println("├─────────────────────────────────────┤");
        System.out.println("│  0. Exit                            │");
        System.out.println("└─────────────────────────────────────┘");
    }

    // ============ UC13: Check-In ============

    private void checkInWithReservation() {
        System.out.println("\n=== CHECK-IN WITH RESERVATION ===");
        System.out.print("Enter Reservation ID: ");
        try {
            Long reservationId = Long.parseLong(scanner.nextLine().trim());
            Stay stay = stayService.checkInGuest(reservationId);
            System.out.println("\n✓ Check-in successful!");
            printStayDetails(stay);
        } catch (NumberFormatException e) {
            System.out.println("✗ Error: Invalid ID format");
        } catch (Exception e) {
            System.out.println("✗ Error: " + e.getMessage());
        }
    }

    private void checkInWalkIn() {
        System.out.println("\n=== WALK-IN CHECK-IN ===");
        try {
            System.out.print("Enter Guest ID: ");
            Long guestId = Long.parseLong(scanner.nextLine().trim());
            System.out.print("Enter Room ID: ");
            Long roomId = Long.parseLong(scanner.nextLine().trim());

            Stay stay = stayService.checkInWalkIn(guestId, roomId);
            System.out.println("\n✓ Walk-in check-in successful!");
            printStayDetails(stay);
        } catch (NumberFormatException e) {
            System.out.println("✗ Error: Invalid ID format");
        } catch (Exception e) {
            System.out.println("✗ Error: " + e.getMessage());
        }
    }

    // ============ UC14: Room Assignment ============

    private void assignRoom() {
        System.out.println("\n=== ASSIGN/CHANGE ROOM ===");
        try {
            System.out.print("Enter Stay ID: ");
            Long stayId = Long.parseLong(scanner.nextLine().trim());
            System.out.print("Enter New Room ID: ");
            Long roomId = Long.parseLong(scanner.nextLine().trim());
            System.out.print("Enter Key Card Number: ");
            String keyCard = scanner.nextLine().trim();

            stayService.assignRoomAndCredentials(stayId, roomId, keyCard);
            System.out.println("\n✓ Room assigned successfully!");
        } catch (NumberFormatException e) {
            System.out.println("✗ Error: Invalid ID format");
        } catch (Exception e) {
            System.out.println("✗ Error: " + e.getMessage());
        }
    }

    // ============ UC15: Incidental Charges ============

    private void recordCharge() {
        System.out.println("\n=== RECORD INCIDENTAL CHARGE ===");
        try {
            System.out.print("Enter Stay ID: ");
            Long stayId = Long.parseLong(scanner.nextLine().trim());

            System.out.println("Service Types: F&B, Minibar, Laundry, Room Service, Spa, Other");
            System.out.print("Enter Service Type: ");
            String serviceType = scanner.nextLine().trim();

            System.out.print("Enter Description: ");
            String description = scanner.nextLine().trim();

            System.out.print("Enter Amount ($): ");
            BigDecimal amount = new BigDecimal(scanner.nextLine().trim());

            IncidentalCharge charge = stayService.recordCharge(stayId, serviceType, description, amount);
            System.out.println("\n✓ Charge recorded successfully!");
            System.out.println("  Charge ID: " + charge.getChargeId());
            System.out.println("  Service: " + charge.getServiceType());
            System.out.println("  Amount: $" + charge.getAmount());
        } catch (NumberFormatException e) {
            System.out.println("✗ Error: Invalid number format");
        } catch (Exception e) {
            System.out.println("✗ Error: " + e.getMessage());
        }
    }

    private void viewCharges() {
        System.out.println("\n=== VIEW STAY CHARGES ===");
        try {
            System.out.print("Enter Stay ID: ");
            Long stayId = Long.parseLong(scanner.nextLine().trim());

            List<IncidentalCharge> charges = stayService.getChargesForStay(stayId);
            if (charges.isEmpty()) {
                System.out.println("No charges recorded for this stay.");
            } else {
                System.out.println("\n┌────────┬────────────────┬────────────────────────┬──────────┐");
                System.out.println("│ ID     │ Service Type   │ Description            │ Amount   │");
                System.out.println("├────────┼────────────────┼────────────────────────┼──────────┤");
                BigDecimal total = BigDecimal.ZERO;
                for (IncidentalCharge c : charges) {
                    System.out.printf("│ %-6d │ %-14s │ %-22s │ $%-7.2f │%n",
                            c.getChargeId(),
                            truncate(c.getServiceType(), 14),
                            truncate(c.getDescription(), 22),
                            c.getAmount());
                    total = total.add(c.getAmount());
                }
                System.out.println("├────────┴────────────────┴────────────────────────┼──────────┤");
                System.out.printf("│ TOTAL                                            │ $%-7.2f │%n", total);
                System.out.println("└──────────────────────────────────────────────────┴──────────┘");
            }
        } catch (NumberFormatException e) {
            System.out.println("✗ Error: Invalid ID format");
        }
    }

    private void viewBalance() {
        System.out.println("\n=== VIEW OUTSTANDING BALANCE ===");
        try {
            System.out.print("Enter Stay ID: ");
            Long stayId = Long.parseLong(scanner.nextLine().trim());

            BigDecimal balance = stayService.getOutstandingBalance(stayId);
            System.out.println("\n┌─────────────────────────────────┐");
            System.out.printf("│  Outstanding Balance: $%-8.2f │%n", balance);
            System.out.println("└─────────────────────────────────┘");
        } catch (NumberFormatException e) {
            System.out.println("✗ Error: Invalid ID format");
        }
    }

    // ============ UC16: Check-Out ============

    private void checkOut() {
        System.out.println("\n=== CHECK-OUT GUEST ===");
        try {
            System.out.print("Enter Stay ID: ");
            Long stayId = Long.parseLong(scanner.nextLine().trim());

            // Show balance first
            BigDecimal balance = stayService.getOutstandingBalance(stayId);
            System.out.println("\nOutstanding Balance: $" + balance);

            System.out.print("Confirm check-out? (y/n): ");
            String confirm = scanner.nextLine().trim().toLowerCase();

            if ("y".equals(confirm) || "yes".equals(confirm)) {
                stayService.checkOutGuest(stayId);
                System.out.println("\n✓ Check-out completed successfully!");
                System.out.println("  Room has been set to 'Under Cleaning'");
            } else {
                System.out.println("Check-out cancelled.");
            }
        } catch (NumberFormatException e) {
            System.out.println("✗ Error: Invalid ID format");
        } catch (Exception e) {
            System.out.println("✗ Error: " + e.getMessage());
        }
    }

    // ============ Query Operations ============

    private void viewActiveStays() {
        System.out.println("\n=== ACTIVE STAYS ===");
        List<Stay> stays = stayService.getActiveStays();
        if (stays.isEmpty()) {
            System.out.println("No active stays found.");
        } else {
            System.out.println("\n┌────────┬──────────┬─────────────────────┬──────────────┐");
            System.out.println("│ Stay ID│ Room     │ Check-In Time       │ Key Card     │");
            System.out.println("├────────┼──────────┼─────────────────────┼──────────────┤");
            for (Stay s : stays) {
                String roomNum = s.getRoom() != null ? s.getRoom().getRoomNumber() : "N/A";
                String checkIn = s.getCheckInTime() != null
                        ? s.getCheckInTime().toString().replace("T", " ").substring(0, 16)
                        : "N/A";
                String keyCard = s.getKeyCardNumber() != null ? s.getKeyCardNumber() : "N/A";
                System.out.printf("│ %-6d │ %-8s │ %-19s │ %-12s │%n",
                        s.getStayId(), roomNum, checkIn, keyCard);
            }
            System.out.println("└────────┴──────────┴─────────────────────┴──────────────┘");
            System.out.println("Total active stays: " + stays.size());
        }
    }

    private void viewStayByRoom() {
        System.out.println("\n=== FIND STAY BY ROOM NUMBER ===");
        System.out.print("Enter Room Number: ");
        String roomNumber = scanner.nextLine().trim();

        Optional<Stay> stayOpt = stayService.getActiveStayByRoom(roomNumber);
        if (stayOpt.isPresent()) {
            System.out.println("\n✓ Active stay found:");
            printStayDetails(stayOpt.get());
        } else {
            System.out.println("No active stay in room " + roomNumber);
        }
    }

    private void viewGuestHistory() {
        System.out.println("\n=== GUEST STAY HISTORY ===");
        try {
            System.out.print("Enter Guest ID: ");
            Long guestId = Long.parseLong(scanner.nextLine().trim());

            List<Stay> history = stayService.getGuestStayHistory(guestId);
            if (history.isEmpty()) {
                System.out.println("No stay history found for guest " + guestId);
            } else {
                System.out.println("\n┌────────┬──────────┬─────────────────────┬─────────────────────┬────────────┐");
                System.out.println("│ Stay ID│ Room     │ Check-In            │ Check-Out           │ Status     │");
                System.out.println("├────────┼──────────┼─────────────────────┼─────────────────────┼────────────┤");
                for (Stay s : history) {
                    String roomNum = s.getRoom() != null ? s.getRoom().getRoomNumber() : "N/A";
                    String checkIn = formatDateTime(s.getCheckInTime());
                    String checkOut = formatDateTime(s.getCheckOutTime());
                    System.out.printf("│ %-6d │ %-8s │ %-19s │ %-19s │ %-10s │%n",
                            s.getStayId(), roomNum, checkIn, checkOut, s.getStatus());
                }
                System.out.println("└────────┴──────────┴─────────────────────┴─────────────────────┴────────────┘");
                System.out.println("Total stays: " + history.size());
            }
        } catch (NumberFormatException e) {
            System.out.println("✗ Error: Invalid ID format");
        }
    }

    // ============ Helper Methods ============

    private void printStayDetails(Stay stay) {
        System.out.println("\n┌─────────────────────────────────────────┐");
        System.out.println("│            STAY DETAILS                 │");
        System.out.println("├─────────────────────────────────────────┤");
        System.out.printf("│  Stay ID:      %-24d │%n", stay.getStayId());
        System.out.printf("│  Status:       %-24s │%n", stay.getStatus());
        if (stay.getRoom() != null) {
            System.out.printf("│  Room:         %-24s │%n", stay.getRoom().getRoomNumber());
        }
        if (stay.getGuest() != null) {
            System.out.printf("│  Guest:        %-24s │%n", stay.getGuest().getName());
        }
        if (stay.getCheckInTime() != null) {
            System.out.printf("│  Check-In:     %-24s │%n", formatDateTime(stay.getCheckInTime()));
        }
        if (stay.getKeyCardNumber() != null) {
            System.out.printf("│  Key Card:     %-24s │%n", stay.getKeyCardNumber());
        }
        System.out.println("└─────────────────────────────────────────┘");
    }

    private String formatDateTime(java.time.LocalDateTime dt) {
        if (dt == null)
            return "N/A";
        return dt.toString().replace("T", " ").substring(0, 16);
    }

    private String truncate(String str, int length) {
        if (str == null)
            return "";
        return str.length() > length ? str.substring(0, length - 2) + ".." : str;
    }
}
