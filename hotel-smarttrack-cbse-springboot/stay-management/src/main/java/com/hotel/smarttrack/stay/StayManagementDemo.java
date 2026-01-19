package com.hotel.smarttrack.stay;

import com.hotel.smarttrack.entity.IncidentalCharge;
import com.hotel.smarttrack.entity.Stay;
import com.hotel.smarttrack.service.StayService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * StayManagementDemo - Demonstrates Stay Management functionality.
 * Runs as part of the Spring Boot application startup.
 * 
 * NOTE: Disabled - Using StayManagementConsole for interactive terminal UI
 * instead.
 * To enable demo mode, uncomment @Component below.
 * 
 * @author Elvis Sawing
 */
// @Component // Disabled - using StayManagementConsole instead
@Order(30) // Run after Room and Guest demos
public class StayManagementDemo implements CommandLineRunner {

    private final StayService stayService;

    public StayManagementDemo(StayService stayService) {
        this.stayService = stayService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n========================================");
        System.out.println("   STAY MANAGEMENT DEMO (Elvis)");
        System.out.println("========================================\n");

        // Note: This demo assumes guests and rooms exist from other demos
        // For standalone testing, prerequisites must be set up first

        try {
            // Demo 1: Walk-in check-in (using guest ID 1, room ID 1)
            System.out.println("--- Demo 1: Walk-in Check-In ---");
            Stay walkInStay = stayService.checkInWalkIn(1L, 1L);
            System.out.println("Created stay: " + walkInStay);

            // Demo 2: Record incidental charges
            System.out.println("\n--- Demo 2: Record Incidental Charges ---");
            IncidentalCharge c1 = stayService.recordCharge(
                    walkInStay.getStayId(), "F&B", "Room service dinner", BigDecimal.valueOf(45.00));
            IncidentalCharge c2 = stayService.recordCharge(
                    walkInStay.getStayId(), "Minibar", "Beverages", BigDecimal.valueOf(18.50));
            IncidentalCharge c3 = stayService.recordCharge(
                    walkInStay.getStayId(), "Laundry", "Express dry cleaning", BigDecimal.valueOf(25.00));
            System.out.println("Total charges recorded: 3");

            // Demo 3: Get charges for stay
            System.out.println("\n--- Demo 3: View Stay Charges ---");
            var charges = stayService.getChargesForStay(walkInStay.getStayId());
            System.out.println("Charges count: " + charges.size());
            for (IncidentalCharge charge : charges) {
                System.out.println("  - " + charge.getServiceType() + ": $" + charge.getAmount());
            }

            // Demo 4: Get outstanding balance
            System.out.println("\n--- Demo 4: Outstanding Balance ---");
            BigDecimal balance = stayService.getOutstandingBalance(walkInStay.getStayId());
            System.out.println("Total outstanding balance: $" + balance);

            // Demo 5: Query active stays
            System.out.println("\n--- Demo 5: Active Stays ---");
            var activeStays = stayService.getActiveStays();
            System.out.println("Active stays count: " + activeStays.size());

            // Demo 6: Check-out
            System.out.println("\n--- Demo 6: Check-Out ---");
            stayService.checkOutGuest(walkInStay.getStayId());
            System.out.println("Check-out completed successfully");

            // Demo 7: Verify active stays after check-out
            System.out.println("\n--- Demo 7: Verify Check-Out ---");
            var activeStaysAfter = stayService.getActiveStays();
            System.out.println("Active stays count after check-out: " + activeStaysAfter.size());

            // Demo 8: Guest stay history
            System.out.println("\n--- Demo 8: Guest Stay History ---");
            var history = stayService.getGuestStayHistory(1L);
            System.out.println("Guest 1 stay history count: " + history.size());

        } catch (Exception e) {
            System.out.println("Demo note: " + e.getMessage());
            System.out.println("(This may occur if prerequisites like guests/rooms are not set up)");
        }

        System.out.println("\n========================================");
        System.out.println("   STAY MANAGEMENT DEMO COMPLETE");
        System.out.println("========================================\n");
    }
}
