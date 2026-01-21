package com.hotel.smarttrack.console;

import com.hotel.smarttrack.entity.Room;
import com.hotel.smarttrack.entity.RoomType;
import com.hotel.smarttrack.service.RoomService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Room Management Console Menu.
 * Provides operations for rooms and room types via terminal UI.
 */
public class RoomConsoleMenu {

    private final RoomService roomService;

    public RoomConsoleMenu(RoomService roomService) {
        this.roomService = roomService;
    }

    public void showMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("\n==============================");
            System.out.println("       ROOM MANAGEMENT        ");
            System.out.println("==============================");
            System.out.println("1. View All Rooms");
            System.out.println("2. View Room by ID");
            System.out.println("3. View Available Rooms (by date range)");
            System.out.println("4. View Rooms by Status");
            System.out.println("5. Create Room");
            System.out.println("6. Update Room Status");
            System.out.println("7. View All Room Types");
            System.out.println("8. Create Room Type");
            System.out.println("0. Back to Main Menu");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> viewAllRooms();
                    case "2" -> viewRoomById(scanner);
                    case "3" -> viewAvailableRooms(scanner);
                    case "4" -> viewRoomsByStatus(scanner);
                    case "5" -> createRoom(scanner);
                    case "6" -> updateRoomStatus(scanner);
                    case "7" -> viewAllRoomTypes();
                    case "8" -> createRoomType(scanner);
                    case "0" -> running = false;
                    default -> System.out.println("Invalid option.");
                }
            } catch (IllegalArgumentException ex) {
                System.out.println("❌ Error: " + ex.getMessage());
            } catch (Exception ex) {
                System.out.println("❌ Unexpected error: " + ex.getMessage());
            }
        }
    }

    private void viewAllRooms() {
        List<Room> rooms = roomService.getAllRooms();
        if (rooms.isEmpty()) {
            System.out.println("No rooms found.");
            return;
        }
        System.out.println("\n--- All Rooms ---");
        rooms.forEach(this::printRoom);
    }

    private void viewRoomById(Scanner scanner) {
        Long id = readLong(scanner, "Room ID: ");
        Optional<Room> room = roomService.getRoomById(id);
        room.ifPresentOrElse(
            this::printRoom,
            () -> System.out.println("Room not found.")
        );
    }

    private void viewAvailableRooms(Scanner scanner) {
        LocalDate checkIn = readDate(scanner, "Check-in Date (YYYY-MM-DD): ");
        LocalDate checkOut = readDate(scanner, "Check-out Date (YYYY-MM-DD): ");
        
        List<Room> rooms = roomService.getAvailableRooms(checkIn, checkOut);
        if (rooms.isEmpty()) {
            System.out.println("No available rooms for this date range.");
            return;
        }
        System.out.println("\n--- Available Rooms ---");
        rooms.forEach(this::printRoom);
    }

    private void viewRoomsByStatus(Scanner scanner) {
        System.out.print("Status (Available/Occupied/Under Cleaning/Out of Service): ");
        String status = scanner.nextLine().trim();
        
        List<Room> rooms = roomService.getRoomsByStatus(status);
        if (rooms.isEmpty()) {
            System.out.println("No rooms with status: " + status);
            return;
        }
        System.out.println("\n--- Rooms with status '" + status + "' ---");
        rooms.forEach(this::printRoom);
    }

    private void createRoom(Scanner scanner) {
        System.out.print("Room Number: ");
        String roomNumber = scanner.nextLine();
        int floorNumber = readInt(scanner, "Floor Number: ");
        Long roomTypeId = readLong(scanner, "Room Type ID: ");

        Room room = roomService.createRoom(roomNumber, floorNumber, roomTypeId);
        System.out.println("✅ Created: " + room);
    }

    private void updateRoomStatus(Scanner scanner) {
        Long id = readLong(scanner, "Room ID: ");
        System.out.print("New Status (Available/Occupied/Under Cleaning/Out of Service): ");
        String status = scanner.nextLine().trim();

        roomService.updateRoomStatus(id, status);
        System.out.println("✅ Room status updated.");
    }

    private void viewAllRoomTypes() {
        List<RoomType> types = roomService.getAllRoomTypes();
        if (types.isEmpty()) {
            System.out.println("No room types found.");
            return;
        }
        System.out.println("\n--- All Room Types ---");
        types.forEach(t -> System.out.printf("ID=%d | %s | Base Price: $%s | Max Guests: %d%n",
            t.getRoomTypeId(), t.getTypeName(), t.getBasePrice(), t.getMaxOccupancy()));
    }

    private void createRoomType(Scanner scanner) {
        System.out.print("Type Name (e.g., Standard, Deluxe, Suite): ");
        String typeName = scanner.nextLine();
        System.out.print("Description: ");
        String description = scanner.nextLine();
        int maxOccupancy = readInt(scanner, "Max Occupancy: ");
        BigDecimal basePrice = readBigDecimal(scanner, "Base Price per night: ");

        // OSGi API: createRoomType(typeName, description, maxOccupancy, basePrice)
        RoomType roomType = roomService.createRoomType(typeName, description, maxOccupancy, basePrice);
        System.out.println("✅ Created: " + roomType);
    }

    private void printRoom(Room room) {
        System.out.printf("ID=%d | Room %s | Floor %d | Type: %s | Status: %s%n",
            room.getRoomId(),
            room.getRoomNumber(),
            room.getFloorNumber(),
            room.getRoomType() != null ? room.getRoomType().getTypeName() : "N/A",
            room.getStatus());
    }

    // ============ Utility Methods ============

    private Long readLong(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
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

    private BigDecimal readBigDecimal(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = scanner.nextLine().trim();
            try {
                return new BigDecimal(s);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid decimal number.");
            }
        }
    }

    private LocalDate readDate(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = scanner.nextLine().trim();
            try {
                return LocalDate.parse(s);
            } catch (Exception e) {
                System.out.println("Please enter date in YYYY-MM-DD format.");
            }
        }
    }
}
