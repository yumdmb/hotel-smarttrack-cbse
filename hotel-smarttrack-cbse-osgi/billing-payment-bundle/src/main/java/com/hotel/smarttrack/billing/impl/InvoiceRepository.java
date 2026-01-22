package com.hotel.smarttrack.billing.impl;

import com.hotel.smarttrack.entity.Invoice;
import com.hotel.smarttrack.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InvoiceRepository {

    private final Map<Long, Invoice> invoices = new ConcurrentHashMap<>();

    private final Map<Long, List<Payment>> paymentsByInvoice = new ConcurrentHashMap<>();
    private final Map<Long, Payment> paymentsById = new ConcurrentHashMap<>();

    private final AtomicLong invoiceIdGen = new AtomicLong(1);
    private final AtomicLong paymentIdGen = new AtomicLong(1);

    public Invoice save(Invoice invoice) {
        if (invoice == null)
            throw new IllegalArgumentException("Invoice is null");

        if (invoice.getInvoiceId() == null) {
            invoice.setInvoiceId(invoiceIdGen.getAndIncrement());
        }
        invoices.put(invoice.getInvoiceId(), invoice);
        return invoice;
    }

    public Optional<Invoice> findById(Long id) {
        if (id == null)
            return Optional.empty();
        return Optional.ofNullable(invoices.get(id));
    }

    public List<Invoice> findAll() {
        return new ArrayList<>(invoices.values());
    }

    public Optional<Invoice> findByStayId(Long stayId) {
        if (stayId == null)
            return Optional.empty();
        for (Invoice i : invoices.values()) {
            if (Objects.equals(i.getStayId(), stayId)) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    public Optional<Invoice> findByReservationId(Long reservationId) {
        if (reservationId == null)
            return Optional.empty();
        for (Invoice i : invoices.values()) {
            if (Objects.equals(i.getReservationId(), reservationId)) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    public List<Invoice> findByStatus(String status) {
        if (status == null)
            return List.of();
        List<Invoice> result = new ArrayList<>();
        for (Invoice i : invoices.values()) {
            if (i.getStatus() != null && status.equalsIgnoreCase(i.getStatus())) {
                result.add(i);
            }
        }
        return result;
    }

    public void updateStatus(Long invoiceId, String status) {
        Invoice inv = invoices.get(invoiceId);
        if (inv == null)
            throw new IllegalArgumentException("Invoice not found: " + invoiceId);
        inv.setStatus(status);
        save(inv);
    }

    // ===== Payments =====

    public Payment addPayment(Long invoiceId, BigDecimal amount, String method, String txRef) {
        Invoice inv = invoices.get(invoiceId);
        if (inv == null)
            throw new IllegalArgumentException("Invoice not found: " + invoiceId);

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid payment amount");
        }

        BigDecimal outstanding = getOutstandingBalance(invoiceId);
        if (amount.compareTo(outstanding) > 0) {
            throw new IllegalArgumentException("Payment exceeds outstanding balance");
        }

        Payment p = new Payment();
        long pid = paymentIdGen.getAndIncrement();

        p.setPaymentId(pid);
        p.setInvoiceId(invoiceId);
        p.setAmount(amount);
        p.setPaymentMethod(method == null ? "Unknown" : method);
        p.setTransactionReference(txRef);
        p.setPaymentTime(LocalDateTime.now());
        p.setStatus("COMPLETED");

        paymentsById.put(pid, p);
        paymentsByInvoice.computeIfAbsent(invoiceId, k -> new ArrayList<>()).add(p);

        BigDecimal total = inv.getAmount() == null ? BigDecimal.ZERO : inv.getAmount();
        BigDecimal paid = getPaidAmount(invoiceId);

        if (paid.compareTo(total) >= 0 && total.compareTo(BigDecimal.ZERO) > 0) {
            inv.setStatus("PAID");
        } else if (paid.compareTo(BigDecimal.ZERO) > 0) {
            inv.setStatus("PARTIALLY_PAID");
        } else {
            inv.setStatus("UNPAID");
        }
        save(inv);

        return p;
    }

    public List<Payment> getPayments(Long invoiceId) {
        return new ArrayList<>(paymentsByInvoice.getOrDefault(invoiceId, List.of()));
    }

    public Optional<Payment> getPaymentById(Long paymentId) {
        if (paymentId == null)
            return Optional.empty();
        return Optional.ofNullable(paymentsById.get(paymentId));
    }

    public void refundPayment(Long paymentId) {
        Payment p = paymentsById.get(paymentId);
        if (p == null)
            throw new IllegalArgumentException("Payment not found: " + paymentId);
        p.setStatus("REFUNDED");

        Long invoiceId = p.getInvoiceId();
        if (invoiceId != null && invoices.containsKey(invoiceId)) {
            Invoice inv = invoices.get(invoiceId);
            BigDecimal total = inv.getAmount() == null ? BigDecimal.ZERO : inv.getAmount();
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

    // ===== Balance helpers =====

    public BigDecimal getPaidAmount(Long invoiceId) {
        List<Payment> list = paymentsByInvoice.getOrDefault(invoiceId, List.of());
        BigDecimal sum = BigDecimal.ZERO;
        for (Payment p : list) {
            if (p.getAmount() != null && "COMPLETED".equalsIgnoreCase(p.getStatus())) {
                sum = sum.add(p.getAmount());
            }
        }
        return sum;
    }

    public BigDecimal getOutstandingBalance(Long invoiceId) {
        Invoice inv = invoices.get(invoiceId);
        if (inv == null)
            throw new IllegalArgumentException("Invoice not found: " + invoiceId);

        BigDecimal amount = inv.getAmount() == null ? BigDecimal.ZERO : inv.getAmount();
        BigDecimal paid = getPaidAmount(invoiceId);

        BigDecimal outstanding = amount.subtract(paid);
        return outstanding.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : outstanding;
    }
}
