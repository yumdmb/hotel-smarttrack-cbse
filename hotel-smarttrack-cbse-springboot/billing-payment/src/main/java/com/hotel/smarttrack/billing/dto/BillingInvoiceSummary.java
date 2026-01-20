package com.hotel.smarttrack.billing.dto;

import java.math.BigDecimal;

public class BillingInvoiceSummary {

    private Long invoiceId;
    private String status;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal outstandingBalance;

    public BillingInvoiceSummary() {}

    public BillingInvoiceSummary(Long invoiceId, String status, BigDecimal totalAmount,
                                 BigDecimal amountPaid, BigDecimal outstandingBalance) {
        this.invoiceId = invoiceId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.amountPaid = amountPaid;
        this.outstandingBalance = outstandingBalance;
    }

    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }

    public BigDecimal getOutstandingBalance() { return outstandingBalance; }
    public void setOutstandingBalance(BigDecimal outstandingBalance) { this.outstandingBalance = outstandingBalance; }
}