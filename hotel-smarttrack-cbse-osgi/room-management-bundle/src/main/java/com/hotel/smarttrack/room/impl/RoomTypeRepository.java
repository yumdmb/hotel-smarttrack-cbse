package com.hotel.smarttrack.room.impl;

import com.hotel.smarttrack.entity.RoomType;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-based RoomType Repository.
 * 
 * @author Eisraq Rejab (refactored for JDBC)
 */
public class RoomTypeRepository {

    private final DataSource dataSource;

    public RoomTypeRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public RoomType save(RoomType entity) {
        if (entity.getRoomTypeId() == null) {
            return insert(entity);
        } else {
            return update(entity);
        }
    }

    private RoomType insert(RoomType entity) {
        String sql = "INSERT INTO room_types (type_name, description, max_occupancy, base_price, tax_rate) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, entity.getTypeName());
            ps.setString(2, entity.getDescription());
            ps.setInt(3, entity.getMaxOccupancy());
            ps.setBigDecimal(4, entity.getBasePrice());
            ps.setBigDecimal(5, entity.getTaxRate() != null ? entity.getTaxRate() : new BigDecimal("0.10"));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    entity.setRoomTypeId(rs.getLong(1));
                }
            }
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert room type: " + e.getMessage(), e);
        }
    }

    private RoomType update(RoomType entity) {
        String sql = "UPDATE room_types SET type_name=?, description=?, max_occupancy=?, base_price=?, tax_rate=? WHERE room_type_id=?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, entity.getTypeName());
            ps.setString(2, entity.getDescription());
            ps.setInt(3, entity.getMaxOccupancy());
            ps.setBigDecimal(4, entity.getBasePrice());
            ps.setBigDecimal(5, entity.getTaxRate());
            ps.setLong(6, entity.getRoomTypeId());

            ps.executeUpdate();
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update room type: " + e.getMessage(), e);
        }
    }

    public Optional<RoomType> findById(Long id) {
        if (id == null)
            return Optional.empty();

        String sql = "SELECT * FROM room_types WHERE room_type_id = ?";

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
            throw new RuntimeException("Failed to find room type: " + e.getMessage(), e);
        }
    }

    public Optional<RoomType> findByTypeNameIgnoreCase(String typeName) {
        if (typeName == null)
            return Optional.empty();

        String sql = "SELECT * FROM room_types WHERE LOWER(type_name) = LOWER(?)";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, typeName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find room type by name: " + e.getMessage(), e);
        }
    }

    public List<RoomType> findAll() {
        String sql = "SELECT * FROM room_types ORDER BY room_type_id";
        List<RoomType> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all room types: " + e.getMessage(), e);
        }
    }

    public void delete(Long id) {
        if (id == null)
            return;

        String sql = "DELETE FROM room_types WHERE room_type_id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete room type: " + e.getMessage(), e);
        }
    }

    public boolean existsById(Long id) {
        return findById(id).isPresent();
    }

    public boolean existsByTypeNameIgnoreCase(String typeName) {
        return findByTypeNameIgnoreCase(typeName).isPresent();
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM room_types";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count room types: " + e.getMessage(), e);
        }
    }

    private RoomType mapRow(ResultSet rs) throws SQLException {
        RoomType rt = new RoomType();
        rt.setRoomTypeId(rs.getLong("room_type_id"));
        rt.setTypeName(rs.getString("type_name"));
        rt.setDescription(rs.getString("description"));
        rt.setMaxOccupancy(rs.getInt("max_occupancy"));
        rt.setBasePrice(rs.getBigDecimal("base_price"));
        rt.setTaxRate(rs.getBigDecimal("tax_rate"));
        return rt;
    }
}
