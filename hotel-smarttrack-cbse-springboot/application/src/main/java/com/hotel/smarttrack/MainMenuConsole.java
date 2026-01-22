package com.hotel.smarttrack;

import com.hotel.smarttrack.billing.console.BillingConsole;
import com.hotel.smarttrack.guest.console.GuestManagementConsole;
import com.hotel.smarttrack.reservation.console.ReservationConsole;
import com.hotel.smarttrack.room.console.RoomManagementConsole;
import com.hotel.smarttrack.stay.console.StayManagementConsole;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
@Order(100)
public class MainMenuConsole implements CommandLineRunner {

    private final BillingConsole billingConsole;
    private final GuestManagementConsole guestManagementConsole;
    private final ReservationConsole reservationConsole;
    private final RoomManagementConsole roomManagementConsole;
    private final StayManagementConsole stayManagementConsole;
    private final Scanner scanner;

    public MainMenuConsole(
            BillingConsole billingConsole,
            GuestManagementConsole guestManagementConsole,
            ReservationConsole reservationConsole,
            RoomManagementConsole roomManagementConsole,
            StayManagementConsole stayManagementConsole) {
        this.billingConsole = billingConsole;
        this.guestManagementConsole = guestManagementConsole;
        this.reservationConsole = reservationConsole;
        this.roomManagementConsole = roomManagementConsole;
        this.stayManagementConsole = stayManagementConsole;
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void run(String... args) {
        System.out.println("\n");
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║            HOTEL SMARTTRACK MANAGEMENT SYSTEM                ║");
        System.out.println("║                  Component-Based Architecture                ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        boolean running = true;
        while (running) {
            printMainMenu();
            System.out.print("\nEnter your choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> guestManagementConsole.start(); // ✅ Guest Management
                case "2" -> roomManagementConsole.showMenu(scanner); // ✅ Room Management
                case "3" -> reservationConsole.showMenu(scanner); // ✅ Reservation
                case "4" -> stayManagementConsole.showMenu(scanner); // ✅ Stay Management
                case "5" -> billingConsole.showMenu(scanner); // ✅ Billing & Payment
                case "0" -> {
                    System.out.println("\n");
                    System.out.println("╔══════════════════════════════════════════════════════════════╗");
                    System.out.println("║     Thank you for using Hotel SmartTrack. Goodbye!           ║");
                    System.out.println("╚══════════════════════════════════════════════════════════════╝");
                    running = false;
                }
                default -> System.out.println("\n⚠ Invalid choice. Please enter a number from 0-5.");
            }
        }

        System.out.println("Bye!");
    }

    private void printMainMenu() {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                      MAIN MENU                               ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║                                                              ║");
        System.out.println("║   1. Guest Management          (Ma Wenting)        ✓ Ready   ║");
        System.out.println("║   2. Room Management           (Eisraq Rejab)      ✓ Ready   ║");
        System.out.println("║   3. Reservation Management    (Li Yuhang)         ✓ Ready   ║");
        System.out.println("║   4. Stay Management           (Elvis Sawing)      ✓ Ready   ║");
        System.out.println("║   5. Billing & Payment         (Huang Di)          ✓ Ready   ║");
        System.out.println("║                                                              ║");
        System.out.println("║   0. Exit System                                             ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }
}
