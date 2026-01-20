package com.hotel.smarttrack.billing;

import com.hotel.smarttrack.entity.Invoice;
import com.hotel.smarttrack.entity.Payment;
import com.hotel.smarttrack.entity.Stay;
import com.hotel.smarttrack.entity.IncidentalCharge;
import com.hotel.smarttrack.repository.InvoiceRepository;
import com.hotel.smarttrack.repository.PaymentRepository;
import com.hotel.smarttrack.repository.StayRepository;
import com.hotel.smarttrack.repository.IncidentalChargeRepository;
import com.hotel.smarttrack.service.BillingService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * BillingManager - Implementation of BillingService.
 * Handles billing activities such as computing total charges,
 * generating invoices, recording payments, and retrieving outstanding balances.
 * Part of Billing & Payment Component (Rule 2).
 */
@Service
public class BillingManager implements BillingService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.10"); // 10% tax

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final StayRepository stayRepository;
    private final IncidentalChargeRepository incidentalChargeRepository;

    public BillingManager(InvoiceRepository invoiceRepository,
            PaymentRepository paymentRepository,
            StayRepository stayRepository,
            IncidentalChargeRepository incidentalChargeRepository) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.stayRepository = stayRepository;
        this.incidentalChargeRepository = incidentalChargeRepository;
    }

    // ============ Invoice Generation ============

    @Override
    public Invoice generateInvoice(Long stayId) {
        // Check if invoice already exists for this stay
        Optional<Invoice> existingInvoice = invoiceRepository.findByStay_StayId(stayId);
        if (existingInvoice.isPresent()) {
            return existingInvoice.get();
        }

        Stay stay = stayRepository.findById(stayId)
                .orElseThrow(() -> new RuntimeException("Stay not found: " + stayId));

        // Calculate charges
        BigDecimal roomCharges = calculateRoomCharges(stay);
        BigDecimal incidentalCharges = calculateIncidentalCharges(stayId);
        BigDecimal subtotal = roomCharges.add(incidentalCharges);
        BigDecimal taxes = subtotal.multiply(TAX_RATE);
        BigDecimal totalAmount = subtotal.add(taxes);

        Invoice invoice = new Invoice();
        invoice.setStay(stay);
        invoice.setGuest(stay.getGuest());
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

        return invoiceRepository.save(invoice);
    }

    @Override
    public BigDecimal computeTotalCharges(Long stayId) {
        Stay stay = stayRepository.findById(stayId)
                .orElseThrow(() -> new RuntimeException("Stay not found: " + stayId));

        BigDecimal roomCharges = calculateRoomCharges(stay);
        BigDecimal incidentalCharges = calculateIncidentalCharges(stayId);
        BigDecimal subtotal = roomCharges.add(incidentalCharges);
        BigDecimal taxes = subtotal.multiply(TAX_RATE);

        return subtotal.add(taxes);
    }

    @Override
    public Optional<Invoice> getInvoiceById(Long invoiceId) {
        return invoiceRepository.findById(invoiceId);
    }

    @Override
    public Optional<Invoice> getInvoiceByStay(Long stayId) {
        return invoiceRepository.findByStay_StayId(stayId);
    }

    // ============ Payment Processing ============

    @Override
    public Payment processPayment(Long invoiceId, BigDecimal amount, String paymentMethod) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceId));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }

        if (amount.compareTo(invoice.getOutstandingBalance()) > 0) {
            throw new IllegalArgumentException("Payment amount exceeds outstanding balance");
        }

        // Create and save payment
        Payment payment = new Payment();
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus("Completed");
        payment.setTransactionReference(generateTransactionReference());
        payment.setPaymentTime(LocalDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);

        // Update invoice
        BigDecimal newAmountPaid = invoice.getAmountPaid().add(amount);
        BigDecimal newOutstandingBalance = invoice.getTotalAmount()
                .subtract(invoice.getDiscounts())
                .subtract(newAmountPaid);

        invoice.setAmountPaid(newAmountPaid);
        invoice.setOutstandingBalance(newOutstandingBalance);

        // Add payment to invoice's payment list
        List<Payment> payments = invoice.getPayments();
        if (payments == null) {
            payments = new ArrayList<>();
        }
        payments.add(savedPayment);
        invoice.setPayments(payments);

        // Update status
        if (newOutstandingBalance.compareTo(BigDecimal.ZERO) <= 0) {
            invoice.setStatus("Paid");
        } else {
            invoice.setStatus("Partially Paid");
        }

        invoiceRepository.save(invoice);

        return savedPayment;
    }

    @Override
    public List<Payment> getPaymentsForInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceId));
        return invoice.getPayments() != null ? invoice.getPayments() : new ArrayList<>();
    }

    // ============ Outstanding Balance Management ============

    @Override
    public BigDecimal getOutstandingBalance(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceId));
        return invoice.getOutstandingBalance();
    }

    @Override
    public List<Invoice> getUnpaidInvoices() {
        List<Invoice> allInvoices = invoiceRepository.findAll();
        List<Invoice> unpaidInvoices = new ArrayList<>();
        for (Invoice invoice : allInvoices) {
            if (!"Paid".equalsIgnoreCase(invoice.getStatus())) {
                unpaidInvoices.add(invoice);
            }
        }
        return unpaidInvoices;
    }

    @Override
    public List<Invoice> getInvoicesByGuest(Long guestId) {
        List<Invoice> allInvoices = invoiceRepository.findAll();
        List<Invoice> guestInvoices = new ArrayList<>();
        for (Invoice invoice : allInvoices) {
            if (invoice.getGuest() != null && invoice.getGuest().getGuestId().equals(guestId)) {
                guestInvoices.add(invoice);
            }
        }
        return guestInvoices;
    }

    @Override
    public void updateInvoiceStatus(Long invoiceId, String status) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceId));
        invoice.setStatus(status);
        invoiceRepository.save(invoice);
    }

    // ============ Discounts ============

    @Override
    public void applyDiscount(Long invoiceId, BigDecimal discountAmount, String reason) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceId));

        if (discountAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Discount amount must be positive");
        }

        BigDecimal currentDiscounts = invoice.getDiscounts() != null ? invoice.getDiscounts() : BigDecimal.ZERO;
        BigDecimal newDiscounts = currentDiscounts.add(discountAmount);

        invoice.setDiscounts(newDiscounts);

        // Recalculate outstanding balance
        BigDecimal newOutstandingBalance = invoice.getTotalAmount()
                .subtract(newDiscounts)
                .subtract(invoice.getAmountPaid());
        invoice.setOutstandingBalance(newOutstandingBalance);

        // Update status if fully paid after discount
        if (newOutstandingBalance.compareTo(BigDecimal.ZERO) <= 0) {
            invoice.setStatus("Paid");
        }

        invoiceRepository.save(invoice);
    }

    // ============ Convenience Methods for Console ============

    /**
     * Get all invoices (for console listing).
     */
    public List<Invoice> listAllInvoices() {
        return invoiceRepository.findAll();
    }

    // ============ Helper Methods ============

    private BigDecimal calculateRoomCharges(Stay stay) {
        if (stay.getRoom() == null || stay.getRoom().getRoomType() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal ratePerNight = stay.getRoom().getRoomType().getBasePrice();
        if (ratePerNight == null) {
            return BigDecimal.ZERO;
        }

        // Calculate number of nights using checkInTime and checkOutTime
        LocalDateTime checkIn = stay.getCheckInTime();
        LocalDateTime checkOut = stay.getCheckOutTime();

        if (checkIn == null || checkOut == null) {
            return ratePerNight; // Default to 1 night
        }

        long nights = ChronoUnit.DAYS.between(checkIn.toLocalDate(), checkOut.toLocalDate());
        if (nights <= 0) {
            nights = 1;
        }

        return ratePerNight.multiply(BigDecimal.valueOf(nights));
    }

    private BigDecimal calculateIncidentalCharges(Long stayId) {
        List<IncidentalCharge> charges = incidentalChargeRepository.findByStayId(stayId);
        BigDecimal total = BigDecimal.ZERO;
        for (IncidentalCharge charge : charges) {
            if (charge.getAmount() != null) {
                total = total.add(charge.getAmount());
            }
        }
        return total;
    }

    private String generateTransactionReference() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
