package com.hotel.smarttrack.billing.impl;

import com.hotel.smarttrack.entity.Invoice;
import com.hotel.smarttrack.entity.Payment;
import com.hotel.smarttrack.entity.Stay;
import com.hotel.smarttrack.service.BillingService;
import com.hotel.smarttrack.service.StayService;

import org.osgi.service.component.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component(service = BillingService.class, immediate = true)
public class BillingManagerImpl implements BillingService {

    private final InvoiceRepository repo = new InvoiceRepository();

    // ============ OSGi Service Reference ============

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private volatile StayService stayService;

    @Activate
    public void activate() {
        System.out.println("==============================================");
        System.out.println("[BillingManagerImpl] Bundle ACTIVATED ✅");
        System.out.println("  - Service Registered: BillingService");
        System.out.println("  - StayService: " + (stayService != null ? "available" : "missing"));
        System.out.println("  - UC17 Generate Billing Documents (Invoice)");
        System.out.println("  - UC18 Process & Record Payments");
        System.out.println("  - UC19 Manage Outstanding Balances");
        System.out.println("  - UC20 Compute Total Charges");
        System.out.println("==============================================");

        loadSeedData();
        System.out.println("[BillingManagerImpl] Loaded " + repo.findAll().size() + " invoices");
    }

    private void loadSeedData() {
        try {
            stayService.getStayById(1L).orElseThrow(
                    () -> new RuntimeException("Stay ID 1 not found"));

            BigDecimal roomCharges = stayService.calculateRoomCharges(1L);
            BigDecimal incidentalCharges = stayService.getTotalIncidentalCharges(1L);
            BigDecimal subtotal = roomCharges.add(incidentalCharges);
            BigDecimal taxes = subtotal.multiply(new BigDecimal("0.10"));
            BigDecimal totalAmount = subtotal.add(taxes);

            Invoice invoice = new Invoice(null, null, 1L, totalAmount, "Issued", LocalDateTime.now());
            repo.save(invoice);

        } catch (Exception e) {
            System.out.println("[BillingManagerImpl] WARNING: Could not load seed data - " + e.getMessage());
        }
    }

    @Deactivate
    public void deactivate() {
        System.out.println("[BillingManagerImpl] Bundle DEACTIVATED");
    }

    // ===================== UC17 =====================

    @Override
    public Invoice generateInvoice(Long stayId) {
        Stay stay = stayService.getStayById(stayId)
                .orElseThrow(() -> new IllegalArgumentException("Stay not found: " + stayId));

        BigDecimal roomCharges = stayService.calculateRoomCharges(stayId);
        BigDecimal incidentalCharges = stayService.getTotalIncidentalCharges(stayId);
        BigDecimal subtotal = roomCharges.add(incidentalCharges);
        BigDecimal taxes = subtotal.multiply(new BigDecimal("0.10"));
        BigDecimal totalAmount = subtotal.add(taxes);

        Invoice inv = new Invoice();
        inv.setStayId(stayId);
        inv.setReservationId(stay.getReservation() != null ? stay.getReservation().getReservationId() : null);
        inv.setAmount(totalAmount);
        inv.setStatus("UNPAID");
        inv.setIssuedAt(LocalDateTime.now());

        return repo.save(inv);
    }

    @Override
    public Optional<Invoice> getInvoiceById(Long invoiceId) {
        return repo.findById(invoiceId);
    }

    @Override
    public Optional<Invoice> getInvoiceByStay(Long stayId) {
        return repo.findByStayId(stayId);
    }

    @Override
    public List<Invoice> getInvoicesByGuest(Long guestId) {
        return List.of();
    }

    @Override
    public List<Invoice> getAllInvoices() {
        return repo.findAll();
    }

    @Override
    public Invoice regenerateInvoice(Long invoiceId) {
        return repo.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));
    }

    // ===================== UC20 =====================

    @Override
    public BigDecimal computeTotalCharges(Long stayId) {
        Optional<Invoice> opt = repo.findByStayId(stayId);
        if (opt.isEmpty())
            return BigDecimal.ZERO;
        Invoice inv = opt.get();
        return inv.getAmount() == null ? BigDecimal.ZERO : inv.getAmount();
    }

    @Override
    public BigDecimal computeRoomCharges(Long stayId) {
        return computeTotalCharges(stayId);
    }

    @Override
    public BigDecimal computeTax(BigDecimal subtotal, BigDecimal taxRate) {
        if (subtotal == null || taxRate == null)
            return BigDecimal.ZERO;
        return subtotal.multiply(taxRate);
    }

    // ===================== UC18 =====================

    @Override
    public Payment processPayment(Long invoiceId, BigDecimal amount, String paymentMethod) {
        return repo.addPayment(invoiceId, amount, paymentMethod, null);
    }

    @Override
    public Payment processPaymentWithReference(Long invoiceId, BigDecimal amount, String paymentMethod,
            String transactionReference) {
        return repo.addPayment(invoiceId, amount, paymentMethod, transactionReference);
    }

    @Override
    public List<Payment> getPaymentsForInvoice(Long invoiceId) {
        return repo.getPayments(invoiceId);
    }

    @Override
    public void refundPayment(Long paymentId) {
        repo.refundPayment(paymentId);
    }

    @Override
    public Optional<Payment> getPaymentById(Long paymentId) {
        return repo.getPaymentById(paymentId);
    }

    // ===================== UC19 =====================

    @Override
    public BigDecimal getOutstandingBalance(Long invoiceId) {
        return repo.getOutstandingBalance(invoiceId);
    }

    @Override
    public List<Invoice> getUnpaidInvoices() {
        return repo.findByStatus("UNPAID");
    }

    @Override
    public List<Invoice> getPartiallyPaidInvoices() {
        return repo.findByStatus("PARTIALLY_PAID");
    }

    @Override
    public List<Invoice> getOverdueInvoices() {
        return List.of();
    }

    @Override
    public void updateInvoiceStatus(Long invoiceId, String status) {
        repo.updateStatus(invoiceId, status);
    }

    // ===================== Discounts / Reports =====================

    @Override
    public void applyDiscount(Long invoiceId, BigDecimal discountAmount, String reason) {
    }

    @Override
    public void removeDiscount(Long invoiceId) {
    }

    @Override
    public BigDecimal getTotalRevenue(String startDate, String endDate) {
        BigDecimal sum = BigDecimal.ZERO;
        for (Invoice inv : repo.findAll()) {
            if ("PAID".equalsIgnoreCase(inv.getStatus()) && inv.getAmount() != null) {
                sum = sum.add(inv.getAmount());
            }
        }
        return sum;
    }

    @Override
    public List<Invoice> getInvoicesByStatus(String status) {
        return new ArrayList<>(repo.findByStatus(status));
    }
}
