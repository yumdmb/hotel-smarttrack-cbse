package com.hotel.smarttrack.reservation.console;

import com.hotel.smarttrack.reservation.ReservationService;
import com.hotel.smarttrack.reservation.dto.ReservationSummary;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

@Component
public class ReservationConsole {

    private final ReservationService reservationService;

    public ReservationConsole(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    public void showMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n┌──────────────────────────────────────────────┐");
            System.out.println("│              RESERVATION MANAGEMENT          │");
            System.out.println("├──────────────────────────────────────────────┤");
            System.out.println("│  1. List reservations                        │");
            System.out.println("│  2. Create reservation                       │");
            System.out.println("│  3. Cancel reservation                       │");
            System.out.println("│                                              │");
            System.out.println("│  0. Back to Main Menu                        │");
            System.out.println("└──────────────────────────────────────────────┘");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> listReservations();
                case "2" -> createReservation(scanner);
                case "3" -> cancelReservation(scanner);
                case "0" -> { return; }
                default -> System.out.println("⚠ Invalid choice. Please enter 0-3.");
            }
        }
    }

    private void listReservations() {
        List<ReservationSummary> list = reservationService.listAll();
        System.out.println("\n--- Reservations ---");
        if (list.isEmpty()) {
            System.out.println("(none)");
            return;
        }
        for (ReservationSummary r : list) {
            System.out.printf("ID=%d | Guest=%d | Room=%d | %s -> %s | Status=%s%n",
                    r.getReservationId(), r.getGuestId(), r.getRoomId(),
                    r.getCheckIn(), r.getCheckOut(), r.getStatus());
        }
    }

    private void createReservation(Scanner scanner) {
        try {
            System.out.print("Guest ID: ");
            Long guestId = Long.parseLong(scanner.nextLine().trim());

            System.out.print("Room ID: ");
            Long roomId = Long.parseLong(scanner.nextLine().trim());

            System.out.print("Check-in (YYYY-MM-DD): ");
            LocalDate checkIn = LocalDate.parse(scanner.nextLine().trim());

            System.out.print("Check-out (YYYY-MM-DD): ");
            LocalDate checkOut = LocalDate.parse(scanner.nextLine().trim());

            ReservationSummary created = reservationService.create(guestId, roomId, checkIn, checkOut);
            System.out.println("✅ Created: ID=" + created.getReservationId() + " Status=" + created.getStatus());
        } catch (Exception e) {
            System.out.println("⚠ Invalid input: " + e.getMessage());
        }
    }

    private void cancelReservation(Scanner scanner) {
        try {
            System.out.print("Reservation ID: ");
            Long id = Long.parseLong(scanner.nextLine().trim());

            boolean ok = reservationService.cancel(id);
            System.out.println(ok ? "✅ Cancelled." : "⚠ Not found.");
        } catch (Exception e) {
            System.out.println("⚠ Invalid input: " + e.getMessage());
        }
    }
}
