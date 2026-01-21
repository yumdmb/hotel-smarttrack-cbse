package com.hotel.smarttrack.guest;

import com.hotel.smarttrack.entity.Guest;
import com.hotel.smarttrack.service.GuestService;
import org.osgi.service.component.annotations.*;

import java.util.List;
import java.util.Optional;

@Component(service = GuestService.class, immediate = true)
public class GuestManagerImpl implements GuestService {

    private final GuestRepository repo = new GuestRepository();

    @Activate
    public void activate() {
        System.out.println("==============================================");
        System.out.println("[GuestManager] Bundle ACTIVATED ✅");
        System.out.println("  - Service Registered: GuestService");
        System.out.println("  - UC1  Manage Guest Records (CRUD)");
        System.out.println("  - UC2  Search Guest Profiles");
        System.out.println("  - UC4  Manage Guest Status (Active/Inactive/Blacklisted)");
        System.out.println("==============================================");

        // seed 1 safe guest (only using known setters from Guest entity)
        Guest g = new Guest();
        g.setName("Seed Guest");
        g.setEmail("seed@guest.com");
        g.setPhone("0100000000");
        g.setIdentificationNumber("SEED-001");
        g.setStatus("ACTIVE");
        repo.save(g);

        System.out.println("[GuestManager] Seed: created 1 guest ✅");
    }

    @Deactivate
    public void deactivate() {
        System.out.println("[GuestManager] Deactivated");
    }

    // ================= UC1: CRUD =================

    @Override
    public Guest createGuest(String name, String email, String phone, String identificationNumber) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name required");

        Guest g = new Guest();
        g.setName(name);
        g.setEmail(email);
        g.setPhone(phone);
        g.setIdentificationNumber(identificationNumber);
        g.setStatus("ACTIVE");

        return repo.save(g);
    }

    @Override
    public Guest updateGuest(Guest guest) {
        if (guest == null || guest.getGuestId() == null) {
            throw new IllegalArgumentException("Guest/guestId required");
        }

        // 必须存在才允许 update（像 Reservation modify 那样）
        repo.findById(guest.getGuestId())
                .orElseThrow(() -> new IllegalArgumentException("Guest not found: " + guest.getGuestId()));

        return repo.save(guest);
    }

    @Override
    public Optional<Guest> getGuestById(Long guestId) {
        return repo.findById(guestId);
    }

    @Override
    public List<Guest> getAllGuests() {
        return repo.findAll();
    }

    // ================= UC2: Search =================

    @Override
    public List<Guest> searchGuests(String searchTerm) {
        return repo.search(searchTerm);
    }

    @Override
    public Optional<Guest> findByEmail(String email) {
        return repo.findByEmail(email);
    }

    @Override
    public Optional<Guest> findByPhone(String phone) {
        return repo.findByPhone(phone);
    }

    @Override
    public Optional<Guest> findByIdentificationNumber(String identificationNumber) {
        return repo.findByIdentificationNumber(identificationNumber);
    }

    // ================= UC4: Status =================

    @Override
    public void deactivateGuest(Long guestId, String justification) {
        Guest g = repo.findById(guestId)
                .orElseThrow(() -> new IllegalArgumentException("Guest not found: " + guestId));

        g.setStatus("INACTIVE");
        repo.save(g);
    }

    @Override
    public void blacklistGuest(Long guestId, String justification) {
        Guest g = repo.findById(guestId)
                .orElseThrow(() -> new IllegalArgumentException("Guest not found: " + guestId));

        g.setStatus("BLACKLISTED");
        repo.save(g);
    }

    @Override
    public void reactivateGuest(Long guestId) {
        Guest g = repo.findById(guestId)
                .orElseThrow(() -> new IllegalArgumentException("Guest not found: " + guestId));

        g.setStatus("ACTIVE");
        repo.save(g);
    }

    @Override
    public List<Guest> getGuestsByStatus(String status) {
        return repo.findByStatus(status);
    }
}
