package com.hotel.smarttrack.console;

import com.hotel.smarttrack.entity.Reservation;
import com.hotel.smarttrack.entity.Room;
import com.hotel.smarttrack.service.GuestService;
import com.hotel.smarttrack.service.ReservationService;
import com.hotel.smarttrack.service.RoomService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Reservation Management Console Menu.
 * Provides reservation CRUD and management operations via terminal UI.
 */
public class ReservationConsoleMenu {

    private final ReservationService reservationService;
    private final GuestService guestService;
    private final RoomService roomService;

    public ReservationConsoleMenu(ReservationService reservationService, 
                                   GuestService guestService, 
                                   RoomService roomService) {
        this.reservationService = reservationService;
        this.guestService = guestService;
        this.roomService = roomService;
    }

    public void showMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("\n==============================");
            System.out.println("   RESERVATION MANAGEMENT     ");
            System.out.println("==============================");
            System.out.println("1. View All Reservations");
            System.out.println("2. View Reservation by ID");
            System.out.println("3. Create Reservation");
            System.out.println("4. Confirm Reservation");
            System.out.println("5. Cancel Reservation");
            System.out.println("6. Assign Room to Reservation");
            System.out.println("7. View Guest Reservations");
            System.out.println("8. Search Available Rooms");
            System.out.println("0. Back to Main Menu");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> viewAllReservations();
                    case "2" -> viewReservationById(scanner);
                    case "3" -> createReservation(scanner);
                    case "4" -> confirmReservation(scanner);
                    case "5" -> cancelReservation(scanner);
                    case "6" -> assignRoom(scanner);
                    case "7" -> viewGuestReservations(scanner);
                    case "8" -> searchAvailableRooms(scanner);
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

    private void viewAllReservations() {
        List<Reservation> reservations = reservationService.getAllReservations();
        if (reservations.isEmpty()) {
            System.out.println("No reservations found.");
            return;
        }
        System.out.println("\n--- All Reservations ---");
        reservations.forEach(this::printReservation);
    }

    private void viewReservationById(Scanner scanner) {
        Long id = readLong(scanner, "Reservation ID: ");
        Optional<Reservation> res = reservationService.getReservationById(id);
        res.ifPresentOrElse(
            this::printReservationDetails,
            () -> System.out.println("Reservation not found.")
        );
    }

    private void createReservation(Scanner scanner) {
        // Show available guests
        System.out.println("\n--- Available Guests ---");
        guestService.getAllGuests().forEach(g -> 
            System.out.printf("ID=%d | %s | %s%n", g.getGuestId(), g.getName(), g.getEmail()));
        
        Long guestId = readLong(scanner, "Guest ID: ");
        
        // Show room types
        System.out.println("\n--- Room Types ---");
        roomService.getAllRoomTypes().forEach(t ->
            System.out.printf("ID=%d | %s | $%s/night%n", 
                t.getRoomTypeId(), t.getTypeName(), t.getBasePrice()));
        
        Long roomTypeId = readLong(scanner, "Room Type ID: ");
        LocalDate checkIn = readDate(scanner, "Check-in Date (YYYY-MM-DD): ");
        LocalDate checkOut = readDate(scanner, "Check-out Date (YYYY-MM-DD): ");
        int numGuests = readInt(scanner, "Number of Guests: ");
        System.out.print("Special Requests (or press Enter to skip): ");
        String specialRequests = scanner.nextLine();

        // OSGi API: createReservation(guestId, roomTypeId, checkIn, checkOut, numberOfGuests, specialRequests)
        Reservation reservation = reservationService.createReservation(
            guestId, roomTypeId, checkIn, checkOut, numGuests, specialRequests);
        System.out.println("✅ Reservation created!");
        printReservationDetails(reservation);
    }

    private void confirmReservation(Scanner scanner) {
        Long id = readLong(scanner, "Reservation ID to confirm: ");
        reservationService.confirmReservation(id);
        System.out.println("✅ Reservation confirmed.");
    }

    private void cancelReservation(Scanner scanner) {
        Long id = readLong(scanner, "Reservation ID to cancel: ");
        // OSGi API: cancelReservation(id) - no reason parameter
        reservationService.cancelReservation(id);
        System.out.println("✅ Reservation cancelled.");
    }

    private void assignRoom(Scanner scanner) {
        Long reservationId = readLong(scanner, "Reservation ID: ");
        
        // Get reservation to show date range
        Optional<Reservation> resOpt = reservationService.getReservationById(reservationId);
        if (resOpt.isEmpty()) {
            System.out.println("Reservation not found.");
            return;
        }
        Reservation res = resOpt.get();
        
        // Show available rooms for reservation dates
        System.out.println("\n--- Available Rooms for " + res.getCheckInDate() + " to " + res.getCheckOutDate() + " ---");
        List<Room> availableRooms = roomService.getAvailableRooms(res.getCheckInDate(), res.getCheckOutDate());
        availableRooms.forEach(r ->
            System.out.printf("ID=%d | Room %s | %s%n",
                r.getRoomId(), r.getRoomNumber(), 
                r.getRoomType() != null ? r.getRoomType().getTypeName() : "N/A"));
        
        if (availableRooms.isEmpty()) {
            System.out.println("No available rooms for these dates.");
            return;
        }
        
        Long roomId = readLong(scanner, "Room ID to assign: ");
        
        reservationService.assignRoom(reservationId, roomId);
        System.out.println("✅ Room assigned to reservation.");
    }

    private void viewGuestReservations(Scanner scanner) {
        Long guestId = readLong(scanner, "Guest ID: ");
        List<Reservation> reservations = reservationService.getReservationsByGuest(guestId);
        if (reservations.isEmpty()) {
            System.out.println("No reservations for this guest.");
            return;
        }
        System.out.println("\n--- Guest Reservations ---");
        reservations.forEach(this::printReservation);
    }

    private void searchAvailableRooms(Scanner scanner) {
        LocalDate checkIn = readDate(scanner, "Check-in Date (YYYY-MM-DD): ");
        LocalDate checkOut = readDate(scanner, "Check-out Date (YYYY-MM-DD): ");
        
        System.out.print("Room Type ID (or 0 for any): ");
        Long roomTypeId = readLong(scanner, "");
        if (roomTypeId == 0) roomTypeId = null;
        
        int occupancy = readInt(scanner, "Required Occupancy: ");
        
        // OSGi API: searchAvailableRooms(checkIn, checkOut, roomTypeId, occupancy)
        List<Long> roomIds = reservationService.searchAvailableRooms(checkIn, checkOut, roomTypeId, occupancy);
        
        if (roomIds.isEmpty()) {
            System.out.println("No available rooms matching criteria.");
            return;
        }
        
        System.out.println("\n--- Available Room IDs ---");
        roomIds.forEach(id -> {
            roomService.getRoomById(id).ifPresent(r -> 
                System.out.printf("ID=%d | Room %s | %s%n",
                    r.getRoomId(), r.getRoomNumber(),
                    r.getRoomType() != null ? r.getRoomType().getTypeName() : "N/A"));
        });
    }

    private void printReservation(Reservation res) {
        System.out.printf("ID=%d | Guest: %s | %s to %s | Status: %s | Room: %s%n",
            res.getReservationId(),
            res.getGuest() != null ? res.getGuest().getName() : "N/A",
            res.getCheckInDate(),
            res.getCheckOutDate(),
            res.getStatus(),
            res.getAssignedRoom() != null ? res.getAssignedRoom().getRoomNumber() : "Not assigned");
    }

    private void printReservationDetails(Reservation res) {
        System.out.println("\n--- Reservation Details ---");
        System.out.println("Reservation ID: " + res.getReservationId());
        System.out.println("Guest:          " + (res.getGuest() != null ? res.getGuest().getName() : "N/A"));
        System.out.println("Room Type:      " + (res.getRoomType() != null ? res.getRoomType().getTypeName() : "N/A"));
        System.out.println("Assigned Room:  " + (res.getAssignedRoom() != null ? res.getAssignedRoom().getRoomNumber() : "Not assigned"));
        System.out.println("Check-in:       " + res.getCheckInDate());
        System.out.println("Check-out:      " + res.getCheckOutDate());
        System.out.println("Guests:         " + res.getNumberOfGuests());
        System.out.println("Status:         " + res.getStatus());
    }

    // ============ Utility Methods ============

    private Long readLong(Scanner scanner, String prompt) {
        while (true) {
            if (!prompt.isEmpty()) System.out.print(prompt);
            String s = scanner.nextLine().trim();
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = scanner.nextLine().trim();
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private LocalDate readDate(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = scanner.nextLine().trim();
            try {
                return LocalDate.parse(s);
            } catch (DateTimeParseException e) {
                System.out.println("Please enter date in YYYY-MM-DD format.");
            }
        }
    }
}
