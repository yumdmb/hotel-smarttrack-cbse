package com.hotel.smarttrack.reservation.console;

import com.hotel.smarttrack.entity.Guest;
import com.hotel.smarttrack.entity.Reservation;
import com.hotel.smarttrack.entity.Room;
import com.hotel.smarttrack.entity.RoomType;
import com.hotel.smarttrack.service.GuestService;
import com.hotel.smarttrack.service.ReservationService;
import com.hotel.smarttrack.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Console UI for Reservation Management operations.
 * Integrated with GuestService and RoomService for validation and lookups.
 */
@Component
public class ReservationConsole {

    private final ReservationService reservationService;
    private final GuestService guestService;
    private final RoomService roomService;

    @Autowired
    public ReservationConsole(ReservationService reservationService,
            @Autowired(required = false) GuestService guestService,
            @Autowired(required = false) RoomService roomService) {
        this.reservationService = reservationService;
        this.guestService = guestService;
        this.roomService = roomService;
    }

    public void showMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n┌──────────────────────────────────────────────┐");
            System.out.println("│              RESERVATION MANAGEMENT          │");
            System.out.println("├──────────────────────────────────────────────┤");
            System.out.println("│  1. List all reservations                    │");
            System.out.println("│  2. Create reservation                       │");
            System.out.println("│  3. Modify reservation                       │");
            System.out.println("│  4. Cancel reservation                       │");
            System.out.println("│  5. Confirm reservation                      │");
            System.out.println("│  6. Mark as No-Show                          │");
            System.out.println("│  7. Assign room to reservation               │");
            System.out.println("├──────────────────────────────────────────────┤");
            System.out.println("│  8. View reservation by ID                   │");
            System.out.println("│  9. View reservations by status              │");
            System.out.println("│ 10. View guest reservation history           │");
            System.out.println("│ 11. Search available rooms                   │");
            System.out.println("├──────────────────────────────────────────────┤");
            System.out.println("│ 12. List all guests                          │");
            System.out.println("│ 13. List all room types                      │");
            System.out.println("│                                              │");
            System.out.println("│  0. Back to Main Menu                        │");
            System.out.println("└──────────────────────────────────────────────┘");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> listReservations();
                case "2" -> createReservation(scanner);
                case "3" -> modifyReservation(scanner);
                case "4" -> cancelReservation(scanner);
                case "5" -> confirmReservation(scanner);
                case "6" -> markNoShow(scanner);
                case "7" -> assignRoom(scanner);
                case "8" -> viewReservationById(scanner);
                case "9" -> viewReservationsByStatus(scanner);
                case "10" -> viewGuestHistory(scanner);
                case "11" -> searchAvailableRooms(scanner);
                case "12" -> listGuests();
                case "13" -> listRoomTypes();
                case "0" -> {
                    return;
                }
                default -> System.out.println("⚠ Invalid choice. Please enter 0-13.");
            }
        }
    }

    // ==================== CRUD Operations ====================

    private void listReservations() {
        List<Reservation> list = reservationService.getAllReservations();
        System.out.println("\n--- All Reservations (" + list.size() + ") ---");
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
            // Show available guests if service is available
            if (guestService != null) {
                System.out.println("\n--- Available Guests ---");
                List<Guest> guests = guestService.getAllGuests();
                if (guests.isEmpty()) {
                    System.out.println("⚠ No guests found. Please create a guest first.");
                    return;
                }
                for (Guest g : guests) {
                    System.out.printf("  ID=%d | %s | %s%n", g.getGuestId(), g.getName(), g.getEmail());
                }
            }
            System.out.print("Guest ID: ");
            Long guestId = Long.parseLong(scanner.nextLine().trim());

            // Show available room types if service is available
            if (roomService != null) {
                System.out.println("\n--- Available Room Types ---");
                List<RoomType> roomTypes = roomService.getAllRoomTypes();
                if (roomTypes.isEmpty()) {
                    System.out.println("⚠ No room types found. Please create room types first.");
                    return;
                }
                for (RoomType rt : roomTypes) {
                    System.out.printf("  ID=%d | %s | Max Occupancy=%d | $%.2f/night%n",
                            rt.getRoomTypeId(), rt.getTypeName(), rt.getMaxOccupancy(), rt.getBasePrice());
                }
            }
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

    private void modifyReservation(Scanner scanner) {
        try {
            System.out.print("Reservation ID to modify: ");
            Long id = Long.parseLong(scanner.nextLine().trim());

            Optional<Reservation> existing = reservationService.getReservationById(id);
            if (existing.isEmpty()) {
                System.out.println("⚠ Reservation not found.");
                return;
            }
            Reservation r = existing.get();
            System.out.println("Current reservation:");
            printReservation(r);

            System.out.println("\nLeave blank to keep current value.");

            System.out.print("New check-in (" + r.getCheckInDate() + "): ");
            String checkInStr = scanner.nextLine().trim();
            LocalDate newCheckIn = checkInStr.isEmpty() ? null : LocalDate.parse(checkInStr);

            System.out.print("New check-out (" + r.getCheckOutDate() + "): ");
            String checkOutStr = scanner.nextLine().trim();
            LocalDate newCheckOut = checkOutStr.isEmpty() ? null : LocalDate.parse(checkOutStr);

            System.out.print("New number of guests (" + r.getNumberOfGuests() + "): ");
            String numGuestsStr = scanner.nextLine().trim();
            int newNumGuests = numGuestsStr.isEmpty() ? 0 : Integer.parseInt(numGuestsStr);

            Reservation modified = reservationService.modifyReservation(id, newCheckIn, newCheckOut, newNumGuests);
            System.out.println("✅ Reservation modified:");
            printReservation(modified);
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

    private void markNoShow(Scanner scanner) {
        try {
            System.out.print("Reservation ID to mark as No-Show: ");
            Long id = Long.parseLong(scanner.nextLine().trim());

            reservationService.markNoShow(id);
            System.out.println("✅ Reservation marked as No-Show.");
        } catch (Exception e) {
            System.out.println("⚠ Error: " + e.getMessage());
        }
    }

    private void assignRoom(Scanner scanner) {
        try {
            System.out.print("Reservation ID: ");
            Long reservationId = Long.parseLong(scanner.nextLine().trim());

            Optional<Reservation> existing = reservationService.getReservationById(reservationId);
            if (existing.isEmpty()) {
                System.out.println("⚠ Reservation not found.");
                return;
            }
            printReservation(existing.get());

            // Show available rooms if service is available
            if (roomService != null) {
                System.out.println("\n--- Available Rooms ---");
                List<Room> rooms = roomService.getAllRooms();
                for (Room room : rooms) {
                    String typeInfo = room.getRoomType() != null ? room.getRoomType().getTypeName() : "N/A";
                    System.out.printf("  ID=%d | Room# %s | Floor %d | Type: %s | Status: %s%n",
                            room.getRoomId(), room.getRoomNumber(), room.getFloorNumber(), typeInfo, room.getStatus());
                }
            }

            System.out.print("Room ID to assign: ");
            Long roomId = Long.parseLong(scanner.nextLine().trim());

            reservationService.assignRoom(reservationId, roomId);
            System.out.println("✅ Room assigned to reservation.");
        } catch (Exception e) {
            System.out.println("⚠ Error: " + e.getMessage());
        }
    }

    // ==================== View Operations ====================

    private void viewReservationById(Scanner scanner) {
        try {
            System.out.print("Reservation ID: ");
            Long id = Long.parseLong(scanner.nextLine().trim());

            reservationService.getReservationById(id).ifPresentOrElse(
                    this::printReservationDetailed,
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
        System.out.println("\n--- Reservations with status: " + status + " (" + list.size() + ") ---");
        if (list.isEmpty()) {
            System.out.println("(none found)");
            return;
        }
        for (Reservation r : list) {
            printReservation(r);
        }
    }

    private void viewGuestHistory(Scanner scanner) {
        try {
            // Show guests if available
            if (guestService != null) {
                System.out.println("\n--- Guests ---");
                List<Guest> guests = guestService.getAllGuests();
                for (Guest g : guests) {
                    System.out.printf("  ID=%d | %s%n", g.getGuestId(), g.getName());
                }
            }
            System.out.print("Guest ID: ");
            Long guestId = Long.parseLong(scanner.nextLine().trim());

            List<Reservation> history = reservationService.getGuestReservationHistory(guestId);
            System.out.println("\n--- Reservation History for Guest " + guestId + " (" + history.size() + ") ---");
            if (history.isEmpty()) {
                System.out.println("(no reservations found)");
                return;
            }
            for (Reservation r : history) {
                printReservation(r);
            }
        } catch (Exception e) {
            System.out.println("⚠ Error: " + e.getMessage());
        }
    }

    private void searchAvailableRooms(Scanner scanner) {
        try {
            if (roomService == null) {
                System.out.println("⚠ RoomService not available.");
                return;
            }

            System.out.print("Check-in (YYYY-MM-DD): ");
            LocalDate checkIn = LocalDate.parse(scanner.nextLine().trim());

            System.out.print("Check-out (YYYY-MM-DD): ");
            LocalDate checkOut = LocalDate.parse(scanner.nextLine().trim());

            // Show room types
            System.out.println("\n--- Room Types ---");
            List<RoomType> roomTypes = roomService.getAllRoomTypes();
            for (RoomType rt : roomTypes) {
                System.out.printf("  ID=%d | %s | $%.2f/night%n",
                        rt.getRoomTypeId(), rt.getTypeName(), rt.getBasePrice());
            }
            System.out.print("Room Type ID (or press Enter for all): ");
            String roomTypeStr = scanner.nextLine().trim();

            List<Room> availableRooms;
            if (roomTypeStr.isEmpty()) {
                availableRooms = roomService.getAvailableRooms(checkIn, checkOut);
            } else {
                Long roomTypeId = Long.parseLong(roomTypeStr);
                availableRooms = roomService.getAvailableRoomsByType(roomTypeId, checkIn, checkOut);
            }

            System.out.println("\n--- Available Rooms (" + availableRooms.size() + ") ---");
            if (availableRooms.isEmpty()) {
                System.out.println("(no available rooms for selected dates)");
                return;
            }
            for (Room room : availableRooms) {
                String typeInfo = room.getRoomType() != null ? room.getRoomType().getTypeName() : "N/A";
                System.out.printf("  ID=%d | Room# %s | Floor %d | Type: %s%n",
                        room.getRoomId(), room.getRoomNumber(), room.getFloorNumber(), typeInfo);
            }
        } catch (Exception e) {
            System.out.println("⚠ Error: " + e.getMessage());
        }
    }

    // ==================== Helper Lists ====================

    private void listGuests() {
        if (guestService == null) {
            System.out.println("⚠ GuestService not available.");
            return;
        }
        List<Guest> guests = guestService.getAllGuests();
        System.out.println("\n--- All Guests (" + guests.size() + ") ---");
        if (guests.isEmpty()) {
            System.out.println("(no guests found)");
            return;
        }
        for (Guest g : guests) {
            System.out.printf("ID=%d | %s | %s | %s%n",
                    g.getGuestId(), g.getName(), g.getEmail(), g.getPhone());
        }
    }

    private void listRoomTypes() {
        if (roomService == null) {
            System.out.println("⚠ RoomService not available.");
            return;
        }
        List<RoomType> roomTypes = roomService.getAllRoomTypes();
        System.out.println("\n--- All Room Types (" + roomTypes.size() + ") ---");
        if (roomTypes.isEmpty()) {
            System.out.println("(no room types found)");
            return;
        }
        for (RoomType rt : roomTypes) {
            System.out.printf("ID=%d | %s | Max Occupancy=%d | $%.2f/night%n",
                    rt.getRoomTypeId(), rt.getTypeName(), rt.getMaxOccupancy(), rt.getBasePrice());
        }
    }

    // ==================== Print Helpers ====================

    private void printReservation(Reservation r) {
        String guestInfo = r.getGuest() != null
                ? "Guest=" + r.getGuest().getGuestId() + " (" + r.getGuest().getName() + ")"
                : "Guest=N/A";
        String roomTypeInfo = r.getRoomType() != null
                ? "Type=" + r.getRoomType().getTypeName()
                : "Type=N/A";
        String roomInfo = r.getAssignedRoom() != null
                ? "Room#" + r.getAssignedRoom().getRoomNumber()
                : "Room=Not assigned";

        System.out.printf("ID=%d | %s | %s | %s | %s -> %s | Status=%s%n",
                r.getReservationId(),
                guestInfo,
                roomTypeInfo,
                roomInfo,
                r.getCheckInDate(),
                r.getCheckOutDate(),
                r.getStatus());
    }

    private void printReservationDetailed(Reservation r) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                    RESERVATION DETAILS                       ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.printf("║  Reservation ID: %-43d║%n", r.getReservationId());
        System.out.printf("║  Status: %-51s║%n", r.getStatus());
        System.out.println("╠══════════════════════════════════════════════════════════════╣");

        if (r.getGuest() != null) {
            System.out.printf("║  Guest ID: %-49d║%n", r.getGuest().getGuestId());
            System.out.printf("║  Guest Name: %-47s║%n", r.getGuest().getName());
            System.out.printf("║  Email: %-52s║%n", r.getGuest().getEmail());
        } else {
            System.out.println("║  Guest: N/A                                                  ║");
        }

        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.printf("║  Check-in: %-49s║%n", r.getCheckInDate());
        System.out.printf("║  Check-out: %-48s║%n", r.getCheckOutDate());
        System.out.printf("║  Number of Guests: %-41d║%n", r.getNumberOfGuests());

        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        if (r.getRoomType() != null) {
            System.out.printf("║  Room Type: %-48s║%n", r.getRoomType().getTypeName());
        }
        if (r.getAssignedRoom() != null) {
            System.out.printf("║  Assigned Room: #%-43s║%n", r.getAssignedRoom().getRoomNumber());
        } else {
            System.out.println("║  Assigned Room: Not assigned                                 ║");
        }

        if (r.getSpecialRequests() != null && !r.getSpecialRequests().isEmpty()) {
            System.out.println("╠══════════════════════════════════════════════════════════════╣");
            System.out.printf("║  Special Requests: %-41s║%n",
                    r.getSpecialRequests().length() > 41
                            ? r.getSpecialRequests().substring(0, 38) + "..."
                            : r.getSpecialRequests());
        }
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }
}
