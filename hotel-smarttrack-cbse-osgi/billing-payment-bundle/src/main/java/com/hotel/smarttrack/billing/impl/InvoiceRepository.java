package com.hotel.smarttrack.billing.impl;

import com.hotel.smarttrack.entity.Invoice;
import com.hotel.smarttrack.entity.Payment;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-based Invoice and Payment Repository.
 */
public class InvoiceRepository {

    private final DataSource dataSource;

    public InvoiceRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // ===== Invoice CRUD =====

    public Invoice save(Invoice invoice) {
        if (invoice == null)
            throw new IllegalArgumentException("Invoice is null");

        if (invoice.getInvoiceId() == null) {
            return insertInvoice(invoice);
        } else {
            return updateInvoice(invoice);
        }
    }

    private Invoice insertInvoice(Invoice invoice) {
        String sql = "INSERT INTO invoices (stay_id, reservation_id, amount, status, issued_at) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setObject(1, invoice.getStayId());
            ps.setObject(2, invoice.getReservationId());
            ps.setBigDecimal(3, invoice.getAmount());
            ps.setString(4, invoice.getStatus() != null ? invoice.getStatus() : "UNPAID");
            ps.setTimestamp(5, invoice.getIssuedAt() != null ? Timestamp.valueOf(invoice.getIssuedAt())
                    : Timestamp.valueOf(LocalDateTime.now()));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    invoice.setInvoiceId(rs.getLong(1));
                }
            }
            return invoice;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert invoice: " + e.getMessage(), e);
        }
    }

    private Invoice updateInvoice(Invoice invoice) {
        String sql = "UPDATE invoices SET stay_id=?, reservation_id=?, amount=?, status=?, issued_at=? WHERE invoice_id=?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, invoice.getStayId());
            ps.setObject(2, invoice.getReservationId());
            ps.setBigDecimal(3, invoice.getAmount());
            ps.setString(4, invoice.getStatus());
            ps.setTimestamp(5, invoice.getIssuedAt() != null ? Timestamp.valueOf(invoice.getIssuedAt()) : null);
            ps.setLong(6, invoice.getInvoiceId());

            ps.executeUpdate();
            return invoice;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update invoice: " + e.getMessage(), e);
        }
    }

    public Optional<Invoice> findById(Long id) {
        if (id == null)
            return Optional.empty();

        String sql = "SELECT * FROM invoices WHERE invoice_id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapInvoiceRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find invoice: " + e.getMessage(), e);
        }
    }

    public List<Invoice> findAll() {
        String sql = "SELECT * FROM invoices ORDER BY invoice_id";
        List<Invoice> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(mapInvoiceRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all invoices: " + e.getMessage(), e);
        }
    }

    public Optional<Invoice> findByStayId(Long stayId) {
        if (stayId == null)
            return Optional.empty();

        String sql = "SELECT * FROM invoices WHERE stay_id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, stayId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapInvoiceRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find invoice by stay: " + e.getMessage(), e);
        }
    }

    public Optional<Invoice> findByReservationId(Long reservationId) {
        if (reservationId == null)
            return Optional.empty();

        String sql = "SELECT * FROM invoices WHERE reservation_id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, reservationId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapInvoiceRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find invoice by reservation: " + e.getMessage(), e);
        }
    }

    public List<Invoice> findByStatus(String status) {
        if (status == null)
            return List.of();

        String sql = "SELECT * FROM invoices WHERE UPPER(status) = UPPER(?)";
        List<Invoice> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapInvoiceRow(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find invoices by status: " + e.getMessage(), e);
        }
    }

    public void updateStatus(Long invoiceId, String status) {
        Invoice inv = findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));
        inv.setStatus(status);
        save(inv);
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM invoices";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count invoices: " + e.getMessage(), e);
        }
    }

    private Invoice mapInvoiceRow(ResultSet rs) throws SQLException {
        Invoice inv = new Invoice();
        inv.setInvoiceId(rs.getLong("invoice_id"));

        long stayId = rs.getLong("stay_id");
        if (!rs.wasNull()) {
            inv.setStayId(stayId);
        }

        long reservationId = rs.getLong("reservation_id");
        if (!rs.wasNull()) {
            inv.setReservationId(reservationId);
        }

        inv.setAmount(rs.getBigDecimal("amount"));
        inv.setStatus(rs.getString("status"));

        Timestamp issuedAt = rs.getTimestamp("issued_at");
        if (issuedAt != null) {
            inv.setIssuedAt(issuedAt.toLocalDateTime());
        }

        return inv;
    }

    // ===== Payment CRUD =====

    public Payment addPayment(Long invoiceId, BigDecimal amount, String method, String txRef) {
        Invoice inv = findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid payment amount");
        }

        BigDecimal outstanding = getOutstandingBalance(invoiceId);
        if (amount.compareTo(outstanding) > 0) {
            throw new IllegalArgumentException("Payment exceeds outstanding balance");
        }

        String sql = "INSERT INTO payments (invoice_id, amount, payment_method, transaction_reference, payment_time, status) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, invoiceId);
            ps.setBigDecimal(2, amount);
            ps.setString(3, method != null ? method : "Unknown");
            ps.setString(4, txRef);
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(6, "COMPLETED");

            ps.executeUpdate();

            Payment payment = new Payment();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    payment.setPaymentId(rs.getLong(1));
                }
            }
            payment.setInvoiceId(invoiceId);
            payment.setAmount(amount);
            payment.setPaymentMethod(method != null ? method : "Unknown");
            payment.setTransactionReference(txRef);
            payment.setPaymentTime(LocalDateTime.now());
            payment.setStatus("COMPLETED");

            // Update invoice status
            BigDecimal total = inv.getAmount() != null ? inv.getAmount() : BigDecimal.ZERO;
            BigDecimal paid = getPaidAmount(invoiceId);

            if (paid.compareTo(total) >= 0 && total.compareTo(BigDecimal.ZERO) > 0) {
                inv.setStatus("PAID");
            } else if (paid.compareTo(BigDecimal.ZERO) > 0) {
                inv.setStatus("PARTIALLY_PAID");
            } else {
                inv.setStatus("UNPAID");
            }
            save(inv);

            return payment;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add payment: " + e.getMessage(), e);
        }
    }

    public List<Payment> getPayments(Long invoiceId) {
        String sql = "SELECT * FROM payments WHERE invoice_id = ?";
        List<Payment> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, invoiceId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapPaymentRow(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get payments: " + e.getMessage(), e);
        }
    }

    public Optional<Payment> getPaymentById(Long paymentId) {
        if (paymentId == null)
            return Optional.empty();

        String sql = "SELECT * FROM payments WHERE payment_id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, paymentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapPaymentRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get payment: " + e.getMessage(), e);
        }
    }

    public void refundPayment(Long paymentId) {
        Payment p = getPaymentById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        String sql = "UPDATE payments SET status = 'REFUNDED' WHERE payment_id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, paymentId);
            ps.executeUpdate();

            // Update invoice status
            Long invoiceId = p.getInvoiceId();
            if (invoiceId != null) {
                Invoice inv = findById(invoiceId).orElse(null);
                if (inv != null) {
                    BigDecimal total = inv.getAmount() != null ? inv.getAmount() : BigDecimal.ZERO;
                    BigDecimal paid = getPaidAmount(invoiceId);

                    if (paid.compareTo(total) >= 0 && total.compareTo(BigDecimal.ZERO) > 0) {
                        inv.setStatus("PAID");
                    } else if (paid.compareTo(BigDecimal.ZERO) > 0) {
                        inv.setStatus("PARTIALLY_PAID");
                    } else {
                        inv.setStatus("UNPAID");
                    }
                    save(inv);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to refund payment: " + e.getMessage(), e);
        }
    }

    private Payment mapPaymentRow(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setPaymentId(rs.getLong("payment_id"));
        p.setInvoiceId(rs.getLong("invoice_id"));
        p.setAmount(rs.getBigDecimal("amount"));
        p.setPaymentMethod(rs.getString("payment_method"));
        p.setTransactionReference(rs.getString("transaction_reference"));

        Timestamp paymentTime = rs.getTimestamp("payment_time");
        if (paymentTime != null) {
            p.setPaymentTime(paymentTime.toLocalDateTime());
        }

        p.setStatus(rs.getString("status"));
        return p;
    }

    // ===== Balance helpers =====

    public BigDecimal getPaidAmount(Long invoiceId) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE invoice_id = ? AND UPPER(status) = 'COMPLETED'";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, invoiceId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
            return BigDecimal.ZERO;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get paid amount: " + e.getMessage(), e);
        }
    }

    public BigDecimal getOutstandingBalance(Long invoiceId) {
        Invoice inv = findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

        BigDecimal amount = inv.getAmount() != null ? inv.getAmount() : BigDecimal.ZERO;
        BigDecimal paid = getPaidAmount(invoiceId);

        BigDecimal outstanding = amount.subtract(paid);
        return outstanding.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : outstanding;
    }
}
