package com.hotel.smarttrack.stay.impl;

import com.hotel.smarttrack.entity.IncidentalCharge;
import com.hotel.smarttrack.entity.Stay;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * JDBC-based IncidentalCharge Repository.
 * 
 * @author Elvis Sawing (refactored for JDBC)
 */
public class IncidentalChargeRepository {

    private final DataSource dataSource;
    private final StayRepository stayRepository;

    public IncidentalChargeRepository(DataSource dataSource, StayRepository stayRepository) {
        this.dataSource = dataSource;
        this.stayRepository = stayRepository;
    }

    public IncidentalCharge save(IncidentalCharge entity) {
        if (entity.getChargeId() == null) {
            return insert(entity);
        } else {
            return update(entity);
        }
    }

    private IncidentalCharge insert(IncidentalCharge entity) {
        String sql = "INSERT INTO incidental_charges (stay_id, service_type, description, amount, charge_time) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setObject(1, entity.getStay() != null ? entity.getStay().getStayId() : null);
            ps.setString(2, entity.getServiceType());
            ps.setString(3, entity.getDescription());
            ps.setBigDecimal(4, entity.getAmount());
            ps.setTimestamp(5, entity.getChargeTime() != null ? Timestamp.valueOf(entity.getChargeTime()) : null);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    entity.setChargeId(rs.getLong(1));
                }
            }
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert incidental charge: " + e.getMessage(), e);
        }
    }

    private IncidentalCharge update(IncidentalCharge entity) {
        String sql = "UPDATE incidental_charges SET stay_id=?, service_type=?, description=?, amount=?, charge_time=? WHERE charge_id=?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, entity.getStay() != null ? entity.getStay().getStayId() : null);
            ps.setString(2, entity.getServiceType());
            ps.setString(3, entity.getDescription());
            ps.setBigDecimal(4, entity.getAmount());
            ps.setTimestamp(5, entity.getChargeTime() != null ? Timestamp.valueOf(entity.getChargeTime()) : null);
            ps.setLong(6, entity.getChargeId());

            ps.executeUpdate();
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update incidental charge: " + e.getMessage(), e);
        }
    }

    public Optional<IncidentalCharge> findById(Long id) {
        if (id == null)
            return Optional.empty();

        String sql = "SELECT * FROM incidental_charges WHERE charge_id = ?";

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
            throw new RuntimeException("Failed to find incidental charge: " + e.getMessage(), e);
        }
    }

    public List<IncidentalCharge> findAll() {
        String sql = "SELECT * FROM incidental_charges ORDER BY charge_id";
        List<IncidentalCharge> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all incidental charges: " + e.getMessage(), e);
        }
    }

    public void delete(Long id) {
        if (id == null)
            return;

        String sql = "DELETE FROM incidental_charges WHERE charge_id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete incidental charge: " + e.getMessage(), e);
        }
    }

    public boolean existsById(Long id) {
        return findById(id).isPresent();
    }

    public List<IncidentalCharge> findByStayId(Long stayId) {
        if (stayId == null)
            return List.of();

        String sql = "SELECT * FROM incidental_charges WHERE stay_id = ?";
        List<IncidentalCharge> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, stayId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find charges by stay: " + e.getMessage(), e);
        }
    }

    public BigDecimal getTotalChargesForStay(Long stayId) {
        return findByStayId(stayId).stream()
                .map(IncidentalCharge::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM incidental_charges";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count incidental charges: " + e.getMessage(), e);
        }
    }

    private IncidentalCharge mapRow(ResultSet rs) throws SQLException {
        IncidentalCharge charge = new IncidentalCharge();
        charge.setChargeId(rs.getLong("charge_id"));

        // Load Stay (lightweight - just load the reference)
        long stayId = rs.getLong("stay_id");
        if (!rs.wasNull()) {
            stayRepository.findById(stayId).ifPresent(charge::setStay);
        }

        charge.setServiceType(rs.getString("service_type"));
        charge.setDescription(rs.getString("description"));
        charge.setAmount(rs.getBigDecimal("amount"));

        Timestamp chargeTime = rs.getTimestamp("charge_time");
        if (chargeTime != null) {
            charge.setChargeTime(chargeTime.toLocalDateTime());
        }

        return charge;
    }
}
