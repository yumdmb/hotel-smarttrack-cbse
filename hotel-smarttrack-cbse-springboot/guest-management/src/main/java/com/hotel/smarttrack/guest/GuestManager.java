package com.hotel.smarttrack.guest;

import com.hotel.smarttrack.entity.Guest;
import com.hotel.smarttrack.repository.GuestRepository;
import com.hotel.smarttrack.service.GuestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GuestManager implements GuestService {

    private final GuestRepository guestRepository;

    public GuestManager(GuestRepository guestRepository) {
        this.guestRepository = guestRepository;
    }

    @Override
    public Guest createGuest(String name, String email, String phone, String identificationNumber) {
        require(name, "Name");
        require(email, "Email");
        require(phone, "Phone");
        require(identificationNumber, "Identification Number");

        validateEmail(email);

        String normalizedEmail = normalizeEmail(email);
        String normalizedId = identificationNumber.trim();

        if (guestRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email already exists.");
        }
        if (guestRepository.existsByIdentificationNumber(normalizedId)) {
            throw new IllegalArgumentException("Identification Number already exists.");
        }

        Guest g = new Guest();
        g.setName(name.trim());
        g.setEmail(normalizedEmail);
        g.setPhone(phone.trim());
        g.setIdentificationNumber(normalizedId);

        g.setStatus("ACTIVE");
        g.setStatusJustification(null);

        return guestRepository.saveAndFlush(g);
    }

    @Override
    public Guest updateGuest(Guest guest) {
        if (guest == null || guest.getGuestId() == null) {
            throw new IllegalArgumentException("Guest ID is required for update.");
        }

        Guest existing = guestRepository.findById(guest.getGuestId())
                .orElseThrow(() -> new IllegalArgumentException("Guest Not Found"));

        // name
        if (notBlank(guest.getName())) {
            existing.setName(guest.getName().trim());
        }

        // email (format + normalize + uniqueness)
        if (notBlank(guest.getEmail())) {
            validateEmail(guest.getEmail());
            String newEmail = normalizeEmail(guest.getEmail());
            String currentEmail = normalizeEmail(existing.getEmail());

            if (currentEmail == null || !currentEmail.equals(newEmail)) {
                if (guestRepository.existsByEmail(newEmail)) {
                    throw new IllegalArgumentException("Email already exists.");
                }
            }
            existing.setEmail(newEmail);
        }

        // phone
        if (notBlank(guest.getPhone())) {
            existing.setPhone(guest.getPhone().trim());
        }

        // identification number (uniqueness)
        if (notBlank(guest.getIdentificationNumber())) {
            String newId = guest.getIdentificationNumber().trim();
            String currentId = existing.getIdentificationNumber();

            if (currentId == null || !currentId.equals(newId)) {
                if (guestRepository.existsByIdentificationNumber(newId)) {
                    throw new IllegalArgumentException("Identification Number already exists.");
                }
            }
            existing.setIdentificationNumber(newId);
        }

        if (notBlank(existing.getStatus())) {
            existing.setStatus(existing.getStatus().trim().toUpperCase());
        } else {
            existing.setStatus("ACTIVE");
        }

        return guestRepository.saveAndFlush(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Guest> getGuestById(Long guestId) {
        return guestRepository.findById(guestId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Guest> searchGuests(String searchTerm) {
        if (!notBlank(searchTerm))
            return List.of();
        return guestRepository.searchGuests(searchTerm.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Guest> getAllGuests() {
        return guestRepository.findAll();
    }

    @Override
    public void deactivateGuest(Long guestId, String justification) {
        setStatus(guestId, "INACTIVE", justification, "Deactivation justification is required.");
    }

    @Override
    public void blacklistGuest(Long guestId, String justification) {
        setStatus(guestId, "BLACKLISTED", justification, "Blacklist justification is required.");
    }

    @Override
    public void reactivateGuest(Long guestId) {
        Guest g = guestRepository.findById(guestId)
                .orElseThrow(() -> new IllegalArgumentException("Guest Not Found"));
        g.setStatus("ACTIVE");
        g.setStatusJustification(null);
        guestRepository.saveAndFlush(g);
    }

    // ---- helpers ----

    private void setStatus(Long guestId, String status, String justification, String err) {
        if (guestId == null)
            throw new IllegalArgumentException("Guest ID is required.");
        if (!notBlank(justification))
            throw new IllegalArgumentException(err);

        Guest g = guestRepository.findById(guestId)
                .orElseThrow(() -> new IllegalArgumentException("Guest Not Found"));

        g.setStatus(status);
        g.setStatusJustification(justification.trim());
        guestRepository.saveAndFlush(g);
    }

    private static void validateEmail(String email) {
        if (!notBlank(email))
            throw new IllegalArgumentException("Email is required.");
        String e = email.trim();
        int at = e.indexOf('@');
        if (at <= 0 || at == e.length() - 1) {
            throw new IllegalArgumentException("Invalid email format.");
        }
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private static void require(String v, String field) {
        if (!notBlank(v))
            throw new IllegalArgumentException(field + " is required.");
    }

    private static boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
