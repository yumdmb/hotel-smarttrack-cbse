package com.hotel.smarttrack.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Invoice {

    private Long invoiceId;
    private Long reservationId;
    private Long stayId;

    private BigDecimal amount;
    private String status;          // UNPAID / PAID
    private LocalDateTime issuedAt; // 开票时间

    public Invoice() {}

    public Invoice(Long invoiceId, Long reservationId, Long stayId,
                   BigDecimal amount, String status, LocalDateTime issuedAt) {
        this.invoiceId = invoiceId;
        this.reservationId = reservationId;
        this.stayId = stayId;
        this.amount = amount;
        this.status = status;
        this.issuedAt = issuedAt;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public Long getStayId() {
        return stayId;
    }

    public void setStayId(Long stayId) {
        this.stayId = stayId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }
}