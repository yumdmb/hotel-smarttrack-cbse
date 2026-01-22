package com.hotel.smarttrack.console;

import com.hotel.smarttrack.entity.Room;
import com.hotel.smarttrack.entity.RoomType;
import com.hotel.smarttrack.service.RoomService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Room Management Console Menu.
 * Provides operations for rooms and room types via terminal UI.
 */
public class RoomConsoleMenu {

    private final RoomService roomService;
    private final ConsoleInputHelper input;

    public RoomConsoleMenu(RoomService roomService, ConsoleInputHelper input) {
        this.roomService = roomService;
        this.input = input;
    }

    public void showMenu() {
        boolean running = true;
        while (running) {
            input.println("\n==============================");
            input.println("       ROOM MANAGEMENT        ");
            input.println("==============================");
            input.println("1. View All Rooms");
            input.println("2. View Room by ID");
            input.println("3. View Available Rooms (by date range)");
            input.println("4. View Rooms by Status");
            input.println("5. Create Room");
            input.println("6. Update Room Status");
            input.println("7. View All Room Types");
            input.println("8. Create Room Type");
            input.println("0. Back to Main Menu");

            String choice = input.readLine("Choose: ");
            try {
                switch (choice) {
                    case "1" -> viewAllRooms();
                    case "2" -> viewRoomById();
                    case "3" -> viewAvailableRooms();
                    case "4" -> viewRoomsByStatus();
                    case "5" -> createRoom();
                    case "6" -> updateRoomStatus();
                    case "7" -> viewAllRoomTypes();
                    case "8" -> createRoomType();
                    case "0" -> running = false;
                    default -> input.println("Invalid option.");
                }
            } catch (IllegalArgumentException ex) {
                input.println("[ERROR] Error: " + ex.getMessage());
            } catch (Exception ex) {
                input.println("[ERROR] Unexpected error: " + ex.getMessage());
            }
        }
    }

    private void viewAllRooms() {
        List<Room> rooms = roomService.getAllRooms();
        if (rooms.isEmpty()) {
            input.println("No rooms found.");
            return;
        }
        input.println("\n--- All Rooms ---");
        rooms.forEach(this::printRoom);
    }

    private void viewRoomById() {
        Long id = input.readLong("Room ID: ");
        Optional<Room> room = roomService.getRoomById(id);
        room.ifPresentOrElse(
                this::printRoom,
                () -> input.println("Room not found."));
    }

    private void viewAvailableRooms() {
        LocalDate checkIn = readDate("Check-in Date (YYYY-MM-DD): ");
        LocalDate checkOut = readDate("Check-out Date (YYYY-MM-DD): ");

        List<Room> rooms = roomService.getAvailableRooms(checkIn, checkOut);
        if (rooms.isEmpty()) {
            input.println("No available rooms for this date range.");
            return;
        }
        input.println("\n--- Available Rooms ---");
        rooms.forEach(this::printRoom);
    }

    private void viewRoomsByStatus() {
        String status = input.readLine("Status (AVAILABLE/OCCUPIED/UNDER_CLEANING/OUT_OF_SERVICE): ");

        List<Room> rooms = roomService.getRoomsByStatus(status);
        if (rooms.isEmpty()) {
            input.println("No rooms with status: " + status);
            return;
        }
        input.println("\n--- Rooms with status '" + status + "' ---");
        rooms.forEach(this::printRoom);
    }

    private void createRoom() {
        String roomNumber = input.readLine("Room Number: ");
        int floorNumber = input.readInt("Floor Number: ");

        // Display available room types for reference
        List<RoomType> types = roomService.getAllRoomTypes();
        if (!types.isEmpty()) {
            StringBuilder hint = new StringBuilder("Room Type ID (");
            for (int i = 0; i < types.size(); i++) {
                RoomType t = types.get(i);
                hint.append(t.getRoomTypeId()).append("=").append(t.getTypeName());
                if (i < types.size() - 1)
                    hint.append(", ");
            }
            hint.append("): ");
            Long roomTypeId = input.readLong(hint.toString());
            Room room = roomService.createRoom(roomNumber, floorNumber, roomTypeId);
            input.println("✅ Created: " + room);
        } else {
            input.println("[!] No room types available. Please create a room type first (option 8).");
        }
    }

    private void updateRoomStatus() {
        Long id = input.readLong("Room ID: ");
        String status = input.readLine("New Status (AVAILABLE/OCCUPIED/UNDER_CLEANING/OUT_OF_SERVICE): ");

        roomService.updateRoomStatus(id, status);
        input.println("✅ Room status updated.");
    }

    private void viewAllRoomTypes() {
        List<RoomType> types = roomService.getAllRoomTypes();
        if (types.isEmpty()) {
            input.println("No room types found.");
            return;
        }
        input.println("\n--- All Room Types ---");
        types.forEach(t -> input.println(String.format("ID=%d | %s | Base Price: $%s | Max Guests: %d",
                t.getRoomTypeId(), t.getTypeName(), t.getBasePrice(), t.getMaxOccupancy())));
    }

    private void createRoomType() {
        String typeName = input.readLine("Type Name (e.g., Standard, Deluxe, Suite): ");
        String description = input.readLine("Description: ");
        int maxOccupancy = input.readInt("Max Occupancy: ");
        BigDecimal basePrice = readBigDecimal("Base Price per night: ");

        RoomType roomType = roomService.createRoomType(typeName, description, maxOccupancy, basePrice);
        input.println("✅ Created: " + roomType);
    }

    private void printRoom(Room room) {
        input.println(String.format("ID=%d | Room %s | Floor %d | Type: %s | Status: %s",
                room.getRoomId(),
                room.getRoomNumber(),
                room.getFloorNumber(),
                room.getRoomType() != null ? room.getRoomType().getTypeName() : "N/A",
                room.getStatus()));
    }

    // ============ Utility Methods ============

    private BigDecimal readBigDecimal(String prompt) {
        while (true) {
            String s = input.readLine(prompt);
            try {
                return new BigDecimal(s);
            } catch (NumberFormatException e) {
                input.println("Please enter a valid decimal number.");
            }
        }
    }

    private LocalDate readDate(String prompt) {
        while (true) {
            String s = input.readLine(prompt);
            try {
                return LocalDate.parse(s);
            } catch (Exception e) {
                input.println("Please enter date in YYYY-MM-DD format.");
            }
        }
    }
}
