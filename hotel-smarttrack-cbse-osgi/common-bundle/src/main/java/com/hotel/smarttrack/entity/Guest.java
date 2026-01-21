package com.hotel.smarttrack.entity;

import java.util.Objects;

/**
 * Guest entity representing hotel guest information.
 * Part of Base Library (CBSE Rule 1) - shared across all OSGi bundles.
 * 
 * <p>
 * This is a plain POJO without JPA annotations for OSGi compatibility.
 * Each business bundle maintains its own in-memory repository.
 * </p>
 * 
 * @author Hotel SmartTrack Team
 */
public class Guest {

    private Long guestId;
    private String name;
    private String email;
    private String phone;
    private String identificationNumber;

    /**
     * Guest status: Active, Inactive, Blacklisted
     */
    private String status;

    /**
     * Justification for status changes (blacklist/deactivation reason)
     */
    private String statusJustification;

    // ============ Constructors ============

    public Guest() {
    }

    public Guest(Long guestId, String name, String email, String phone,
            String identificationNumber, String status, String statusJustification) {
        this.guestId = guestId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.identificationNumber = identificationNumber;
        this.status = status;
        this.statusJustification = statusJustification;
    }

    // ============ Getters and Setters ============

    public Long getGuestId() {
        return guestId;
    }

    public void setGuestId(Long guestId) {
        this.guestId = guestId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getIdentificationNumber() {
        return identificationNumber;
    }

    public void setIdentificationNumber(String identificationNumber) {
        this.identificationNumber = identificationNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusJustification() {
        return statusJustification;
    }

    public void setStatusJustification(String statusJustification) {
        this.statusJustification = statusJustification;
    }

    // ============ Object Methods ============

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Guest guest = (Guest) o;
        return Objects.equals(guestId, guest.guestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(guestId);
    }

    @Override
    public String toString() {
        return "Guest{guestId=" + guestId + ", name='" + name + "', email='" + email + "', status='" + status + "'}";
    }
}
