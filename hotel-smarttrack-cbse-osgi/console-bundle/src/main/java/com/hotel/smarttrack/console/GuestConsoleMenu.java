package com.hotel.smarttrack.console;

import com.hotel.smarttrack.entity.Guest;
import com.hotel.smarttrack.service.GuestService;

import java.util.List;
import java.util.Optional;

/**
 * Guest Management Console Menu.
 * Provides CRUD operations for guests via terminal UI.
 */
public class GuestConsoleMenu {

    private final GuestService guestService;
    private final ConsoleInputHelper input;

    public GuestConsoleMenu(GuestService guestService, ConsoleInputHelper input) {
        this.guestService = guestService;
        this.input = input;
    }

    public void showMenu() {
        boolean running = true;
        while (running) {
            input.println("\n==============================");
            input.println("      GUEST MANAGEMENT        ");
            input.println("==============================");
            input.println("1. Create Guest");
            input.println("2. Update Guest");
            input.println("3. View Guest by ID");
            input.println("4. View All Guests");
            input.println("5. Search Guests");
            input.println("6. Deactivate Guest");
            input.println("7. Blacklist Guest");
            input.println("8. Reactivate Guest");
            input.println("0. Back to Main Menu");

            String choice = input.readLine("Choose: ");
            try {
                switch (choice) {
                    case "1" -> createGuest();
                    case "2" -> updateGuest();
                    case "3" -> viewGuestById();
                    case "4" -> viewAllGuests();
                    case "5" -> searchGuests();
                    case "6" -> deactivateGuest();
                    case "7" -> blacklistGuest();
                    case "8" -> reactivateGuest();
                    case "0" -> running = false;
                    default -> input.println("Invalid option.");
                }
            } catch (IllegalArgumentException ex) {
                input.println("❌ Error: " + ex.getMessage());
            } catch (Exception ex) {
                input.println("❌ Unexpected error: " + ex.getMessage());
            }
        }
    }

    private void createGuest() {
        String name = input.readLine("Name: ");
        String email = input.readLine("Email: ");
        String phone = input.readLine("Phone: ");
        String idNo = input.readLine("Identification Number: ");

        Guest g = guestService.createGuest(name, email, phone, idNo);
        input.println("✅ Created: " + g);
    }

    private void updateGuest() {
        Long id = input.readLong("Guest ID to update: ");

        Optional<Guest> opt = guestService.getGuestById(id);
        if (opt.isEmpty()) {
            input.println("Guest Not Found");
            return;
        }

        Guest existing = opt.get();
        input.println("Current: " + existing);
        input.println("Leave blank to keep current value.");

        String name = input.readLine("New Name: ");
        String email = input.readLine("New Email: ");
        String phone = input.readLine("New Phone: ");
        String idNo = input.readLine("New Identification Number: ");

        if (ConsoleInputHelper.notBlank(name)) existing.setName(name.trim());
        if (ConsoleInputHelper.notBlank(email)) existing.setEmail(email.trim());
        if (ConsoleInputHelper.notBlank(phone)) existing.setPhone(phone.trim());
        if (ConsoleInputHelper.notBlank(idNo)) existing.setIdentificationNumber(idNo.trim());

        Guest saved = guestService.updateGuest(existing);
        input.println("✅ Updated: " + saved);
    }

    private void viewGuestById() {
        Long id = input.readLong("Guest ID: ");
        Optional<Guest> guest = guestService.getGuestById(id);
        input.println(guest.map(Object::toString).orElse("Guest Not Found"));
    }

    private void viewAllGuests() {
        List<Guest> list = guestService.getAllGuests();
        if (list.isEmpty()) {
            input.println("No guests.");
            return;
        }
        input.println("\n--- All Guests ---");
        list.forEach(g -> input.println(g.toString()));
    }

    private void searchGuests() {
        String term = input.readLine("Search term (name/email/phone/ID): ");
        List<Guest> results = guestService.searchGuests(term);
        if (results.isEmpty()) {
            input.println("No guest found");
            return;
        }
        results.forEach(g -> input.println(g.toString()));
    }

    private void deactivateGuest() {
        Long id = input.readLong("Guest ID to deactivate: ");
        String reason = input.readLine("Reason/Justification: ");
        guestService.deactivateGuest(id, reason);
        input.println("✅ Guest deactivated.");
    }

    private void blacklistGuest() {
        Long id = input.readLong("Guest ID to blacklist: ");
        String reason = input.readLine("Reason/Justification: ");
        guestService.blacklistGuest(id, reason);
        input.println("✅ Guest blacklisted.");
    }

    private void reactivateGuest() {
        Long id = input.readLong("Guest ID to reactivate: ");
        guestService.reactivateGuest(id);
        input.println("✅ Guest reactivated.");
    }
}
