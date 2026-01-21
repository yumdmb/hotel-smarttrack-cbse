package com.hotel.smarttrack.console;

import com.hotel.smarttrack.service.*;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.api.console.SessionFactory;
import org.osgi.service.component.annotations.*;

/**
 * HotelConsoleCommand - Main console entry point for Hotel SmartTrack OSGi.
 * 
 * This component provides an interactive terminal UI using OSGi Declarative Services
 * with Karaf's Session for proper terminal input handling.
 * 
 * Usage: In Karaf, run "hotel:console" to launch the menu.
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

    // ============ Karaf Session Reference ============
    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    private volatile SessionFactory sessionFactory;

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
     * Called via: hotel:console
     * 
     * @param session The Karaf console session (automatically injected by Gogo)
     */
    public void console(Session session) {
        // Create input helper using Karaf's session
        ConsoleInputHelper input = new ConsoleInputHelper(session);

        // Initialize menus with injected services  
        guestConsoleMenu = new GuestConsoleMenu(guestService, input);
        roomConsoleMenu = new RoomConsoleMenu(roomService, input);
        reservationConsoleMenu = new ReservationConsoleMenu(reservationService, guestService, roomService, input);
        stayConsoleMenu = new StayConsoleMenu(stayService, guestService, roomService, reservationService, input);
        billingConsoleMenu = new BillingConsoleMenu(billingService, stayService, input);

        printWelcomeBanner(input);

        boolean running = true;
        while (running) {
            printMainMenu(input);
            String choice = input.readLine("\nEnter your choice: ");

            try {
                switch (choice) {
                    case "1" -> guestConsoleMenu.showMenu();
                    case "2" -> roomConsoleMenu.showMenu();
                    case "3" -> reservationConsoleMenu.showMenu();
                    case "4" -> stayConsoleMenu.showMenu();
                    case "5" -> billingConsoleMenu.showMenu();
                    case "0" -> {
                        printGoodbyeBanner(input);
                        running = false;
                    }
                    default -> input.println("\n⚠ Invalid choice. Please enter a number from 0-5.");
                }
            } catch (Exception e) {
                input.println("\n❌ Error: " + e.getMessage());
            }
        }
    }

    /**
     * Fallback method without session parameter (for older Gogo versions).
     */
    public void console() {
        // Fallback: use null session (ConsoleInputHelper will use System.in/out)
        ConsoleInputHelper input = new ConsoleInputHelper(null);

        // Initialize menus with injected services  
        guestConsoleMenu = new GuestConsoleMenu(guestService, input);
        roomConsoleMenu = new RoomConsoleMenu(roomService, input);
        reservationConsoleMenu = new ReservationConsoleMenu(reservationService, guestService, roomService, input);
        stayConsoleMenu = new StayConsoleMenu(stayService, guestService, roomService, reservationService, input);
        billingConsoleMenu = new BillingConsoleMenu(billingService, stayService, input);

        printWelcomeBanner(input);

        boolean running = true;
        while (running) {
            printMainMenu(input);
            String choice = input.readLine("\nEnter your choice: ");

            try {
                switch (choice) {
                    case "1" -> guestConsoleMenu.showMenu();
                    case "2" -> roomConsoleMenu.showMenu();
                    case "3" -> reservationConsoleMenu.showMenu();
                    case "4" -> stayConsoleMenu.showMenu();
                    case "5" -> billingConsoleMenu.showMenu();
                    case "0" -> {
                        printGoodbyeBanner(input);
                        running = false;
                    }
                    default -> input.println("\n⚠ Invalid choice. Please enter a number from 0-5.");
                }
            } catch (Exception e) {
                input.println("\n❌ Error: " + e.getMessage());
            }
        }
    }

    // ============ UI Helpers ============

    private void printWelcomeBanner(ConsoleInputHelper input) {
        input.println("\n");
        input.println("╔══════════════════════════════════════════════════════════════╗");
        input.println("║            HOTEL SMARTTRACK MANAGEMENT SYSTEM                ║");
        input.println("║               OSGi Component-Based Architecture              ║");
        input.println("╚══════════════════════════════════════════════════════════════╝");
    }

    private void printMainMenu(ConsoleInputHelper input) {
        input.println("\n╔══════════════════════════════════════════════════════════════╗");
        input.println("║                      MAIN MENU                               ║");
        input.println("╠══════════════════════════════════════════════════════════════╣");
        input.println("║                                                              ║");
        input.println("║   1. Guest Management          (Ma Wenting)                  ║");
        input.println("║   2. Room Management           (Eisraq Rejab)                ║");
        input.println("║   3. Reservation Management    (Li Yuhang)                   ║");
        input.println("║   4. Stay Management           (Elvis Sawing)                ║");
        input.println("║   5. Billing & Payment         (Huang Di)                    ║");
        input.println("║                                                              ║");
        input.println("║   0. Exit Console                                            ║");
        input.println("╚══════════════════════════════════════════════════════════════╝");
    }

    private void printGoodbyeBanner(ConsoleInputHelper input) {
        input.println("\n");
        input.println("╔══════════════════════════════════════════════════════════════╗");
        input.println("║     Thank you for using Hotel SmartTrack. Goodbye!           ║");
        input.println("╚══════════════════════════════════════════════════════════════╝");
    }
}
