package com.hotel.smarttrack.billing.dto;

import java.time.LocalDate;

public class BillingInvoiceSummary {

    private Long id;
    private Long reservationId;
    private double amount;
    private String status;      // "UNPAID" / "PAID"
    private LocalDate createdDate;

    public BillingInvoiceSummary(Long id, Long reservationId, double amount, String status, LocalDate createdDate) {
        this.id = id;
        this.reservationId = reservationId;
        this.amount = amount;
        this.status = status;
        this.createdDate = createdDate;
    }

    public Long getId() {
        return id;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public double getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
