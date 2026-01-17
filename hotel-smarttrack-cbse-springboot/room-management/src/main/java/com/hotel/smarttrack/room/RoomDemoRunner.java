package com.hotel.smarttrack.room;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.hotel.smarttrack.service.RoomService;

/**
 * Command-line runner to execute the Room Management Demo
 * when the Spring Boot application starts.
 */
@Component
public class RoomDemoRunner implements CommandLineRunner {

    private final RoomService roomService;

    public RoomDemoRunner(RoomService roomService) {
        this.roomService = roomService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n========================================");
        System.out.println("  HOTEL SMARTTRACK SYSTEM");
        System.out.println("========================================\n");

        // Run the demo
        RoomManagementDemo demo = new RoomManagementDemo(roomService);
        demo.runEnhancedDemo();

        System.out.println("========================================");
        System.out.println("  APPLICATION READY");
        System.out.println("========================================\n");
    }
}
