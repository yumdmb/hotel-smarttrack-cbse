package com.hotel.smarttrack.console;

import com.hotel.smarttrack.service.*;
import org.osgi.service.component.annotations.*;

import java.util.Scanner;

/**
 * HotelConsoleCommand - Main console entry point for Hotel SmartTrack OSGi.
 * 
 * This component provides an interactive terminal UI similar to the Spring Boot
 * MainMenuConsole, but using OSGi Declarative Services.
 * 
 * Usage: After starting in Felix, run "hotel:console" to launch the menu.
 */
@Component(
    service = HotelConsoleCommand.class,
    immediate = true,
    property = {
        "osgi.command.scope=hotel",
        "osgi.command.function=console"
    }
)
public class HotelConsoleCommand {

    // ============ OSGi Service References ============

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private volatile GuestService guestService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private volatile RoomService roomService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private volatile ReservationService reservationService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private volatile StayService stayService;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    private volatile BillingService billingService;

    // Console menus (lazy initialized)
    private GuestConsoleMenu guestConsoleMenu;
    private RoomConsoleMenu roomConsoleMenu;
    private ReservationConsoleMenu reservationConsoleMenu;
    private StayConsoleMenu stayConsoleMenu;
    private BillingConsoleMenu billingConsoleMenu;

    // ============ Lifecycle Methods ============

    @Activate
    public void activate() {
        System.out.println("[HotelConsoleCommand] Console bundle ACTIVATED");
        System.out.println("  Type 'hotel:console' to start the interactive menu");
    }

    @Deactivate
    public void deactivate() {
        System.out.println("[HotelConsoleCommand] Console bundle DEACTIVATED");
    }

    // ============ Gogo Shell Command ============

    /**
     * Starts the interactive hotel management console.
     * Called via: g! hotel:console
     */
    public void console() {
        // Initialize menus with injected services
        guestConsoleMenu = new GuestConsoleMenu(guestService);
        roomConsoleMenu = new RoomConsoleMenu(roomService);
        reservationConsoleMenu = new ReservationConsoleMenu(reservationService, guestService, roomService);
        stayConsoleMenu = new StayConsoleMenu(stayService, guestService, roomService, reservationService);
        billingConsoleMenu = new BillingConsoleMenu(billingService, stayService);

        Scanner scanner = new Scanner(System.in);

        printWelcomeBanner();

        boolean running = true;
        while (running) {
            printMainMenu();
            System.out.print("\nEnter your choice: ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> guestConsoleMenu.showMenu(scanner);
                    case "2" -> roomConsoleMenu.showMenu(scanner);
                    case "3" -> reservationConsoleMenu.showMenu(scanner);
                    case "4" -> stayConsoleMenu.showMenu(scanner);
                    case "5" -> billingConsoleMenu.showMenu(scanner);
                    case "0" -> {
                        printGoodbyeBanner();
                        running = false;
                    }
                    default -> System.out.println("\n⚠ Invalid choice. Please enter a number from 0-5.");
                }
            } catch (Exception e) {
                System.out.println("\n❌ Error: " + e.getMessage());
            }
        }
    }

    // ============ UI Helpers ============

    private void printWelcomeBanner() {
        System.out.println("\n");
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║            HOTEL SMARTTRACK MANAGEMENT SYSTEM                ║");
        System.out.println("║               OSGi Component-Based Architecture              ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }

    private void printMainMenu() {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                      MAIN MENU                               ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║                                                              ║");
        System.out.println("║   1. Guest Management          (Ma Wenting)                  ║");
        System.out.println("║   2. Room Management           (Eisraq Rejab)                ║");
        System.out.println("║   3. Reservation Management    (Li Yuhang)                   ║");
        System.out.println("║   4. Stay Management           (Elvis Sawing)                ║");
        System.out.println("║   5. Billing & Payment         (Huang Di)                    ║");
        System.out.println("║                                                              ║");
        System.out.println("║   0. Exit Console                                            ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }

    private void printGoodbyeBanner() {
        System.out.println("\n");
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║     Thank you for using Hotel SmartTrack. Goodbye!           ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }
}
