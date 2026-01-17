package com.hotel.smarttrack.billing;

import com.hotel.smarttrack.entity.Invoice;
import com.hotel.smarttrack.entity.Payment;
import com.hotel.smarttrack.service.BillingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * BillingManager - Implementation of BillingService.
 * Business logic for Billing & Payment Management (Rule 2 & 3).
 */
@Service
public class BillingManager implements BillingService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    public BillingManager(InvoiceRepository invoiceRepository,
            PaymentRepository paymentRepository) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional
    public Invoice generateInvoice(Long stayId) {
        // Calculate charges (simplified)
        BigDecimal roomCharges = BigDecimal.valueOf(150.00);
        BigDecimal incidentalCharges = BigDecimal.valueOf(50.00);
        BigDecimal taxes = roomCharges.add(incidentalCharges).multiply(BigDecimal.valueOf(0.10));
        BigDecimal totalAmount = roomCharges.add(incidentalCharges).add(taxes);

        Invoice invoice = new Invoice();
        invoice.setRoomCharges(roomCharges);
        invoice.setIncidentalCharges(incidentalCharges);
        invoice.setTaxes(taxes);
        invoice.setDiscounts(BigDecimal.ZERO);
        invoice.setTotalAmount(totalAmount);
        invoice.setAmountPaid(BigDecimal.ZERO);
        invoice.setOutstandingBalance(totalAmount);
        invoice.setStatus("Issued");
        invoice.setIssuedTime(LocalDateTime.now());
        invoice.setPayments(new ArrayList<>());

        Invoice saved = invoiceRepository.save(invoice);

        System.out.println("[BillingManager] Generated invoice for stay " + stayId +
                " - Total: $" + totalAmount);
        return saved;
    }

    @Override
    public BigDecimal computeTotalCharges(Long stayId) {
        return getInvoiceByStay(stayId)
                .map(Invoice::getTotalAmount)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public Optional<Invoice> getInvoiceById(Long invoiceId) {
        return invoiceRepository.findById(invoiceId);
    }

    @Override
    public Optional<Invoice> getInvoiceByStay(Long stayId) {
        return invoiceRepository.findByStay_StayId(stayId);
    }

    @Override
    @Transactional
    public Payment processPayment(Long invoiceId, BigDecimal amount, String paymentMethod) {
        Payment payment = new Payment();
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus("Completed");
        payment.setTransactionReference(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setPaymentTime(LocalDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);

        invoiceRepository.findById(invoiceId).ifPresent(invoice -> {
            if (invoice.getPayments() == null) {
                invoice.setPayments(new ArrayList<>());
            }
            invoice.getPayments().add(savedPayment);

            BigDecimal newPaidAmount = invoice.getAmountPaid().add(amount);
            invoice.setAmountPaid(newPaidAmount);
            invoice.setOutstandingBalance(invoice.getTotalAmount().subtract(newPaidAmount));

            if (invoice.getOutstandingBalance().compareTo(BigDecimal.ZERO) <= 0) {
                invoice.setStatus("Paid");
            } else {
                invoice.setStatus("Partially Paid");
            }

            invoiceRepository.save(invoice);

            System.out.println("[BillingManager] Processed payment: $" + amount +
                    " via " + paymentMethod + " - Ref: " + savedPayment.getTransactionReference());
        });

        return savedPayment;
    }

    @Override
    public List<Payment> getPaymentsForInvoice(Long invoiceId) {
        return getInvoiceById(invoiceId)
                .map(Invoice::getPayments)
                .orElse(new ArrayList<>());
    }

    @Override
    public BigDecimal getOutstandingBalance(Long invoiceId) {
        return getInvoiceById(invoiceId)
                .map(Invoice::getOutstandingBalance)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public List<Invoice> getUnpaidInvoices() {
        return invoiceRepository.findAll().stream()
                .filter(i -> i.getOutstandingBalance() != null
                        && i.getOutstandingBalance().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());
    }

    @Override
    public List<Invoice> getInvoicesByGuest(Long guestId) {
        return invoiceRepository.findAll().stream()
                .filter(i -> i.getGuest() != null
                        && i.getGuest().getGuestId() != null
                        && i.getGuest().getGuestId().equals(guestId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateInvoiceStatus(Long invoiceId, String status) {
        invoiceRepository.findById(invoiceId).ifPresent(invoice -> {
            invoice.setStatus(status);
            invoiceRepository.save(invoice);
            System.out.println("[BillingManager] Updated invoice " + invoiceId + " status to: " + status);
        });
    }

    @Override
    @Transactional
    public void applyDiscount(Long invoiceId, BigDecimal discountAmount, String reason) {
        invoiceRepository.findById(invoiceId).ifPresent(invoice -> {
            BigDecimal currentDiscount = invoice.getDiscounts() == null ? BigDecimal.ZERO : invoice.getDiscounts();
            invoice.setDiscounts(currentDiscount.add(discountAmount));

            BigDecimal newTotal = invoice.getRoomCharges()
                    .add(invoice.getIncidentalCharges())
                    .add(invoice.getTaxes())
                    .subtract(invoice.getDiscounts());

            invoice.setTotalAmount(newTotal);
            invoice.setOutstandingBalance(newTotal.subtract(invoice.getAmountPaid()));

            invoiceRepository.save(invoice);

            System.out.println("[BillingManager] Applied discount: $" + discountAmount + " - " + reason);
        });
    }
}
