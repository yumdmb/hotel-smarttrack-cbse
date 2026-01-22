package com.hotel.smarttrack.guest.impl;

import com.hotel.smarttrack.entity.Guest;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-based Guest Repository.
 * 
 * Replaces in-memory ConcurrentHashMap storage with H2 database operations.
 * Uses DataSource injected from Karaf's pax-jdbc.
 */
public class GuestRepository {

    private final DataSource dataSource;

    public GuestRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Save or update a guest.
     */
    public Guest save(Guest g) {
        if (g == null)
            throw new IllegalArgumentException("Guest is null");

        if (g.getGuestId() == null) {
            return insert(g);
        } else {
            return update(g);
        }
    }

    private Guest insert(Guest g) {
        String sql = "INSERT INTO guests (name, email, phone, identification_number, status, status_justification) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, g.getName());
            ps.setString(2, g.getEmail());
            ps.setString(3, g.getPhone());
            ps.setString(4, g.getIdentificationNumber());
            ps.setString(5, g.getStatus() != null ? g.getStatus() : "ACTIVE");
            ps.setString(6, g.getStatusJustification());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    g.setGuestId(rs.getLong(1));
                }
            }
            return g;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert guest: " + e.getMessage(), e);
        }
    }

    private Guest update(Guest g) {
        String sql = "UPDATE guests SET name=?, email=?, phone=?, identification_number=?, status=?, status_justification=? WHERE guest_id=?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, g.getName());
            ps.setString(2, g.getEmail());
            ps.setString(3, g.getPhone());
            ps.setString(4, g.getIdentificationNumber());
            ps.setString(5, g.getStatus());
            ps.setString(6, g.getStatusJustification());
            ps.setLong(7, g.getGuestId());

            ps.executeUpdate();
            return g;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update guest: " + e.getMessage(), e);
        }
    }

    public Optional<Guest> findById(Long id) {
        if (id == null)
            return Optional.empty();

        String sql = "SELECT * FROM guests WHERE guest_id = ?";

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
            throw new RuntimeException("Failed to find guest: " + e.getMessage(), e);
        }
    }

    public List<Guest> findAll() {
        String sql = "SELECT * FROM guests ORDER BY guest_id";
        List<Guest> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all guests: " + e.getMessage(), e);
        }
    }

    public Optional<Guest> findByEmail(String email) {
        if (email == null)
            return Optional.empty();

        String sql = "SELECT * FROM guests WHERE LOWER(email) = LOWER(?)";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find guest by email: " + e.getMessage(), e);
        }
    }

    public Optional<Guest> findByPhone(String phone) {
        if (phone == null)
            return Optional.empty();

        String sql = "SELECT * FROM guests WHERE phone = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, phone);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find guest by phone: " + e.getMessage(), e);
        }
    }

    public Optional<Guest> findByIdentificationNumber(String idNo) {
        if (idNo == null)
            return Optional.empty();

        String sql = "SELECT * FROM guests WHERE LOWER(identification_number) = LOWER(?)";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, idNo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find guest by identification: " + e.getMessage(), e);
        }
    }

    public List<Guest> search(String term) {
        if (term == null || term.isBlank())
            return List.of();

        String sql = "SELECT * FROM guests WHERE " +
                "LOWER(name) LIKE LOWER(?) OR " +
                "LOWER(email) LIKE LOWER(?) OR " +
                "phone LIKE ? OR " +
                "LOWER(identification_number) LIKE LOWER(?)";

        List<Guest> result = new ArrayList<>();
        String pattern = "%" + term.trim() + "%";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            return result;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to search guests: " + e.getMessage(), e);
        }
    }

    public List<Guest> findByStatus(String status) {
        if (status == null)
            return List.of();

        String sql = "SELECT * FROM guests WHERE UPPER(status) = UPPER(?)";
        List<Guest> result = new ArrayList<>();

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
            throw new RuntimeException("Failed to find guests by status: " + e.getMessage(), e);
        }
    }

    public void delete(Long id) {
        if (id == null)
            return;

        String sql = "DELETE FROM guests WHERE guest_id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete guest: " + e.getMessage(), e);
        }
    }

    /**
     * Map a ResultSet row to a Guest entity.
     */
    private Guest mapRow(ResultSet rs) throws SQLException {
        Guest g = new Guest();
        g.setGuestId(rs.getLong("guest_id"));
        g.setName(rs.getString("name"));
        g.setEmail(rs.getString("email"));
        g.setPhone(rs.getString("phone"));
        g.setIdentificationNumber(rs.getString("identification_number"));
        g.setStatus(rs.getString("status"));
        g.setStatusJustification(rs.getString("status_justification"));
        return g;
    }
}
