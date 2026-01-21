package com.hotel.smarttrack.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Payment entity for recording payment transactions.
 * Part of Base Library (CBSE Rule 1) - shared across all OSGi bundles.
 * 
 * <p>
 * This is a plain POJO without JPA annotations for OSGi compatibility.
 * </p>
 * 
 * @author Hotel SmartTrack Team
 */
public class Payment {

    private Long paymentId;
    private Long invoiceId; // Reference to Invoice by ID
    private BigDecimal amount;

    /**
     * Payment method: Cash, Credit Card, Debit Card, Digital Wallet
     */
    private String paymentMethod;

    /**
     * Payment status: Pending, Completed, Failed, Refunded
     */
    private String status;

    private String transactionReference;
    private LocalDateTime paymentTime;

    // ============ Constructors ============

    public Payment() {
    }

    public Payment(Long paymentId, Long invoiceId, BigDecimal amount, String paymentMethod,
            String status, String transactionReference, LocalDateTime paymentTime) {
        this.paymentId = paymentId;
        this.invoiceId = invoiceId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.transactionReference = transactionReference;
        this.paymentTime = paymentTime;
    }

    // ============ Getters and Setters ============

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }

    // ============ Object Methods ============

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Payment payment = (Payment) o;
        return Objects.equals(paymentId, payment.paymentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentId);
    }

    @Override
    public String toString() {
        return "Payment{paymentId=" + paymentId + ", amount=" + amount + ", status='" + status + "'}";
    }
}
