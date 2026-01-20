package com.hotel.smarttrack.reservation.console;

import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
public class ReservationConsole {

    public void showMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n┌──────────────────────────────────────────────┐");
            System.out.println("│              RESERVATION MANAGEMENT          │");
            System.out.println("├──────────────────────────────────────────────┤");
            System.out.println("│  1. List reservations (demo)                 │");
            System.out.println("│  2. Create reservation (demo)                │");
            System.out.println("│  3. Cancel reservation (demo)                │");
            System.out.println("│                                              │");
            System.out.println("│  0. Back to Main Menu                        │");
            System.out.println("└──────────────────────────────────────────────┘");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> System.out.println("Demo: list reservations (not implemented yet).");
                case "2" -> System.out.println("Demo: create reservation (not implemented yet).");
                case "3" -> System.out.println("Demo: cancel reservation (not implemented yet).");
                case "0" -> {
                    return;
                }
                default -> System.out.println("⚠ Invalid choice. Please enter 0-3.");
            }
        }
    }
}
