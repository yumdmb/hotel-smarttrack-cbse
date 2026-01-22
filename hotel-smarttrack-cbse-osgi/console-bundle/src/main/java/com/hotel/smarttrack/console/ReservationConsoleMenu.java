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

/**
 * Reservation Management Console Menu.
 * Provides reservation CRUD and management operations via terminal UI.
 */
public class ReservationConsoleMenu {

    private final ReservationService reservationService;
    private final GuestService guestService;
    private final RoomService roomService;
    private final ConsoleInputHelper input;

    public ReservationConsoleMenu(ReservationService reservationService, 
                                   GuestService guestService, 
                                   RoomService roomService,
                                   ConsoleInputHelper input) {
        this.reservationService = reservationService;
        this.guestService = guestService;
        this.roomService = roomService;
        this.input = input;
    }

    public void showMenu() {
        boolean running = true;
        while (running) {
            input.println("\n==============================");
            input.println("   RESERVATION MANAGEMENT     ");
            input.println("==============================");
            input.println("1. View All Reservations");
            input.println("2. View Reservation by ID");
            input.println("3. Create Reservation");
            input.println("4. Confirm Reservation");
            input.println("5. Cancel Reservation");
            input.println("6. Assign Room to Reservation");
            input.println("7. View Guest Reservations");
            input.println("8. Search Available Rooms");
            input.println("0. Back to Main Menu");

            String choice = input.readLine("Choose: ");
            try {
                switch (choice) {
                    case "1" -> viewAllReservations();
                    case "2" -> viewReservationById();
                    case "3" -> createReservation();
                    case "4" -> confirmReservation();
                    case "5" -> cancelReservation();
                    case "6" -> assignRoom();
                    case "7" -> viewGuestReservations();
                    case "8" -> searchAvailableRooms();
                    case "0" -> running = false;
                    default -> input.println("Invalid option.");
                }
            } catch (IllegalArgumentException | IllegalStateException ex) {
                input.println("[ERROR] Error: " + ex.getMessage());
            } catch (Exception ex) {
                input.println("[ERROR] Unexpected error: " + ex.getMessage());
            }
        }
    }

    private void viewAllReservations() {
        List<Reservation> reservations = reservationService.getAllReservations();
        if (reservations.isEmpty()) {
            input.println("No reservations found.");
            return;
        }
        input.println("\n--- All Reservations ---");
        reservations.forEach(this::printReservation);
    }

    private void viewReservationById() {
        Long id = input.readLong("Reservation ID: ");
        Optional<Reservation> res = reservationService.getReservationById(id);
        res.ifPresentOrElse(
            this::printReservationDetails,
            () -> input.println("Reservation not found.")
        );
    }

    private void createReservation() {
        // Show available guests
        input.println("\n--- Available Guests ---");
        guestService.getAllGuests().forEach(g -> 
            input.println(String.format("ID=%d | %s | %s", g.getGuestId(), g.getName(), g.getEmail())));
        
        Long guestId = input.readLong("Guest ID: ");
        
        // Show room types
        input.println("\n--- Room Types ---");
        roomService.getAllRoomTypes().forEach(t ->
            input.println(String.format("ID=%d | %s | $%s/night", 
                t.getRoomTypeId(), t.getTypeName(), t.getBasePrice())));
        
        Long roomTypeId = input.readLong("Room Type ID: ");
        LocalDate checkIn = readDate("Check-in Date (YYYY-MM-DD): ");
        LocalDate checkOut = readDate("Check-out Date (YYYY-MM-DD): ");
        int numGuests = input.readInt("Number of Guests: ");
        String specialRequests = input.readLine("Special Requests (or press Enter to skip): ");

        Reservation reservation = reservationService.createReservation(
            guestId, roomTypeId, checkIn, checkOut, numGuests, specialRequests);
        input.println("✅ Reservation created!");
        printReservationDetails(reservation);
    }

    private void confirmReservation() {
        Long id = input.readLong("Reservation ID to confirm: ");
        reservationService.confirmReservation(id);
        input.println("✅ Reservation confirmed.");
    }

    private void cancelReservation() {
        Long id = input.readLong("Reservation ID to cancel: ");
        reservationService.cancelReservation(id);
        input.println("✅ Reservation cancelled.");
    }

    private void assignRoom() {
        Long reservationId = input.readLong("Reservation ID: ");
        
        // Get reservation to show date range
        Optional<Reservation> resOpt = reservationService.getReservationById(reservationId);
        if (resOpt.isEmpty()) {
            input.println("Reservation not found.");
            return;
        }
        Reservation res = resOpt.get();
        
        // Show available rooms for reservation dates
        input.println("\n--- Available Rooms for " + res.getCheckInDate() + " to " + res.getCheckOutDate() + " ---");
        List<Room> availableRooms = roomService.getAvailableRooms(res.getCheckInDate(), res.getCheckOutDate());
        availableRooms.forEach(r ->
            input.println(String.format("ID=%d | Room %s | %s",
                r.getRoomId(), r.getRoomNumber(), 
                r.getRoomType() != null ? r.getRoomType().getTypeName() : "N/A")));
        
        if (availableRooms.isEmpty()) {
            input.println("No available rooms for these dates.");
            return;
        }
        
        Long roomId = input.readLong("Room ID to assign: ");
        
        reservationService.assignRoom(reservationId, roomId);
        input.println("✅ Room assigned to reservation.");
    }

    private void viewGuestReservations() {
        Long guestId = input.readLong("Guest ID: ");
        List<Reservation> reservations = reservationService.getReservationsByGuest(guestId);
        if (reservations.isEmpty()) {
            input.println("No reservations for this guest.");
            return;
        }
        input.println("\n--- Guest Reservations ---");
        reservations.forEach(this::printReservation);
    }

    private void searchAvailableRooms() {
        LocalDate checkIn = readDate("Check-in Date (YYYY-MM-DD): ");
        LocalDate checkOut = readDate("Check-out Date (YYYY-MM-DD): ");
        
        input.println("Room Type ID (or 0 for any): ");
        Long roomTypeId = input.readLong("");
        if (roomTypeId == 0) roomTypeId = null;
        
        int occupancy = input.readInt("Required Occupancy: ");
        
        List<Long> roomIds = reservationService.searchAvailableRooms(checkIn, checkOut, roomTypeId, occupancy);
        
        if (roomIds.isEmpty()) {
            input.println("No available rooms matching criteria.");
            return;
        }
        
        input.println("\n--- Available Room IDs ---");
        roomIds.forEach(id -> {
            roomService.getRoomById(id).ifPresent(r -> 
                input.println(String.format("ID=%d | Room %s | %s",
                    r.getRoomId(), r.getRoomNumber(),
                    r.getRoomType() != null ? r.getRoomType().getTypeName() : "N/A")));
        });
    }

    private void printReservation(Reservation res) {
        input.println(String.format("ID=%d | Guest: %s | %s to %s | Status: %s | Room: %s",
            res.getReservationId(),
            res.getGuest() != null ? res.getGuest().getName() : "N/A",
            res.getCheckInDate(),
            res.getCheckOutDate(),
            res.getStatus(),
            res.getAssignedRoom() != null ? res.getAssignedRoom().getRoomNumber() : "Not assigned"));
    }

    private void printReservationDetails(Reservation res) {
        input.println("\n--- Reservation Details ---");
        input.println("Reservation ID: " + res.getReservationId());
        input.println("Guest:          " + (res.getGuest() != null ? res.getGuest().getName() : "N/A"));
        input.println("Room Type:      " + (res.getRoomType() != null ? res.getRoomType().getTypeName() : "N/A"));
        input.println("Assigned Room:  " + (res.getAssignedRoom() != null ? res.getAssignedRoom().getRoomNumber() : "Not assigned"));
        input.println("Check-in:       " + res.getCheckInDate());
        input.println("Check-out:      " + res.getCheckOutDate());
        input.println("Guests:         " + res.getNumberOfGuests());
        input.println("Status:         " + res.getStatus());
    }

    // ============ Utility Methods ============

    private LocalDate readDate(String prompt) {
        while (true) {
            String s = input.readLine(prompt);
            try {
                return LocalDate.parse(s);
            } catch (DateTimeParseException e) {
                input.println("Please enter date in YYYY-MM-DD format.");
            }
        }
    }
}
