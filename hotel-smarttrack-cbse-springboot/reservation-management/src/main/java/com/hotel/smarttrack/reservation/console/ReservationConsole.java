package com.hotel.smarttrack.reservation.console;

import com.hotel.smarttrack.entity.Reservation;
import com.hotel.smarttrack.service.ReservationService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/**
 * Console UI for Reservation Management operations.
 */
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
            System.out.println("│  1. List all reservations                    │");
            System.out.println("│  2. Create reservation                       │");
            System.out.println("│  3. Cancel reservation                       │");
            System.out.println("│  4. Confirm reservation                      │");
            System.out.println("│  5. View reservation by ID                   │");
            System.out.println("│  6. View reservations by status              │");
            System.out.println("│                                              │");
            System.out.println("│  0. Back to Main Menu                        │");
            System.out.println("└──────────────────────────────────────────────┘");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> listReservations();
                case "2" -> createReservation(scanner);
                case "3" -> cancelReservation(scanner);
                case "4" -> confirmReservation(scanner);
                case "5" -> viewReservationById(scanner);
                case "6" -> viewReservationsByStatus(scanner);
                case "0" -> {
                    return;
                }
                default -> System.out.println("⚠ Invalid choice. Please enter 0-6.");
            }
        }
    }

    private void listReservations() {
        List<Reservation> list = reservationService.getAllReservations();
        System.out.println("\n--- All Reservations ---");
        if (list.isEmpty()) {
            System.out.println("(no reservations found)");
            return;
        }
        for (Reservation r : list) {
            printReservation(r);
        }
    }

    private void createReservation(Scanner scanner) {
        try {
            System.out.print("Guest ID: ");
            Long guestId = Long.parseLong(scanner.nextLine().trim());

            System.out.print("Room Type ID: ");
            Long roomTypeId = Long.parseLong(scanner.nextLine().trim());

            System.out.print("Check-in (YYYY-MM-DD): ");
            LocalDate checkIn = LocalDate.parse(scanner.nextLine().trim());

            System.out.print("Check-out (YYYY-MM-DD): ");
            LocalDate checkOut = LocalDate.parse(scanner.nextLine().trim());

            System.out.print("Number of guests: ");
            int numGuests = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Special requests (or press Enter to skip): ");
            String specialRequests = scanner.nextLine().trim();
            if (specialRequests.isEmpty())
                specialRequests = null;

            Reservation created = reservationService.createReservation(
                    guestId, roomTypeId, checkIn, checkOut, numGuests, specialRequests);

            System.out.println("✅ Created reservation:");
            printReservation(created);
        } catch (Exception e) {
            System.out.println("⚠ Error: " + e.getMessage());
        }
    }

    private void cancelReservation(Scanner scanner) {
        try {
            System.out.print("Reservation ID to cancel: ");
            Long id = Long.parseLong(scanner.nextLine().trim());

            reservationService.cancelReservation(id);
            System.out.println("✅ Reservation cancelled.");
        } catch (Exception e) {
            System.out.println("⚠ Error: " + e.getMessage());
        }
    }

    private void confirmReservation(Scanner scanner) {
        try {
            System.out.print("Reservation ID to confirm: ");
            Long id = Long.parseLong(scanner.nextLine().trim());

            reservationService.confirmReservation(id);
            System.out.println("✅ Reservation confirmed.");
        } catch (Exception e) {
            System.out.println("⚠ Error: " + e.getMessage());
        }
    }

    private void viewReservationById(Scanner scanner) {
        try {
            System.out.print("Reservation ID: ");
            Long id = Long.parseLong(scanner.nextLine().trim());

            reservationService.getReservationById(id).ifPresentOrElse(
                    this::printReservation,
                    () -> System.out.println("⚠ Reservation not found."));
        } catch (Exception e) {
            System.out.println("⚠ Error: " + e.getMessage());
        }
    }

    private void viewReservationsByStatus(Scanner scanner) {
        System.out.println("Available statuses: Reserved, Confirmed, Cancelled, No-Show, Checked-In, Checked-Out");
        System.out.print("Enter status: ");
        String status = scanner.nextLine().trim();

        List<Reservation> list = reservationService.getReservationsByStatus(status);
        System.out.println("\n--- Reservations with status: " + status + " ---");
        if (list.isEmpty()) {
            System.out.println("(none found)");
            return;
        }
        for (Reservation r : list) {
            printReservation(r);
        }
    }

    private void printReservation(Reservation r) {
        String guestInfo = r.getGuest() != null
                ? "Guest=" + r.getGuest().getGuestId() + " (" + r.getGuest().getName() + ")"
                : "Guest=N/A";
        String roomTypeInfo = r.getRoomType() != null ? "RoomType=" + r.getRoomType().getRoomTypeId() : "RoomType=N/A";
        String roomInfo = r.getAssignedRoom() != null ? "Room=" + r.getAssignedRoom().getRoomId() : "Room=Not assigned";

        System.out.printf("ID=%d | %s | %s | %s | %s -> %s | Status=%s%n",
                r.getReservationId(),
                guestInfo,
                roomTypeInfo,
                roomInfo,
                r.getCheckInDate(),
                r.getCheckOutDate(),
                r.getStatus());

        if (r.getSpecialRequests() != null && !r.getSpecialRequests().isEmpty()) {
            System.out.println("   └─ Special requests: " + r.getSpecialRequests());
        }
    }
}
