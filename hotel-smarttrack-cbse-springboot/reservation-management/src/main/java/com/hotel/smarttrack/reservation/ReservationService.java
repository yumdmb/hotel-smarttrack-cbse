package com.hotel.smarttrack.reservation;

import com.hotel.smarttrack.reservation.dto.ReservationSummary;

import java.time.LocalDate;
import java.util.List;

public interface ReservationService {

    List<ReservationSummary> listAll();

    ReservationSummary create(Long guestId, Long roomId, LocalDate checkIn, LocalDate checkOut);

    boolean cancel(Long reservationId);
}
