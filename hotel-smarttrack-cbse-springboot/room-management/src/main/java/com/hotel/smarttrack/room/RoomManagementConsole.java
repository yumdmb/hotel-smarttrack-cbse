package com.hotel.smarttrack.room;

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
            System.out.print("\nEnter your choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> manageRoomRecords();
                case "2" -> manageRoomStatus();
                case "3" -> manageRoomPricing();
                case "4" -> displayRoomAvailability();
                case "0" -> running = false;
                default -> System.out.println("\n⚠ Invalid choice. Please enter a number from 0-4.");
            }
        }
    }

    private void printRoomMenu() {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                    ROOM MANAGEMENT                           ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║                                                              ║");
        System.out.println("║   1. Manage Room Records                                     ║");
        System.out.println("║   2. Manage Room Status                                      ║");
        System.out.println("║   3. Manage Room Pricing                                     ║");
        System.out.println("║   4. Display Room Availability                               ║");
        System.out.println("║                                                              ║");
        System.out.println("║   0. Back to Main Menu                                       ║");
        System.out.println("║                                                              ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }

    // ============ UC5: Manage Room Records ============

    private void manageRoomRecords() {
        boolean running = true;
        while (running) {
            System.out.println("\n┌─────────────────────────────────────────────────────────────┐");
            System.out.println("│  MANAGE ROOM RECORDS                                        │");
            System.out.println("├─────────────────────────────────────────────────────────────┤");
            System.out.println("│  1. Create Room                                             │");
            System.out.println("│  2. View All Rooms                                          │");
            System.out.println("│  3. View Room Details                                       │");
            System.out.println("│  4. Update Room                                             │");
            System.out.println("│  5. Delete Room                                             │");
            System.out.println("│  6. Manage Room Types                                       │");
            System.out.println("│  0. Back                                                    │");
            System.out.println("└─────────────────────────────────────────────────────────────┘");
            System.out.print("\nChoice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> createRoom();
                case "2" -> viewAllRooms();
                case "3" -> viewRoomDetails();
                case "4" -> updateRoom();
                case "5" -> deleteRoom();
                case "6" -> manageRoomTypes();
                case "0" -> running = false;
                default -> System.out.println("⚠ Invalid choice.");
            }
        }
    }

    private void createRoom() {
        System.out.println("\n▸ CREATE NEW ROOM");
        
        try {
            System.out.print("Room Number: ");
            String roomNumber = scanner.nextLine().trim();

            System.out.print("Floor Number: ");
            int floorNumber = Integer.parseInt(scanner.nextLine().trim());

            // Display room types
            List<RoomType> roomTypes = roomService.getAllRoomTypes();
            if (roomTypes.isEmpty()) {
                System.out.println("⚠ No room types available. Please create a room type first.");
                return;
            }

            System.out.println("\nAvailable Room Types:");
            for (RoomType rt : roomTypes) {
                System.out.printf("  %d. %s - $%.2f/night\n", 
                    rt.getRoomTypeId(), rt.getTypeName(), rt.getBasePrice());
            }

            System.out.print("Select Room Type ID: ");
            Long roomTypeId = Long.parseLong(scanner.nextLine().trim());

            Room room = roomService.createRoom(roomNumber, floorNumber, roomTypeId);
            System.out.println("✓ Room created successfully!");
            System.out.printf("  Room #%s, Floor %d, Type: %s\n", 
                room.getRoomNumber(), room.getFloorNumber(), room.getRoomType().getTypeName());

        } catch (NumberFormatException e) {
            System.out.println("⚠ Error: Invalid number format.");
        } catch (Exception e) {
            System.out.println("⚠ Error: " + e.getMessage());
        }
    }

    private void viewAllRooms() {
        System.out.println("\n▸ ALL ROOMS");
        List<Room> rooms = roomService.getAllRooms();

        if (rooms.isEmpty()) {
            System.out.println("  No rooms found.");
            return;
        }

        System.out.println("\n┌──────┬────────┬─────────────────┬───────────────────┐");
        System.out.println("│ Room │ Floor  │ Type            │ Status            │");
        System.out.println("├──────┼────────┼─────────────────┼───────────────────┤");
        
        for (Room room : rooms) {
            System.out.printf("│ %-4s │ %-6d │ %-15s │ %-17s │\n",
                room.getRoomNumber(),
                room.getFloorNumber(),
                room.getRoomType().getTypeName(),
                room.getStatus());
        }
        System.out.println("└──────┴────────┴─────────────────┴───────────────────┘");
    }

    private void viewRoomDetails() {
        System.out.print("\nEnter Room Number: ");
        String roomNumber = scanner.nextLine().trim();

        Optional<Room> roomOpt = roomService.getRoomByNumber(roomNumber);
        if (roomOpt.isEmpty()) {
            System.out.println("⚠ Room not found.");
            return;
        }

        Room room = roomOpt.get();
        RoomType type = room.getRoomType();

        System.out.println("\n┌─────────────────────────────────────────────────────────────┐");
        System.out.println("│  ROOM DETAILS                                               │");
        System.out.println("├─────────────────────────────────────────────────────────────┤");
        System.out.printf("│  Room Number:     %-40s │\n", room.getRoomNumber());
        System.out.printf("│  Floor:           %-40d │\n", room.getFloorNumber());
        System.out.printf("│  Type:            %-40s │\n", type.getTypeName());
        System.out.printf("│  Max Occupancy:   %-40d │\n", type.getMaxOccupancy());
        System.out.printf("│  Base Price:      $%-39.2f │\n", type.getBasePrice());
        System.out.printf("│  Status:          %-40s │\n", room.getStatus());
        System.out.println("└─────────────────────────────────────────────────────────────┘");
    }

    private void updateRoom() {
        System.out.print("\nEnter Room Number to Update: ");
        String roomNumber = scanner.nextLine().trim();

        Optional<Room> roomOpt = roomService.getRoomByNumber(roomNumber);
        if (roomOpt.isEmpty()) {
            System.out.println("⚠ Room not found.");
            return;
        }

        Room room = roomOpt.get();

        try {
            System.out.print("New Floor Number (current: " + room.getFloorNumber() + "): ");
            String floorInput = scanner.nextLine().trim();
            if (!floorInput.isEmpty()) {
                room.setFloorNumber(Integer.parseInt(floorInput));
            }

            roomService.updateRoom(room);
            System.out.println("✓ Room updated successfully!");

        } catch (Exception e) {
            System.out.println("⚠ Error: " + e.getMessage());
        }
    }

    private void deleteRoom() {
        System.out.print("\nEnter Room Number to Delete: ");
        String roomNumber = scanner.nextLine().trim();

        Optional<Room> roomOpt = roomService.getRoomByNumber(roomNumber);
        if (roomOpt.isEmpty()) {
            System.out.println("⚠ Room not found.");
            return;
        }

        Room room = roomOpt.get();
        System.out.print("Confirm delete room #" + roomNumber + "? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if ("yes".equals(confirm)) {
            roomService.deleteRoom(room.getRoomId());
            System.out.println("✓ Room deleted successfully.");
        } else {
            System.out.println("  Deletion cancelled.");
        }
    }

    private void manageRoomTypes() {
        boolean running = true;
        while (running) {
            System.out.println("\n┌─────────────────────────────────────────────────────────────┐");
            System.out.println("│  MANAGE ROOM TYPES                                          │");
            System.out.println("├─────────────────────────────────────────────────────────────┤");
            System.out.println("│  1. Create Room Type                                        │");
            System.out.println("│  2. View All Room Types                                     │");
            System.out.println("│  0. Back                                                    │");
            System.out.println("└─────────────────────────────────────────────────────────────┘");
            System.out.print("\nChoice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> createRoomType();
                case "2" -> viewAllRoomTypes();
                case "0" -> running = false;
                default -> System.out.println("⚠ Invalid choice.");
            }
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

            System.out.print("Base Price per Night: $");
            BigDecimal basePrice = new BigDecimal(scanner.nextLine().trim());

            RoomType roomType = roomService.createRoomType(typeName, description, maxOccupancy, basePrice);
            System.out.println("✓ Room type created successfully!");
            System.out.printf("  ID: %d, %s - $%.2f/night\n", 
                roomType.getRoomTypeId(), roomType.getTypeName(), roomType.getBasePrice());

        } catch (Exception e) {
            System.out.println("⚠ Error: " + e.getMessage());
        }
    }

    private void viewAllRoomTypes() {
        System.out.println("\n▸ ALL ROOM TYPES");
        List<RoomType> types = roomService.getAllRoomTypes();

        if (types.isEmpty()) {
            System.out.println("  No room types found.");
            return;
        }

        System.out.println("\n┌────┬─────────────────┬──────────────┬────────────┐");
        System.out.println("│ ID │ Type Name       │ Max Occupancy│ Base Price │");
        System.out.println("├────┼─────────────────┼──────────────┼────────────┤");
        
        for (RoomType type : types) {
            System.out.printf("│ %-2d │ %-15s │ %-12d │ $%-9.2f │\n",
                type.getRoomTypeId(),
                type.getTypeName(),
                type.getMaxOccupancy(),
                type.getBasePrice());
        }
        System.out.println("└────┴─────────────────┴──────────────┴────────────┘");
    }

    // ============ UC6: Manage Room Status ============

    private void manageRoomStatus() {
        System.out.println("\n▸ MANAGE ROOM STATUS");
        System.out.print("Enter Room Number: ");
        String roomNumber = scanner.nextLine().trim();

        Optional<Room> roomOpt = roomService.getRoomByNumber(roomNumber);
        if (roomOpt.isEmpty()) {
            System.out.println("⚠ Room not found.");
            return;
        }

        Room room = roomOpt.get();
        System.out.println("  Current Status: " + room.getStatus());
        System.out.println("\nAvailable Statuses:");
        System.out.println("  1. Available");
        System.out.println("  2. Occupied");
        System.out.println("  3. Under Cleaning");
        System.out.println("  4. Out of Service");
        System.out.print("\nSelect New Status (1-4): ");

        String statusChoice = scanner.nextLine().trim();
        String newStatus = switch (statusChoice) {
            case "1" -> "Available";
            case "2" -> "Occupied";
            case "3" -> "Under Cleaning";
            case "4" -> "Out of Service";
            default -> null;
        };

        if (newStatus == null) {
            System.out.println("⚠ Invalid status choice.");
            return;
        }

        try {
            roomService.updateRoomStatus(room.getRoomId(), newStatus);
            System.out.println("✓ Room status updated to: " + newStatus);
        } catch (Exception e) {
            System.out.println("⚠ Error: " + e.getMessage());
        }
    }

    // ============ UC7: Manage Room Pricing ============

    private void manageRoomPricing() {
        System.out.println("\n▸ MANAGE ROOM PRICING");
        
        List<RoomType> types = roomService.getAllRoomTypes();
        if (types.isEmpty()) {
            System.out.println("  No room types available.");
            return;
        }

        System.out.println("\nRoom Types:");
        for (RoomType type : types) {
            System.out.printf("  %d. %s - $%.2f/night (Tax: %.1f%%)\n",
                type.getRoomTypeId(), type.getTypeName(), 
                type.getBasePrice(), type.getTaxRate().multiply(BigDecimal.valueOf(100)));
        }

        try {
            System.out.print("\nSelect Room Type ID to Update Pricing: ");
            Long roomTypeId = Long.parseLong(scanner.nextLine().trim());

            System.out.print("New Base Price: $");
            BigDecimal newPrice = new BigDecimal(scanner.nextLine().trim());

            System.out.print("New Tax Rate (e.g., 0.10 for 10%): ");
            BigDecimal newTaxRate = new BigDecimal(scanner.nextLine().trim());

            RoomType updated = roomService.updateRoomPricing(roomTypeId, newPrice, newTaxRate);
            System.out.println("✓ Pricing updated successfully!");
            System.out.printf("  %s: $%.2f/night (Tax: %.1f%%)\n",
                updated.getTypeName(), updated.getBasePrice(), 
                updated.getTaxRate().multiply(BigDecimal.valueOf(100)));

        } catch (Exception e) {
            System.out.println("⚠ Error: " + e.getMessage());
        }
    }

    // ============ UC8: Display Room Availability ============

    private void displayRoomAvailability() {
        System.out.println("\n▸ ROOM AVAILABILITY");
        
        try {
            System.out.print("Check-in Date (yyyy-MM-dd): ");
            String checkInStr = scanner.nextLine().trim();
            LocalDate checkIn = LocalDate.parse(checkInStr, dateFormatter);

            System.out.print("Check-out Date (yyyy-MM-dd): ");
            String checkOutStr = scanner.nextLine().trim();
            LocalDate checkOut = LocalDate.parse(checkOutStr, dateFormatter);

            if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
                System.out.println("⚠ Check-out date must be after check-in date.");
                return;
            }

            List<Room> availableRooms = roomService.getAvailableRooms(checkIn, checkOut);

            if (availableRooms.isEmpty()) {
                System.out.println("  No rooms available for the selected dates.");
                return;
            }

            System.out.printf("\n▸ Available Rooms (%s to %s)\n", checkInStr, checkOutStr);
            System.out.println("\n┌──────┬────────┬─────────────────┬────────────┐");
            System.out.println("│ Room │ Floor  │ Type            │ Price/Night│");
            System.out.println("├──────┼────────┼─────────────────┼────────────┤");

            for (Room room : availableRooms) {
                System.out.printf("│ %-4s │ %-6d │ %-15s │ $%-9.2f │\n",
                    room.getRoomNumber(),
                    room.getFloorNumber(),
                    room.getRoomType().getTypeName(),
                    room.getRoomType().getBasePrice());
            }
            System.out.println("└──────┴────────┴─────────────────┴────────────┘");

        } catch (DateTimeParseException e) {
            System.out.println("⚠ Invalid date format. Use yyyy-MM-dd.");
        } catch (Exception e) {
            System.out.println("⚠ Error: " + e.getMessage());
        }
    }
}
