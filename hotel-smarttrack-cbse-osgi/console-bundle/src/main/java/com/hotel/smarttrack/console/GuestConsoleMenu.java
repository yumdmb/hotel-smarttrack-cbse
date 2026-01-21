package com.hotel.smarttrack.console;

import com.hotel.smarttrack.entity.Guest;
import com.hotel.smarttrack.service.GuestService;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Guest Management Console Menu.
 * Provides CRUD operations for guests via terminal UI.
 */
public class GuestConsoleMenu {

    private final GuestService guestService;

    public GuestConsoleMenu(GuestService guestService) {
        this.guestService = guestService;
    }

    public void showMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("\n==============================");
            System.out.println("      GUEST MANAGEMENT        ");
            System.out.println("==============================");
            System.out.println("1. Create Guest");
            System.out.println("2. Update Guest");
            System.out.println("3. View Guest by ID");
            System.out.println("4. View All Guests");
            System.out.println("5. Search Guests");
            System.out.println("6. Deactivate Guest");
            System.out.println("7. Blacklist Guest");
            System.out.println("8. Reactivate Guest");
            System.out.println("0. Back to Main Menu");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> createGuest(scanner);
                    case "2" -> updateGuest(scanner);
                    case "3" -> viewGuestById(scanner);
                    case "4" -> viewAllGuests();
                    case "5" -> searchGuests(scanner);
                    case "6" -> deactivateGuest(scanner);
                    case "7" -> blacklistGuest(scanner);
                    case "8" -> reactivateGuest(scanner);
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

    private void createGuest(Scanner scanner) {
        System.out.print("Name: ");
        String name = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Phone: ");
        String phone = scanner.nextLine();
        System.out.print("Identification Number: ");
        String idNo = scanner.nextLine();

        Guest g = guestService.createGuest(name, email, phone, idNo);
        System.out.println("✅ Created: " + g);
    }

    private void updateGuest(Scanner scanner) {
        Long id = readLong(scanner, "Guest ID to update: ");

        Optional<Guest> opt = guestService.getGuestById(id);
        if (opt.isEmpty()) {
            System.out.println("Guest Not Found");
            return;
        }

        Guest existing = opt.get();
        System.out.println("Current: " + existing);
        System.out.println("Leave blank to keep current value.");

        System.out.print("New Name: ");
        String name = scanner.nextLine();
        System.out.print("New Email: ");
        String email = scanner.nextLine();
        System.out.print("New Phone: ");
        String phone = scanner.nextLine();
        System.out.print("New Identification Number: ");
        String idNo = scanner.nextLine();

        if (notBlank(name)) existing.setName(name.trim());
        if (notBlank(email)) existing.setEmail(email.trim());
        if (notBlank(phone)) existing.setPhone(phone.trim());
        if (notBlank(idNo)) existing.setIdentificationNumber(idNo.trim());

        Guest saved = guestService.updateGuest(existing);
        System.out.println("✅ Updated: " + saved);
    }

    private void viewGuestById(Scanner scanner) {
        Long id = readLong(scanner, "Guest ID: ");
        Optional<Guest> guest = guestService.getGuestById(id);
        System.out.println(guest.map(Object::toString).orElse("Guest Not Found"));
    }

    private void viewAllGuests() {
        List<Guest> list = guestService.getAllGuests();
        if (list.isEmpty()) {
            System.out.println("No guests.");
            return;
        }
        System.out.println("\n--- All Guests ---");
        list.forEach(System.out::println);
    }

    private void searchGuests(Scanner scanner) {
        System.out.print("Search term (name/email/phone/ID): ");
        String term = scanner.nextLine();
        List<Guest> results = guestService.searchGuests(term);
        if (results.isEmpty()) {
            System.out.println("No guest found");
            return;
        }
        results.forEach(System.out::println);
    }

    private void deactivateGuest(Scanner scanner) {
        Long id = readLong(scanner, "Guest ID to deactivate: ");
        System.out.print("Reason/Justification: ");
        String reason = scanner.nextLine();
        guestService.deactivateGuest(id, reason);
        System.out.println("✅ Guest deactivated.");
    }

    private void blacklistGuest(Scanner scanner) {
        Long id = readLong(scanner, "Guest ID to blacklist: ");
        System.out.print("Reason/Justification: ");
        String reason = scanner.nextLine();
        guestService.blacklistGuest(id, reason);
        System.out.println("✅ Guest blacklisted.");
    }

    private void reactivateGuest(Scanner scanner) {
        Long id = readLong(scanner, "Guest ID to reactivate: ");
        guestService.reactivateGuest(id);
        System.out.println("✅ Guest reactivated.");
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

    private static boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
