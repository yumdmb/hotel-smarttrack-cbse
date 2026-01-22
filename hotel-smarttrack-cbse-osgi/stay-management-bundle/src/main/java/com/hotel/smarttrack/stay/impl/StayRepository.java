package com.hotel.smarttrack.stay.impl;

import com.hotel.smarttrack.entity.Stay;
import com.hotel.smarttrack.service.GuestService;
import com.hotel.smarttrack.service.ReservationService;
import com.hotel.smarttrack.service.RoomService;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-based Stay Repository.
 * 
 * @author Elvis Sawing (refactored for JDBC)
 */
public class StayRepository {

    private final DataSource dataSource;
    private final GuestService guestService;
    private final RoomService roomService;
    private final ReservationService reservationService;

    public StayRepository(DataSource dataSource, GuestService guestService,
            RoomService roomService, ReservationService reservationService) {
        this.dataSource = dataSource;
        this.guestService = guestService;
        this.roomService = roomService;
        this.reservationService = reservationService;
    }

    public Stay save(Stay entity) {
        if (entity.getStayId() == null) {
            return insert(entity);
        } else {
            return update(entity);
        }
    }

    private Stay insert(Stay entity) {
        String sql = "INSERT INTO stays (reservation_id, guest_id, room_id, check_in_time, check_out_time, status, key_card_number) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setObject(1, entity.getReservation() != null ? entity.getReservation().getReservationId() : null);
            ps.setObject(2, entity.getGuest() != null ? entity.getGuest().getGuestId() : null);
            ps.setObject(3, entity.getRoom() != null ? entity.getRoom().getRoomId() : null);
            ps.setTimestamp(4, entity.getCheckInTime() != null ? Timestamp.valueOf(entity.getCheckInTime()) : null);
            ps.setTimestamp(5, entity.getCheckOutTime() != null ? Timestamp.valueOf(entity.getCheckOutTime()) : null);
            ps.setString(6, entity.getStatus() != null ? entity.getStatus() : "CHECKED_IN");
            ps.setString(7, entity.getKeyCardNumber());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    entity.setStayId(rs.getLong(1));
                }
            }
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert stay: " + e.getMessage(), e);
        }
    }

    private Stay update(Stay entity) {
        String sql = "UPDATE stays SET reservation_id=?, guest_id=?, room_id=?, check_in_time=?, check_out_time=?, status=?, key_card_number=? WHERE stay_id=?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, entity.getReservation() != null ? entity.getReservation().getReservationId() : null);
            ps.setObject(2, entity.getGuest() != null ? entity.getGuest().getGuestId() : null);
            ps.setObject(3, entity.getRoom() != null ? entity.getRoom().getRoomId() : null);
            ps.setTimestamp(4, entity.getCheckInTime() != null ? Timestamp.valueOf(entity.getCheckInTime()) : null);
            ps.setTimestamp(5, entity.getCheckOutTime() != null ? Timestamp.valueOf(entity.getCheckOutTime()) : null);
            ps.setString(6, entity.getStatus());
            ps.setString(7, entity.getKeyCardNumber());
            ps.setLong(8, entity.getStayId());

            ps.executeUpdate();
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update stay: " + e.getMessage(), e);
        }
    }

    public Optional<Stay> findById(Long id) {
        if (id == null)
            return Optional.empty();

        String sql = "SELECT * FROM stays WHERE stay_id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find stay: " + e.getMessage(), e);
        }
    }

    public List<Stay> findAll() {
        String sql = "SELECT * FROM stays ORDER BY stay_id";
        List<Stay> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all stays: " + e.getMessage(), e);
        }
    }

    public void delete(Long id) {
        if (id == null)
            return;

        String sql = "DELETE FROM stays WHERE stay_id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete stay: " + e.getMessage(), e);
        }
    }

    public boolean existsById(Long id) {
        return findById(id).isPresent();
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM stays";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count stays: " + e.getMessage(), e);
        }
    }

    private Stay mapRow(ResultSet rs) throws SQLException {
        Stay stay = new Stay();
        stay.setStayId(rs.getLong("stay_id"));

        // Load Reservation
        long reservationId = rs.getLong("reservation_id");
        if (!rs.wasNull()) {
            reservationService.getReservationById(reservationId).ifPresent(stay::setReservation);
        }

        // Load Guest
        long guestId = rs.getLong("guest_id");
        if (!rs.wasNull()) {
            guestService.getGuestById(guestId).ifPresent(stay::setGuest);
        }

        // Load Room
        long roomId = rs.getLong("room_id");
        if (!rs.wasNull()) {
            roomService.getRoomById(roomId).ifPresent(stay::setRoom);
        }

        Timestamp checkIn = rs.getTimestamp("check_in_time");
        if (checkIn != null) {
            stay.setCheckInTime(checkIn.toLocalDateTime());
        }

        Timestamp checkOut = rs.getTimestamp("check_out_time");
        if (checkOut != null) {
            stay.setCheckOutTime(checkOut.toLocalDateTime());
        }

        stay.setStatus(rs.getString("status"));
        stay.setKeyCardNumber(rs.getString("key_card_number"));

        return stay;
    }
}
