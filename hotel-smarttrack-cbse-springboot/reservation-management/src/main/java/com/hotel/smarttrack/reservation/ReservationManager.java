package com.hotel.smarttrack.reservation;

import com.hotel.smarttrack.reservation.dto.ReservationSummary;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ReservationManager implements ReservationService {

    private final List<ReservationSummary> store = new ArrayList<>();
    private final AtomicLong idSeq = new AtomicLong(1000);

    @Override
    public List<ReservationSummary> listAll() {
        return new ArrayList<>(store);
    }

    @Override
    public ReservationSummary create(Long guestId, Long roomId, LocalDate checkIn, LocalDate checkOut) {
        if (guestId == null || roomId == null) {
            throw new IllegalArgumentException("guestId/roomId cannot be null.");
        }
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("checkOut must be after checkIn.");
        }

        long newId = idSeq.incrementAndGet();
        ReservationSummary r = new ReservationSummary(newId, guestId, roomId, checkIn, checkOut, "Created");
        store.add(r);
        return r;
    }

    @Override
    public boolean cancel(Long reservationId) {
        Optional<ReservationSummary> opt = store.stream()
                .filter(r -> r.getReservationId().equals(reservationId))
                .findFirst();

        if (opt.isEmpty()) return false;

        opt.get().setStatus("Cancelled");
        return true;
    }
}
