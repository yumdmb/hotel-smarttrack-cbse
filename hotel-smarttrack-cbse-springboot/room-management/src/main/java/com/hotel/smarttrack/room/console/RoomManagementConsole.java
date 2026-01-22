package com.hotel.smarttrack.room.console;

import com.hotel.smarttrack.entity.Room;
import com.hotel.smarttrack.entity.RoomType;
import com.hotel.smarttrack.service.RoomService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * RoomManagementConsole - Terminal UI for Room Management.
 * Provides menu-driven interface for room operations.
 */
@Component
public class RoomManagementConsole {

    private final RoomService roomService;
    private Scanner scanner;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public RoomManagementConsole(RoomService roomService) {
        this.roomService = roomService;
    }

    /**
     * Main menu - called from MainMenuConsole
     */
    public void showMenu(Scanner scanner) {
        this.scanner = scanner;

        boolean running = true;
        while (running) {
            printRoomMenu();
            System.out.print("\nChoose: ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> viewAllRooms();
                    case "2" -> viewRoomDetails();
                    case "3" -> createRoom();
                    case "4" -> viewAllRoomTypes();
                    case "5" -> createRoomType();
                    case "6" -> viewRoomsByStatus();
                    case "7" -> updateRoomStatus();
                    case "8" -> viewRoomTypePricing();
                    case "9" -> updateRoomTypePricing();
                    case "10" -> displayRoomAvailability();
                    case "0" -> running = false;
                    default -> System.out.println("⚠ Invalid option.");
                }
            } catch (IllegalArgumentException ex) {
                System.out.println("⚠ Error: " + ex.getMessage());
            } catch (Exception ex) {
                System.out.println("⚠ Unexpected error: " + ex.getMessage());
            }
        }
    }

    private void printRoomMenu() {
        System.out.println("\n==============================");
        System.out.println("       ROOM MANAGEMENT        ");
        System.out.println("==============================");
        System.out.println("UC5 - Manage Room Records:");
        System.out.println("  1. View All Rooms");
        System.out.println("  2. View Room by ID");
        System.out.println("  3. Create Room");
        System.out.println("  4. View All Room Types");
        System.out.println("  5. Create Room Type");
        System.out.println("");
        System.out.println("UC6 - Manage Room Status:");
        System.out.println("  6. View Rooms by Status");
        System.out.println("  7. Update Room Status");
        System.out.println("");
        System.out.println("UC7 - Manage Room Pricing:");
        System.out.println("  8. View Room Type Pricing");
        System.out.println("  9. Update Room Type Pricing");
        System.out.println("");
        System.out.println("UC8 - Display Room Availability:");
        System.out.println(" 10. View Available Rooms (by date range)");
        System.out.println("");
        System.out.println("  0. Back to Main Menu");
    }

    // ============ UC5: Manage Room Records ============

    private void createRoom() {
        System.out.println("\n▸ CREATE NEW ROOM");

        try {
            // Display available room types first
            List<RoomType> roomTypes = roomService.getAllRoomTypes();
            if (roomTypes.isEmpty()) {
                System.out.println("⚠ No room types available. Please create a room type first.");
                return;
            }

            System.out.println("\n--- Available Room Types ---");
            for (RoomType rt : roomTypes) {
                System.out.printf("  %d = %s (Base Price: $%.2f/night, Max Guests: %d)\n",
                        rt.getRoomTypeId(), rt.getTypeName(), rt.getBasePrice(), rt.getMaxOccupancy());
            }

            System.out.print("\nRoom Number: ");
            String roomNumber = scanner.nextLine().trim();

            System.out.print("Floor Number: ");
            int floorNumber = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Room Type ID: ");
            Long roomTypeId = Long.parseLong(scanner.nextLine().trim());

            Room room = roomService.createRoom(roomNumber, floorNumber, roomTypeId);
            System.out.println("✅ Created: " + room);

        } catch (NumberFormatException e) {
            System.out.println("⚠ Error: Invalid number format.");
        } catch (Exception e) {
            System.out.println("⚠ Error: " + e.getMessage());
        }
    }

    private void viewAllRooms() {
        System.out.println("\n--- All Rooms ---");
        List<Room> rooms = roomService.getAllRooms();

        if (rooms.isEmpty()) {
            System.out.println("No rooms found.");
            return;
        }

        for (Room room : rooms) {
            System.out.printf("ID=%d | Room %s | Floor %d | Type: %s | Status: %s\n",
                    room.getRoomId(),
                    room.getRoomNumber(),
                    room.getFloorNumber(),
                    room.getRoomType().getTypeName(),
                    room.getStatus());
        }
    }

    private void viewRoomDetails() {
        System.out.print("\nRoom ID: ");
        try {
            Long roomId = Long.parseLong(scanner.nextLine().trim());
            
            Optional<Room> roomOpt = roomService.getRoomById(roomId);
            if (roomOpt.isEmpty()) {
                System.out.println("⚠ Room not found.");
                return;
            }

            Room room = roomOpt.get();
            System.out.printf("ID=%d | Room %s | Floor %d | Type: %s | Status: %s\n",
                    room.getRoomId(),
                    room.getRoomNumber(),
                    room.getFloorNumber(),
                    room.getRoomType() != null ? room.getRoomType().getTypeName() : "N/A",
                    room.getStatus());
        } catch (NumberFormatException e) {
            System.out.println("⚠ Error: Invalid number format.");
        }
    }

    private void viewAllRoomTypes() {
        System.out.println("\n--- All Room Types ---");
        List<RoomType> types = roomService.getAllRoomTypes();

        if (types.isEmpty()) {
            System.out.println("No room types found.");
            return;
        }

        for (RoomType type : types) {
            System.out.printf("ID=%d | %s | Base Price: $%s | Max Guests: %d\n",
                    type.getRoomTypeId(), type.getTypeName(), type.getBasePrice(), type.getMaxOccupancy());
        }
    }

    private void createRoomType() {
        System.out.println("\n▸ CREATE ROOM TYPE");

        try {
            System.out.print("Type Name (e.g., Standard, Deluxe, Suite): ");
            String typeName = scanner.nextLine().trim();

            System.out.print("Description: ");
            String description = scanner.nextLine().trim();

            System.out.print("Max Occupancy: ");
            int maxOccupancy = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Base Price per night: ");
            BigDecimal basePrice = new BigDecimal(scanner.nextLine().trim());

            RoomType roomType = roomService.createRoomType(typeName, description, maxOccupancy, basePrice);
            System.out.println("✅ Created: " + roomType);

        } catch (Exception e) {
            System.out.println("⚠ Error: " + e.getMessage());
        }
    }

    // ============ UC6: Manage Room Status ============

    private void viewRoomsByStatus() {
        System.out.print("Status (AVAILABLE/OCCUPIED/UNDER_CLEANING/OUT_OF_SERVICE): ");
        String status = scanner.nextLine().trim();

        List<Room> rooms = roomService.getRoomsByStatus(status);
        if (rooms.isEmpty()) {
            System.out.println("No rooms with status: " + status);
            return;
        }

        System.out.println("\n--- Rooms with status '" + status + "' ---");
        for (Room room : rooms) {
            System.out.printf("ID=%d | Room %s | Floor %d | Type: %s | Status: %s\n",
                    room.getRoomId(),
                    room.getRoomNumber(),
                    room.getFloorNumber(),
                    room.getRoomType().getTypeName(),
                    room.getStatus());
        }
    }

    private void updateRoomStatus() {
        try {
            System.out.print("Room ID: ");
            Long id = Long.parseLong(scanner.nextLine().trim());

            System.out.print("New Status (AVAILABLE/OCCUPIED/UNDER_CLEANING/OUT_OF_SERVICE): ");
            String status = scanner.nextLine().trim();

            roomService.updateRoomStatus(id, status);
            System.out.println("✅ Room status updated.");
        } catch (NumberFormatException e) {
            System.out.println("⚠ Error: Invalid number format.");
        } catch (Exception e) {
            System.out.println("⚠ Error: " + e.getMessage());
        }
    }

    // ============ UC7: Manage Room Pricing ============

    private void viewRoomTypePricing() {
        List<RoomType> types = roomService.getAllRoomTypes();
        if (types.isEmpty()) {
            System.out.println("No room types found.");
            return;
        }

        System.out.println("\n┌───────────────────────────────────────────────────────┐");
        System.out.println("│                 ROOM TYPE PRICING                     │");
        System.out.println("├─────┬─────────────────┬──────────────┬───────────────┤");
        System.out.println("│ ID  │ Type Name       │ Base Price   │ Max Guests    │");
        System.out.println("├─────┼─────────────────┼──────────────┼───────────────┤");

        for (RoomType t : types) {
            System.out.printf("│ %-3d │ %-15s │ $%-11s │ %-13d │\n",
                    t.getRoomTypeId(),
                    t.getTypeName(),
                    t.getBasePrice(),
                    t.getMaxOccupancy());
        }
        System.out.println("└─────┴─────────────────┴──────────────┴───────────────┘");
    }

    private void updateRoomTypePricing() {
        List<RoomType> types = roomService.getAllRoomTypes();
        if (types.isEmpty()) {
            System.out.println("No room types available.");
            return;
        }

        System.out.println("\n--- Current Room Type Pricing ---");
        for (RoomType t : types) {
            System.out.printf("  ID=%d | %s | Current Price: $%s/night\n",
                    t.getRoomTypeId(), t.getTypeName(), t.getBasePrice());
        }

        try {
            System.out.print("\nRoom Type ID to update: ");
            Long roomTypeId = Long.parseLong(scanner.nextLine().trim());

            Optional<RoomType> roomTypeOpt = types.stream()
                    .filter(t -> t.getRoomTypeId().equals(roomTypeId))
                    .findFirst();

            if (roomTypeOpt.isEmpty()) {
                System.out.println("⚠ Room type not found.");
                return;
            }

            RoomType roomType = roomTypeOpt.get();
            System.out.println("Current price for " + roomType.getTypeName() + ": $" + roomType.getBasePrice());

            System.out.print("New Base Price per night: $");
            BigDecimal newPrice = new BigDecimal(scanner.nextLine().trim());

            roomService.updateRoomPricing(roomTypeId, newPrice, null);
            System.out.println("✅ Price updated successfully. New price: $" + newPrice + "/night");

        } catch (NumberFormatException e) {
            System.out.println("⚠ Error: Invalid number format.");
        } catch (Exception e) {
            System.out.println("⚠ Error: " + e.getMessage());
        }
    }

    // ============ UC8: Display Room Availability ============

    private void displayRoomAvailability() {
        try {
            System.out.print("Check-in Date (YYYY-MM-DD): ");
            String checkInStr = scanner.nextLine().trim();
            LocalDate checkIn = LocalDate.parse(checkInStr, dateFormatter);

            System.out.print("Check-out Date (YYYY-MM-DD): ");
            String checkOutStr = scanner.nextLine().trim();
            LocalDate checkOut = LocalDate.parse(checkOutStr, dateFormatter);

            if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
                System.out.println("⚠ Check-out date must be after check-in date.");
                return;
            }

            List<Room> availableRooms = roomService.getAvailableRooms(checkIn, checkOut);

            if (availableRooms.isEmpty()) {
                System.out.println("No available rooms for this date range.");
                return;
            }

            System.out.println("\n--- Available Rooms ---");
            for (Room room : availableRooms) {
                System.out.printf("ID=%d | Room %s | Floor %d | Type: %s | Status: %s\n",
                        room.getRoomId(),
                        room.getRoomNumber(),
                        room.getFloorNumber(),
                        room.getRoomType().getTypeName(),
                        room.getStatus());
            }

        } catch (DateTimeParseException e) {
            System.out.println("⚠ Invalid date format. Use YYYY-MM-DD.");
        } catch (Exception e) {
            System.out.println("⚠ Error: " + e.getMessage());
        }
    }
}
