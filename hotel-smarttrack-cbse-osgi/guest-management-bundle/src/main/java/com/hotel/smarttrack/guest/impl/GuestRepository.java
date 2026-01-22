package com.hotel.smarttrack.guest.impl;

import com.hotel.smarttrack.entity.Guest;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class GuestRepository {

    private final Map<Long, Guest> guests = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    public Guest save(Guest g) {
        if (g == null)
            throw new IllegalArgumentException("Guest is null");

        if (g.getGuestId() == null) {
            g.setGuestId(idGen.getAndIncrement());
        }
        guests.put(g.getGuestId(), g);
        return g;
    }

    public Optional<Guest> findById(Long id) {
        if (id == null)
            return Optional.empty();
        return Optional.ofNullable(guests.get(id));
    }

    public List<Guest> findAll() {
        return new ArrayList<>(guests.values());
    }

    public Optional<Guest> findByEmail(String email) {
        if (email == null)
            return Optional.empty();
        for (Guest g : guests.values()) {
            if (g.getEmail() != null && email.equalsIgnoreCase(g.getEmail()))
                return Optional.of(g);
        }
        return Optional.empty();
    }

    public Optional<Guest> findByPhone(String phone) {
        if (phone == null)
            return Optional.empty();
        for (Guest g : guests.values()) {
            if (g.getPhone() != null && phone.equals(g.getPhone()))
                return Optional.of(g);
        }
        return Optional.empty();
    }

    public Optional<Guest> findByIdentificationNumber(String idNo) {
        if (idNo == null)
            return Optional.empty();
        for (Guest g : guests.values()) {
            if (g.getIdentificationNumber() != null && idNo.equalsIgnoreCase(g.getIdentificationNumber())) {
                return Optional.of(g);
            }
        }
        return Optional.empty();
    }

    public List<Guest> search(String term) {
        if (term == null || term.isBlank())
            return List.of();
        String t = term.trim().toLowerCase();

        List<Guest> out = new ArrayList<>();
        for (Guest g : guests.values()) {
            boolean match = (g.getName() != null && g.getName().toLowerCase().contains(t)) ||
                    (g.getEmail() != null && g.getEmail().toLowerCase().contains(t)) ||
                    (g.getPhone() != null && g.getPhone().toLowerCase().contains(t)) ||
                    (g.getIdentificationNumber() != null && g.getIdentificationNumber().toLowerCase().contains(t));
            if (match)
                out.add(g);
        }
        return out;
    }

    public List<Guest> findByStatus(String status) {
        if (status == null)
            return List.of();
        List<Guest> out = new ArrayList<>();
        for (Guest g : guests.values()) {
            if (g.getStatus() != null && status.equalsIgnoreCase(g.getStatus())) {
                out.add(g);
            }
        }
        return out;
    }

    public void delete(Long id) {
        if (id == null)
            return;
        guests.remove(id);
    }
}
