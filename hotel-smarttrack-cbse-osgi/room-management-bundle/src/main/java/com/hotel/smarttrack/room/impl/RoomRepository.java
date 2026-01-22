package com.hotel.smarttrack.room.impl;

import com.hotel.smarttrack.entity.Room;
import com.hotel.smarttrack.entity.RoomType;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-based Room Repository.
 * 
 * @author Eisraq Rejab (refactored for JDBC)
 */
public class RoomRepository {

    private final DataSource dataSource;
    private final RoomTypeRepository roomTypeRepository;

    public RoomRepository(DataSource dataSource, RoomTypeRepository roomTypeRepository) {
        this.dataSource = dataSource;
        this.roomTypeRepository = roomTypeRepository;
    }

    public Room save(Room entity) {
        if (entity.getRoomId() == null) {
            return insert(entity);
        } else {
            return update(entity);
        }
    }

    private Room insert(Room entity) {
        String sql = "INSERT INTO rooms (room_number, floor_number, status, room_type_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, entity.getRoomNumber());
            ps.setInt(2, entity.getFloorNumber());
            ps.setString(3, entity.getStatus() != null ? entity.getStatus() : "AVAILABLE");
            ps.setLong(4, entity.getRoomType() != null ? entity.getRoomType().getRoomTypeId() : null);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    entity.setRoomId(rs.getLong(1));
                }
            }
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert room: " + e.getMessage(), e);
        }
    }

    private Room update(Room entity) {
        String sql = "UPDATE rooms SET room_number=?, floor_number=?, status=?, room_type_id=? WHERE room_id=?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, entity.getRoomNumber());
            ps.setInt(2, entity.getFloorNumber());
            ps.setString(3, entity.getStatus());
            ps.setLong(4, entity.getRoomType() != null ? entity.getRoomType().getRoomTypeId() : null);
            ps.setLong(5, entity.getRoomId());

            ps.executeUpdate();
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update room: " + e.getMessage(), e);
        }
    }

    public Optional<Room> findById(Long id) {
        if (id == null)
            return Optional.empty();

        String sql = "SELECT * FROM rooms WHERE room_id = ?";

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
            throw new RuntimeException("Failed to find room: " + e.getMessage(), e);
        }
    }

    public Optional<Room> findByRoomNumber(String roomNumber) {
        if (roomNumber == null)
            return Optional.empty();

        String sql = "SELECT * FROM rooms WHERE room_number = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, roomNumber);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find room by number: " + e.getMessage(), e);
        }
    }

    public List<Room> findAll() {
        String sql = "SELECT * FROM rooms ORDER BY room_id";
        List<Room> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all rooms: " + e.getMessage(), e);
        }
    }

    public List<Room> findByStatus(String status) {
        String sql = "SELECT * FROM rooms WHERE UPPER(status) = UPPER(?)";
        List<Room> result = new ArrayList<>();

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
            throw new RuntimeException("Failed to find rooms by status: " + e.getMessage(), e);
        }
    }

    public List<Room> findByFloorNumber(int floorNumber) {
        String sql = "SELECT * FROM rooms WHERE floor_number = ?";
        List<Room> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, floorNumber);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find rooms by floor: " + e.getMessage(), e);
        }
    }

    public List<Room> findByRoomTypeId(Long roomTypeId) {
        String sql = "SELECT * FROM rooms WHERE room_type_id = ?";
        List<Room> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, roomTypeId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find rooms by type: " + e.getMessage(), e);
        }
    }

    public List<Room> findAllAvailable() {
        return findByStatus("AVAILABLE");
    }

    public void delete(Long id) {
        if (id == null)
            return;

        String sql = "DELETE FROM rooms WHERE room_id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete room: " + e.getMessage(), e);
        }
    }

    public boolean existsById(Long id) {
        return findById(id).isPresent();
    }

    public boolean existsByRoomNumber(String roomNumber) {
        return findByRoomNumber(roomNumber).isPresent();
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM rooms";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count rooms: " + e.getMessage(), e);
        }
    }

    private Room mapRow(ResultSet rs) throws SQLException {
        Room room = new Room();
        room.setRoomId(rs.getLong("room_id"));
        room.setRoomNumber(rs.getString("room_number"));
        room.setFloorNumber(rs.getInt("floor_number"));
        room.setStatus(rs.getString("status"));

        // Load RoomType if present
        long roomTypeId = rs.getLong("room_type_id");
        if (!rs.wasNull()) {
            roomTypeRepository.findById(roomTypeId).ifPresent(room::setRoomType);
        }

        return room;
    }
}
