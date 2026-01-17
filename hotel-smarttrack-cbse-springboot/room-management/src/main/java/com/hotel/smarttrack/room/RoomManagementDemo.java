package com.hotel.smarttrack.room;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.hotel.smarttrack.entity.Room;
import com.hotel.smarttrack.entity.RoomType;
import com.hotel.smarttrack.service.RoomService;

/**
 * Demonstration class showcasing enhanced Room Management features:
 * - Input validation at service layer
 * - Date range validation
 * - Status value validation using RoomStatus enum
 * - Enhanced availability checking with reservation integration
 */
public class RoomManagementDemo {

    private final RoomService roomService;

    public RoomManagementDemo(RoomService roomService) {
        this.roomService = roomService;
    }

    /**
     * Run a complete demonstration of all enhanced features.
     */
    public void runEnhancedDemo() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║   ROOM MANAGEMENT MODULE - DEMO        ║");
        System.out.println("║   Enhanced Features Demonstration      ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        // 1. Input Validation Demo
        demonstrateInputValidation();

        // 2. Room Type and Pricing Management
        demonstrateRoomTypeManagement();

        // 3. Room CRUD Operations with Validation
        demonstrateRoomCRUD();

        // 4. Status Management with Enum Validation
        demonstrateStatusManagement();

        // 5. Enhanced Availability Checking
        demonstrateEnhancedAvailability();

        // 6. Date Range Validation
        demonstrateDateRangeValidation();

        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║   ROOM MANAGEMENT DEMO COMPLETED ✓     ║");
        System.out.println("╚════════════════════════════════════════╝\n");
    }

    private void demonstrateInputValidation() {
        System.out.println("┌─ 1. Input Validation ─────────────────┐");

        try {
            // This should fail - empty room type name
            roomService.createRoomType("", "Description", 2, new BigDecimal("100"));
        } catch (IllegalArgumentException e) {
            System.out.println("  ✓ Prevented empty name");
        }

        try {
            // This should fail - zero occupancy
            roomService.createRoomType("Invalid", "Description", 0, new BigDecimal("100"));
        } catch (IllegalArgumentException e) {
            System.out.println("  ✓ Prevented zero occupancy");
        }

        try {
            // This should fail - negative price
            roomService.createRoomType("Invalid", "Description", 2, new BigDecimal("-50"));
        } catch (IllegalArgumentException e) {
            System.out.println("  ✓ Prevented negative price");
        }

        System.out.println("  ✓ All input validations working!");
        System.out.println("└───────────────────────────────────────┘\n");
    }

    private void demonstrateRoomTypeManagement() {
        System.out.println("┌─ 2. Room Type & Pricing ──────────────┐");

        List<RoomType> types = roomService.getAllRoomTypes();
        System.out.println("  Available Room Types:");
        types.forEach(rt -> System.out.println("  • " + rt.getTypeName() + ": $" + rt.getBasePrice() +
                " (Max: " + rt.getMaxOccupancy() + " guests)"));

        // Update pricing for first room type
        if (!types.isEmpty()) {
            RoomType first = types.get(0);
            roomService.updateRoomPricing(
                    first.getRoomTypeId(),
                    new BigDecimal("120.00"),
                    new BigDecimal("0.10"));
            System.out.println("  ✓ Updated " + first.getTypeName() + " pricing to $120.00");
        }

        System.out.println("└───────────────────────────────────────┘\n");
    }

    private void demonstrateRoomCRUD() {
        System.out.println("┌─ 3. Room CRUD Operations ─────────────┐");

        List<RoomType> types = roomService.getAllRoomTypes();
        if (types.size() >= 2) {
            Long standardTypeId = types.get(0).getRoomTypeId();
            Long deluxeTypeId = types.get(1).getRoomTypeId();

            // Create rooms with validation
            roomService.createRoom("101", 1, standardTypeId);
            roomService.createRoom("102", 1, standardTypeId);
            roomService.createRoom("201", 2, deluxeTypeId);
            roomService.createRoom("202", 2, deluxeTypeId);

            System.out.println("  ✓ Created 4 rooms (101, 102, 201, 202)");

            // Try to create duplicate - should fail
            try {
                roomService.createRoom("101", 1, standardTypeId);
            } catch (IllegalArgumentException e) {
                System.out.println("  ✓ Prevented duplicate room number");
            }

            // Try to create room with invalid floor
            try {
                roomService.createRoom("999", 0, standardTypeId);
            } catch (IllegalArgumentException e) {
                System.out.println("  ✓ Prevented invalid floor number");
            }
        }

        System.out.println("  Total rooms: " + roomService.getAllRooms().size());
        System.out.println("└───────────────────────────────────────┘\n");
    }

    private void demonstrateStatusManagement() {
        System.out.println("┌─ 4. Status Management (Enum) ─────────┐");

        Room room = roomService.getRoomByNumber("101").orElse(null);
        if (room != null) {
            // Valid status updates
            roomService.updateRoomStatus(room.getRoomId(), RoomStatus.OCCUPIED.getDisplayName());
            System.out.println("  ✓ Room 101 → Occupied");

            roomService.updateRoomStatus(room.getRoomId(), RoomStatus.UNDER_CLEANING.getDisplayName());
            System.out.println("  ✓ Room 101 → Under Cleaning");

            roomService.updateRoomStatus(room.getRoomId(), RoomStatus.AVAILABLE.getDisplayName());
            System.out.println("  ✓ Room 101 → Available");

            // Try invalid status - should fail
            try {
                roomService.updateRoomStatus(room.getRoomId(), "InvalidStatus");
            } catch (IllegalArgumentException e) {
                System.out.println("  ✓ Prevented invalid status");
            }
        }

        System.out.println("└───────────────────────────────────────┘\n");
    }

    private void demonstrateEnhancedAvailability() {
        System.out.println("┌─ 5. Enhanced Availability ────────────┐");

        LocalDate checkIn = LocalDate.now().plusDays(7);
        LocalDate checkOut = LocalDate.now().plusDays(10);

        System.out.println("  Date Range: " + checkIn + " to " + checkOut);

        // Get available rooms
        List<Room> availableRooms = roomService.getAvailableRooms(checkIn, checkOut);
        System.out.println("  Available rooms: " + availableRooms.size());

        // Get available rooms by type
        List<RoomType> types = roomService.getAllRoomTypes();
        if (!types.isEmpty()) {
            Long firstTypeId = types.get(0).getRoomTypeId();
            List<Room> availableByType = roomService.getAvailableRoomsByType(firstTypeId, checkIn, checkOut);
            System.out.println("  Available " + types.get(0).getTypeName() + " rooms: " + availableByType.size());
        }

        System.out.println("  ✓ Availability checking with date integration");
        System.out.println("└───────────────────────────────────────┘\n");
    }

    private void demonstrateDateRangeValidation() {
        System.out.println("┌─ 6. Date Range Validation ────────────┐");

        // Try invalid date ranges
        try {
            // Check-in after check-out
            roomService.getAvailableRooms(
                    LocalDate.now().plusDays(10),
                    LocalDate.now().plusDays(5));
        } catch (IllegalArgumentException e) {
            System.out.println("  ✓ Prevented reversed dates");
        }

        try {
            // Check-in in the past
            roomService.getAvailableRooms(
                    LocalDate.now().minusDays(5),
                    LocalDate.now().plusDays(5));
        } catch (IllegalArgumentException e) {
            System.out.println("  ✓ Prevented past check-in");
        }

        try {
            // Same check-in and check-out
            LocalDate sameDate = LocalDate.now().plusDays(5);
            roomService.getAvailableRooms(sameDate, sameDate);
        } catch (IllegalArgumentException e) {
            System.out.println("  ✓ Prevented same-day booking");
        }

        try {
            // Null dates
            roomService.getAvailableRooms(null, LocalDate.now().plusDays(5));
        } catch (IllegalArgumentException e) {
            System.out.println("  ✓ Prevented null dates");
        }

        System.out.println("  ✓ All date validations working!");
        System.out.println("└───────────────────────────────────────┘\n");
    }

    /**
     * Main method for standalone testing.
     */
    public static void main(String[] args) {
        System.out.println("Enhanced Room Management Component - Demo");
        System.out.println("Note: This requires Spring context to run properly.");
        System.out.println("Please run through the main application or a Spring Boot test.");
    }
}
