package com.hotel.smarttrack;

import com.hotel.smarttrack.room.RoomManagementConsole;
import com.hotel.smarttrack.stay.StayManagementConsole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Scanner;

/**
 * MainMenuConsole - Central hub for all module consoles.
 * This is the main terminal UI that orchestrates access to all management
 * modules.
 * 
 * Each team member's console is called from here:
 * - Guest Management (Ma Wenting)
 * - Room Management (Eisraq Rejab)
 * - Reservation Management (Li Yuhang)
 * - Stay Management (Elvis Sawing)
 * - Billing & Payment (Huang Di)
 */
@Component
@Order(100) // Run after all initialization
public class MainMenuConsole implements CommandLineRunner {

    private final RoomManagementConsole roomManagementConsole;
    private final StayManagementConsole stayManagementConsole;
    // Add other consoles here as they are created:
    // private final GuestManagementConsole guestManagementConsole;
    // private final ReservationManagementConsole reservationManagementConsole;
    // private final BillingManagementConsole billingManagementConsole;

    private final Scanner scanner;

    public MainMenuConsole(RoomManagementConsole roomManagementConsole,
                           StayManagementConsole stayManagementConsole
    // Add other consoles to constructor as they are created
    ) {
        this.roomManagementConsole = roomManagementConsole;
        this.stayManagementConsole = stayManagementConsole;
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n");
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                                                              ║");
        System.out.println("║            HOTEL SMARTTRACK MANAGEMENT SYSTEM                ║");
        System.out.println("║                  Component-Based Architecture                ║");
        System.out.println("║                                                              ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();

        boolean running = true;
        while (running) {
            printMainMenu();
            System.out.print("\nEnter your choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> accessGuestManagement();
                case "2" -> roomManagementConsole.showMenu(scanner);
                case "3" -> accessReservationManagement();
                case "4" -> stayManagementConsole.showMenu(scanner);
                case "5" -> accessBillingManagement();
                case "0" -> {
                    System.out.println("\n");
                    System.out.println("╔══════════════════════════════════════════════════════════════╗");
                    System.out.println("║     Thank you for using Hotel SmartTrack. Goodbye!          ║");
                    System.out.println("╚══════════════════════════════════════════════════════════════╝");
                    running = false;
                }
                default -> System.out.println("\n⚠ Invalid choice. Please enter a number from 0-5.");
            }
        }
    }

    private void printMainMenu() {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                      MAIN MENU                               ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║                                                              ║");
        System.out.println("║   1. Guest Management          (Ma Wenting)                  ║");
        System.out.println("║   2. Room Management           (Eisraq Rejab)      ✓ Ready   ║");
        System.out.println("║   3. Reservation Management    (Li Yuhang)                   ║");
        System.out.println("║   4. Stay Management           (Elvis Sawing)      ✓ Ready   ║");
        System.out.println("║   5. Billing & Payment         (Huang Di)                    ║");
        System.out.println("║                                                              ║");
        System.out.println("║   0. Exit System                                             ║");
        System.out.println("║                                                              ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }

    // ============ Module Access Methods ============
    // These will be replaced with actual console calls as modules are completed

    private void accessGuestManagement() {
        System.out.println("\n┌─────────────────────────────────────────────────────────────┐");
        System.out.println("│  GUEST MANAGEMENT                                           │");
        System.out.println("│  Status: Not yet implemented                                │");
        System.out.println("│  Owner: Ma Wenting                                          │");
        System.out.println("│                                                             │");
        System.out.println("│  To enable: Create GuestManagementConsole in                │");
        System.out.println("│             guest-management module with showMenu(Scanner)  │");
        System.out.println("└─────────────────────────────────────────────────────────────┘");
        System.out.println("\nPress Enter to return to main menu...");
        scanner.nextLine();
    }

    private void accessRoomManagement() {
        System.out.println("\n┌─────────────────────────────────────────────────────────────┐");
        System.out.println("│  ROOM MANAGEMENT                                            │");
        System.out.println("│  Status: Not yet implemented                                │");
        System.out.println("│  Owner: Eisraq Rejab                                        │");
        System.out.println("│                                                             │");
        System.out.println("│  To enable: Create RoomManagementConsole in                 │");
        System.out.println("│             room-management module with showMenu(Scanner)   │");
        System.out.println("└─────────────────────────────────────────────────────────────┘");
        System.out.println("\nPress Enter to return to main menu...");
        scanner.nextLine();
    }

    private void accessReservationManagement() {
        System.out.println("\n┌─────────────────────────────────────────────────────────────┐");
        System.out.println("│  RESERVATION MANAGEMENT                                     │");
        System.out.println("│  Status: Not yet implemented                                │");
        System.out.println("│  Owner: Li Yuhang                                           │");
        System.out.println("│                                                             │");
        System.out.println("│  To enable: Create ReservationManagementConsole in          │");
        System.out.println("│             reservation-management module                   │");
        System.out.println("└─────────────────────────────────────────────────────────────┘");
        System.out.println("\nPress Enter to return to main menu...");
        scanner.nextLine();
    }

    private void accessBillingManagement() {
        System.out.println("\n┌─────────────────────────────────────────────────────────────┐");
        System.out.println("│  BILLING & PAYMENT                                          │");
        System.out.println("│  Status: Not yet implemented                                │");
        System.out.println("│  Owner: Huang Di                                            │");
        System.out.println("│                                                             │");
        System.out.println("│  To enable: Create BillingManagementConsole in              │");
        System.out.println("│             billing-payment module with showMenu(Scanner)   │");
        System.out.println("└─────────────────────────────────────────────────────────────┘");
        System.out.println("\nPress Enter to return to main menu...");
        scanner.nextLine();
    }
}
