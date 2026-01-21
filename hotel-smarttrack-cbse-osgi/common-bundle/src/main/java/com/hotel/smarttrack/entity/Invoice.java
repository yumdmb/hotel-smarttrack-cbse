package com.hotel.smarttrack.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Invoice entity for billing documents.
 * Part of Base Library (CBSE Rule 1) - shared across all OSGi bundles.
 * 
 * <p>
 * This is a plain POJO without JPA annotations for OSGi compatibility.
 * </p>
 * 
 * @author Hotel SmartTrack Team
 */
public class Invoice {

    private Long invoiceId;
    private Stay stay;
    private Guest guest;
    private BigDecimal roomCharges;
    private BigDecimal incidentalCharges;
    private BigDecimal taxes;
    private BigDecimal discounts;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal outstandingBalance;
    private List<Payment> payments;

    /**
     * Invoice status: Draft, Issued, Paid, Partially Paid, Overdue
     */
    private String status;

    private LocalDateTime issuedTime;

    /**
     * Discount reason if any discount applied
     */
    private String discountReason;

    // ============ Constructors ============

    public Invoice() {
        this.payments = new ArrayList<>();
    }

    public Invoice(Long invoiceId, Stay stay, Guest guest, BigDecimal roomCharges,
            BigDecimal incidentalCharges, BigDecimal taxes, BigDecimal discounts,
            BigDecimal totalAmount, BigDecimal amountPaid, BigDecimal outstandingBalance,
            List<Payment> payments, String status, LocalDateTime issuedTime, String discountReason) {
        this.invoiceId = invoiceId;
        this.stay = stay;
        this.guest = guest;
        this.roomCharges = roomCharges;
        this.incidentalCharges = incidentalCharges;
        this.taxes = taxes;
        this.discounts = discounts;
        this.totalAmount = totalAmount;
        this.amountPaid = amountPaid;
        this.outstandingBalance = outstandingBalance;
        this.payments = payments != null ? payments : new ArrayList<>();
        this.status = status;
        this.issuedTime = issuedTime;
        this.discountReason = discountReason;
    }

    // ============ Getters and Setters ============

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public Stay getStay() {
        return stay;
    }

    public void setStay(Stay stay) {
        this.stay = stay;
    }

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }

    public BigDecimal getRoomCharges() {
        return roomCharges;
    }

    public void setRoomCharges(BigDecimal roomCharges) {
        this.roomCharges = roomCharges;
    }

    public BigDecimal getIncidentalCharges() {
        return incidentalCharges;
    }

    public void setIncidentalCharges(BigDecimal incidentalCharges) {
        this.incidentalCharges = incidentalCharges;
    }

    public BigDecimal getTaxes() {
        return taxes;
    }

    public void setTaxes(BigDecimal taxes) {
        this.taxes = taxes;
    }

    public BigDecimal getDiscounts() {
        return discounts;
    }

    public void setDiscounts(BigDecimal discounts) {
        this.discounts = discounts;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public BigDecimal getOutstandingBalance() {
        return outstandingBalance;
    }

    public void setOutstandingBalance(BigDecimal outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getIssuedTime() {
        return issuedTime;
    }

    public void setIssuedTime(LocalDateTime issuedTime) {
        this.issuedTime = issuedTime;
    }

    public String getDiscountReason() {
        return discountReason;
    }

    public void setDiscountReason(String discountReason) {
        this.discountReason = discountReason;
    }

    // ============ Helper Methods ============

    /**
     * Add a payment to this invoice.
     */
    public void addPayment(Payment payment) {
        if (this.payments == null) {
            this.payments = new ArrayList<>();
        }
        this.payments.add(payment);
    }

    // ============ Object Methods ============

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Invoice invoice = (Invoice) o;
        return Objects.equals(invoiceId, invoice.invoiceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invoiceId);
    }

    @Override
    public String toString() {
        return "Invoice{invoiceId=" + invoiceId + ", totalAmount=" + totalAmount + ", status='" + status + "'}";
    }
}
