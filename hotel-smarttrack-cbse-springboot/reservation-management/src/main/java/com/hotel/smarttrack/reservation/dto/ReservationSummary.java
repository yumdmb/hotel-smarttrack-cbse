package com.hotel.smarttrack.reservation.dto;

import java.time.LocalDate;

public class ReservationSummary {

    private Long reservationId;
    private Long guestId;
    private Long roomId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private String status; // Created / Cancelled

    public ReservationSummary() {}

    public ReservationSummary(Long reservationId, Long guestId, Long roomId,
                              LocalDate checkIn, LocalDate checkOut, String status) {
        this.reservationId = reservationId;
        this.guestId = guestId;
        this.roomId = roomId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.status = status;
    }

    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }

    public Long getGuestId() { return guestId; }
    public void setGuestId(Long guestId) { this.guestId = guestId; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public LocalDate getCheckIn() { return checkIn; }
    public void setCheckIn(LocalDate checkIn) { this.checkIn = checkIn; }

    public LocalDate getCheckOut() { return checkOut; }
    public void setCheckOut(LocalDate checkOut) { this.checkOut = checkOut; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "ReservationSummary{" +
                "reservationId=" + reservationId +
                ", guestId=" + guestId +
                ", roomId=" + roomId +
                ", checkIn=" + checkIn +
                ", checkOut=" + checkOut +
                ", status='" + status + '\'' +
                '}';
    }
}
