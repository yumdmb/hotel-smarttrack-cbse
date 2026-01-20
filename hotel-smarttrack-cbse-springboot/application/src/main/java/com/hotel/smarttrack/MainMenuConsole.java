package com.hotel.smarttrack;

import com.hotel.smarttrack.reservation.console.ReservationConsole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
@Order(100)
public class MainMenuConsole implements CommandLineRunner {

    private final ReservationConsole reservationConsole;
    private final Scanner scanner;

    public MainMenuConsole(ReservationConsole reservationConsole) {
        this.reservationConsole = reservationConsole;
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
                case "1" -> placeholder("Guest Management (Ma Wenting)");
                case "2" -> placeholder("Room Management (Eisraq Rejab)");
                case "3" -> reservationConsole.showMenu(scanner); // ✅ Reservation
                case "4" -> placeholder("Stay Management (Elvis Sawing)");
                case "5" -> placeholder("Billing & Payment (Huang Di)"); // ✅ 只显示，不集成
                case "0" -> running = false;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void printMainMenu() {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                      MAIN MENU                               ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║   1. Guest Management        (Ma Wenting)                    ║");
        System.out.println("║   2. Room Management         (Eisraq Rejab)                  ║");
        System.out.println("║   3. Reservation Management  (Li Yuhang)   ✓ Ready           ║");
        System.out.println("║   4. Stay Management         (Elvis Sawing)                  ║");
        System.out.println("║   5. Billing & Payment       (Huang Di)                      ║");
        System.out.println("║   0. Exit System                                             ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }

    private void placeholder(String name) {
        System.out.println("\n" + name + " not yet implemented.");
        System.out.println("Press Enter to return...");
        scanner.nextLine();
    }
}