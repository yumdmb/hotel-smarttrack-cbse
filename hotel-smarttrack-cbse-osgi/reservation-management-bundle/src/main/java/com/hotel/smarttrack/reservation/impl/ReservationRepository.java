package com.hotel.smarttrack.reservation.impl;

import com.hotel.smarttrack.entity.Guest;
import com.hotel.smarttrack.entity.Reservation;
import com.hotel.smarttrack.entity.Room;
import com.hotel.smarttrack.entity.RoomType;
import com.hotel.smarttrack.service.GuestService;
import com.hotel.smarttrack.service.RoomService;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-based Reservation Repository.
 */
public class ReservationRepository {

    private final DataSource dataSource;
    private final GuestService guestService;
    private final RoomService roomService;

    public ReservationRepository(DataSource dataSource, GuestService guestService, RoomService roomService) {
        this.dataSource = dataSource;
        this.guestService = guestService;
        this.roomService = roomService;
    }

    public Reservation save(Reservation r) {
        if (r.getReservationId() == null) {
            return insert(r);
        } else {
            return update(r);
        }
    }

    private Reservation insert(Reservation r) {
        String sql = "INSERT INTO reservations (guest_id, room_type_id, assigned_room_id, check_in_date, check_out_date, number_of_guests, status, special_requests) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, r.getGuest() != null ? r.getGuest().getGuestId() : null);
            ps.setObject(2, r.getRoomType() != null ? r.getRoomType().getRoomTypeId() : null);
            ps.setObject(3, r.getAssignedRoom() != null ? r.getAssignedRoom().getRoomId() : null);
            ps.setDate(4, Date.valueOf(r.getCheckInDate()));
            ps.setDate(5, Date.valueOf(r.getCheckOutDate()));
            ps.setInt(6, r.getNumberOfGuests());
            ps.setString(7, r.getStatus() != null ? r.getStatus() : "RESERVED");
            ps.setString(8, r.getSpecialRequests());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    r.setReservationId(rs.getLong(1));
                }
            }
            return r;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert reservation: " + e.getMessage(), e);
        }
    }

    private Reservation update(Reservation r) {
        String sql = "UPDATE reservations SET guest_id=?, room_type_id=?, assigned_room_id=?, check_in_date=?, check_out_date=?, number_of_guests=?, status=?, special_requests=? WHERE reservation_id=?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, r.getGuest() != null ? r.getGuest().getGuestId() : null);
            ps.setObject(2, r.getRoomType() != null ? r.getRoomType().getRoomTypeId() : null);
            ps.setObject(3, r.getAssignedRoom() != null ? r.getAssignedRoom().getRoomId() : null);
            ps.setDate(4, Date.valueOf(r.getCheckInDate()));
            ps.setDate(5, Date.valueOf(r.getCheckOutDate()));
            ps.setInt(6, r.getNumberOfGuests());
            ps.setString(7, r.getStatus());
            ps.setString(8, r.getSpecialRequests());
            ps.setLong(9, r.getReservationId());

            ps.executeUpdate();
            return r;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update reservation: " + e.getMessage(), e);
        }
    }

    public Optional<Reservation> findById(Long id) {
        if (id == null)
            return Optional.empty();

        String sql = "SELECT * FROM reservations WHERE reservation_id = ?";

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
            throw new RuntimeException("Failed to find reservation: " + e.getMessage(), e);
        }
    }

    public List<Reservation> findAll() {
        String sql = "SELECT * FROM reservations ORDER BY reservation_id";
        List<Reservation> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all reservations: " + e.getMessage(), e);
        }
    }

    public List<Reservation> findByStatus(String status) {
        if (status == null)
            return List.of();

        String sql = "SELECT * FROM reservations WHERE UPPER(status) = UPPER(?)";
        List<Reservation> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find reservations by status: " + e.getMessage(), e);
        }
    }

    public List<Reservation> findByDateRange(LocalDate start, LocalDate end) {
        if (start == null || end == null)
            return List.of();

        String sql = "SELECT * FROM reservations WHERE check_out_date >= ? AND check_in_date <= ?";
        List<Reservation> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find reservations by date range: " + e.getMessage(), e);
        }
    }

    public void delete(Long id) {
        if (id == null)
            return;

        String sql = "DELETE FROM reservations WHERE reservation_id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete reservation: " + e.getMessage(), e);
        }
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM reservations";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count reservations: " + e.getMessage(), e);
        }
    }

    private Reservation mapRow(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setReservationId(rs.getLong("reservation_id"));

        // Load Guest
        long guestId = rs.getLong("guest_id");
        if (!rs.wasNull()) {
            guestService.getGuestById(guestId).ifPresent(r::setGuest);
        }

        // Load RoomType
        long roomTypeId = rs.getLong("room_type_id");
        if (!rs.wasNull()) {
            roomService.getRoomTypeById(roomTypeId).ifPresent(r::setRoomType);
        }

        // Load Assigned Room
        long assignedRoomId = rs.getLong("assigned_room_id");
        if (!rs.wasNull()) {
            roomService.getRoomById(assignedRoomId).ifPresent(r::setAssignedRoom);
        }

        r.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
        r.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
        r.setNumberOfGuests(rs.getInt("number_of_guests"));
        r.setStatus(rs.getString("status"));
        r.setSpecialRequests(rs.getString("special_requests"));

        return r;
    }
}