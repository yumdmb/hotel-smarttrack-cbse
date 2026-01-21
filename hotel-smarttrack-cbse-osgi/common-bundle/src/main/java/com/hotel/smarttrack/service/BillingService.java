package com.hotel.smarttrack.service;

import com.hotel.smarttrack.entity.Invoice;
import com.hotel.smarttrack.entity.Payment;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * BillingService interface - exposes Billing & Payment functionality.
 * Part of Base Library (CBSE Rule 5) - interface in common library.
 * 
 * <p>
 * Implemented by BillingManager in billing-payment-bundle.
 * </p>
 * 
 * <p>
 * Handles UC17-UC20:
 * </p>
 * <ul>
 * <li>UC17: Generate Billing Documents</li>
 * <li>UC18: Process & Record Payments</li>
 * <li>UC19: Manage Outstanding Balances</li>
 * <li>UC20: Compute Total Charges</li>
 * </ul>
 * 
 * @author Huang Di
 */
public interface BillingService {

    // ============ Invoice Generation (UC17) ============

    /**
     * Generate an invoice for a stay.
     * 
     * @param stayId Stay ID
     * @return Generated invoice
     * @throws IllegalArgumentException if stay not found
     */
    Invoice generateInvoice(Long stayId);

    /**
     * Get invoice by ID.
     * 
     * @param invoiceId Invoice ID
     * @return Invoice if found
     */
    Optional<Invoice> getInvoiceById(Long invoiceId);

    /**
     * Get invoice by stay.
     * 
     * @param stayId Stay ID
     * @return Invoice if found
     */
    Optional<Invoice> getInvoiceByStay(Long stayId);

    /**
     * Get all invoices for a guest.
     * 
     * @param guestId Guest ID
     * @return List of invoices
     */
    List<Invoice> getInvoicesByGuest(Long guestId);

    /**
     * Get all invoices.
     * 
     * @return List of all invoices
     */
    List<Invoice> getAllInvoices();

    /**
     * Regenerate/update an invoice (recalculate charges).
     * 
     * @param invoiceId Invoice ID
     * @return Updated invoice
     * @throws IllegalArgumentException if invoice not found
     */
    Invoice regenerateInvoice(Long invoiceId);

    // ============ Charge Computation (UC20) ============

    /**
     * Compute total charges for a stay (room + incidentals + taxes - discounts).
     * 
     * @param stayId Stay ID
     * @return Total charges
     * @throws IllegalArgumentException if stay not found
     */
    BigDecimal computeTotalCharges(Long stayId);

    /**
     * Compute room charges for a stay.
     * 
     * @param stayId Stay ID
     * @return Room charges
     */
    BigDecimal computeRoomCharges(Long stayId);

    /**
     * Compute tax amount for charges.
     * 
     * @param subtotal Subtotal amount
     * @param taxRate  Tax rate (e.g., 0.10 for 10%)
     * @return Tax amount
     */
    BigDecimal computeTax(BigDecimal subtotal, BigDecimal taxRate);

    // ============ Payment Processing (UC18) ============

    /**
     * Process a payment for an invoice.
     * 
     * @param invoiceId     Invoice ID
     * @param amount        Payment amount
     * @param paymentMethod Payment method (Cash, Credit Card, Debit Card, Digital
     *                      Wallet)
     * @return Processed payment record
     * @throws IllegalArgumentException if invoice not found or invalid amount
     */
    Payment processPayment(Long invoiceId, BigDecimal amount, String paymentMethod);

    /**
     * Record a payment with transaction reference.
     * 
     * @param invoiceId            Invoice ID
     * @param amount               Payment amount
     * @param paymentMethod        Payment method
     * @param transactionReference Transaction reference number
     * @return Processed payment record
     */
    Payment processPaymentWithReference(Long invoiceId, BigDecimal amount,
            String paymentMethod, String transactionReference);

    /**
     * Get all payments for an invoice.
     * 
     * @param invoiceId Invoice ID
     * @return List of payments
     */
    List<Payment> getPaymentsForInvoice(Long invoiceId);

    /**
     * Refund a payment.
     * 
     * @param paymentId Payment ID
     * @throws IllegalArgumentException if payment not found
     */
    void refundPayment(Long paymentId);

    /**
     * Get payment by ID.
     * 
     * @param paymentId Payment ID
     * @return Payment if found
     */
    Optional<Payment> getPaymentById(Long paymentId);

    // ============ Outstanding Balance Management (UC19) ============

    /**
     * Get outstanding balance for an invoice.
     * 
     * @param invoiceId Invoice ID
     * @return Outstanding balance
     * @throws IllegalArgumentException if invoice not found
     */
    BigDecimal getOutstandingBalance(Long invoiceId);

    /**
     * Get all unpaid invoices.
     * 
     * @return List of invoices with outstanding balance > 0
     */
    List<Invoice> getUnpaidInvoices();

    /**
     * Get all partially paid invoices.
     * 
     * @return List of partially paid invoices
     */
    List<Invoice> getPartiallyPaidInvoices();

    /**
     * Get overdue invoices.
     * 
     * @return List of overdue invoices
     */
    List<Invoice> getOverdueInvoices();

    /**
     * Update invoice status.
     * 
     * @param invoiceId Invoice ID
     * @param status    New status (Draft, Issued, Paid, Partially Paid, Overdue)
     * @throws IllegalArgumentException if invoice not found or invalid status
     */
    void updateInvoiceStatus(Long invoiceId, String status);

    // ============ Discounts ============

    /**
     * Apply a discount to an invoice.
     * 
     * @param invoiceId      Invoice ID
     * @param discountAmount Discount amount
     * @param reason         Reason for discount
     * @throws IllegalArgumentException if invoice not found
     */
    void applyDiscount(Long invoiceId, BigDecimal discountAmount, String reason);

    /**
     * Remove discount from an invoice.
     * 
     * @param invoiceId Invoice ID
     * @throws IllegalArgumentException if invoice not found
     */
    void removeDiscount(Long invoiceId);

    // ============ Reports ============

    /**
     * Get total revenue for a date range.
     * 
     * @param startDate Start date (as string in format yyyy-MM-dd)
     * @param endDate   End date (as string in format yyyy-MM-dd)
     * @return Total revenue
     */
    BigDecimal getTotalRevenue(String startDate, String endDate);

    /**
     * Get invoices by status.
     * 
     * @param status Invoice status
     * @return List of invoices with the specified status
     */
    List<Invoice> getInvoicesByStatus(String status);
}
