package com.hotel.smarttrack.entity;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Stay entity representing active/past guest stays (check-in/check-out
 * records).
 * Part of Base Library (CBSE Rule 1) - shared across all OSGi bundles.
 * 
 * <p>
 * This is a plain POJO without JPA annotations for OSGi compatibility.
 * </p>
 * 
 * @author Hotel SmartTrack Team
 */
public class Stay {

    private Long stayId;
    private Reservation reservation;
    private Guest guest;
    private Room room;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    /**
     * Stay status: CHECKED_IN, CHECKED_OUT
     */
    private String status;

    private String keyCardNumber;

    // ============ Constructors ============

    public Stay() {
    }

    public Stay(Long stayId, Reservation reservation, Guest guest, Room room,
            LocalDateTime checkInTime, LocalDateTime checkOutTime, String status, String keyCardNumber) {
        this.stayId = stayId;
        this.reservation = reservation;
        this.guest = guest;
        this.room = room;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.status = status;
        this.keyCardNumber = keyCardNumber;
    }

    // ============ Getters and Setters ============

    public Long getStayId() {
        return stayId;
    }

    public void setStayId(Long stayId) {
        this.stayId = stayId;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getKeyCardNumber() {
        return keyCardNumber;
    }

    public void setKeyCardNumber(String keyCardNumber) {
        this.keyCardNumber = keyCardNumber;
    }

    // ============ Object Methods ============

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Stay stay = (Stay) o;
        return Objects.equals(stayId, stay.stayId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stayId);
    }

    @Override
    public String toString() {
        return "Stay{stayId=" + stayId + ", status='" + status + "'}";
    }
}
