package com.hotel.smarttrack.service;

import com.hotel.smarttrack.entity.Invoice;
import com.hotel.smarttrack.entity.Payment;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * BillingService interface - exposes Billing & Payment functionality.
 * Part of Base Library (Rule 5) - interface in common library.
 * Implemented by BillingManager in billing-payment component.
 */
public interface BillingService {

    // ============ Invoice Generation ============

    Invoice generateInvoice(Long stayId);

    BigDecimal computeTotalCharges(Long stayId);

    Optional<Invoice> getInvoiceById(Long invoiceId);

    Optional<Invoice> getInvoiceByStay(Long stayId);

    // ============ Payment Processing ============

    Payment processPayment(Long invoiceId, BigDecimal amount, String paymentMethod);

    List<Payment> getPaymentsForInvoice(Long invoiceId);

    // ============ Outstanding Balance Management ============

    BigDecimal getOutstandingBalance(Long invoiceId);

    List<Invoice> getUnpaidInvoices();

    List<Invoice> getInvoicesByGuest(Long guestId);

    void updateInvoiceStatus(Long invoiceId, String status);

    // ============ Discounts ============

    void applyDiscount(Long invoiceId, BigDecimal discountAmount, String reason);
}
