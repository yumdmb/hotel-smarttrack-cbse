package com.hotel.smarttrack.console;

import com.hotel.smarttrack.entity.Invoice;
import com.hotel.smarttrack.service.BillingService;
import com.hotel.smarttrack.service.StayService;

import java.math.BigDecimal;
import java.util.List;

/**
 * Billing & Payment Console Menu.
 * Provides invoice generation and payment processing via terminal UI.
 */
public class BillingConsoleMenu {

    private final BillingService billingService;
    private final StayService stayService;
    private final ConsoleInputHelper input;

    public BillingConsoleMenu(BillingService billingService, StayService stayService, ConsoleInputHelper input) {
        this.billingService = billingService;
        this.stayService = stayService;
        this.input = input;
    }

    public void showMenu() {
        if (billingService == null) {
            input.println("\n⚠ Billing Service is not available.");
            input.println("Make sure the billing-payment-bundle is installed and started.");
            return;
        }

        boolean running = true;
        while (running) {
            input.println("\n==============================");
            input.println("      BILLING & PAYMENT       ");
            input.println("==============================");
            input.println("1. List All Invoices");
            input.println("2. List Unpaid Invoices");
            input.println("3. Generate Invoice for Stay");
            input.println("4. Process Payment");
            input.println("5. View Invoice Details");
            input.println("6. View Stays Ready for Invoicing");
            input.println("0. Back to Main Menu");

            String choice = input.readLine("Choose: ");
            try {
                switch (choice) {
                    case "1" -> listAllInvoices();
                    case "2" -> listUnpaidInvoices();
                    case "3" -> generateInvoice();
                    case "4" -> processPayment();
                    case "5" -> viewInvoiceDetails();
                    case "6" -> viewStaysReadyForInvoicing();
                    case "0" -> running = false;
                    default -> input.println("Invalid option.");
                }
            } catch (IllegalArgumentException ex) {
                input.println("❌ Error: " + ex.getMessage());
            } catch (Exception ex) {
                input.println("❌ Unexpected error: " + ex.getMessage());
            }
        }
    }

    private void listAllInvoices() {
        List<Invoice> invoices = billingService.getAllInvoices();
        if (invoices.isEmpty()) {
            input.println("No invoices found.");
            return;
        }
        input.println("\n--- All Invoices ---");
        invoices.forEach(this::printInvoiceSummary);
    }

    private void listUnpaidInvoices() {
        List<Invoice> invoices = billingService.getUnpaidInvoices();
        if (invoices.isEmpty()) {
            input.println("No unpaid invoices found.");
            return;
        }
        input.println("\n--- Unpaid Invoices ---");
        invoices.forEach(this::printInvoiceSummary);
    }

    private void generateInvoice() {
        // Show stays that can be invoiced
        viewStaysReadyForInvoicing();
        
        Long stayId = input.readLong("Stay ID to generate invoice for: ");
        
        Invoice invoice = billingService.generateInvoice(stayId);
        input.println("✅ Invoice generated successfully!");
        printInvoiceDetails(invoice);
    }

    private void processPayment() {
        // Show unpaid invoices
        listUnpaidInvoices();
        
        Long invoiceId = input.readLong("Invoice ID: ");
        BigDecimal amount = readBigDecimal("Payment Amount: ");
        String paymentMethod = input.readLine("Payment Method (Cash/Credit Card/Debit Card/Digital Wallet): ");
        
        billingService.processPayment(invoiceId, amount, paymentMethod);
        input.println("✅ Payment processed successfully!");
        
        // Show updated invoice
        billingService.getInvoiceById(invoiceId).ifPresent(this::printInvoiceDetails);
    }

    private void viewInvoiceDetails() {
        Long invoiceId = input.readLong("Invoice ID: ");
        billingService.getInvoiceById(invoiceId).ifPresentOrElse(
            this::printInvoiceDetails,
            () -> input.println("❌ Invoice not found.")
        );
    }

    private void viewStaysReadyForInvoicing() {
        if (stayService == null) {
            input.println("Stay service not available.");
            return;
        }
        
        input.println("\n--- Active Stays (may need invoicing) ---");
        stayService.getActiveStays().forEach(s ->
            input.println(String.format("ID=%d | Guest: %s | Room: %s | Status: %s",
                s.getStayId(),
                s.getGuest() != null ? s.getGuest().getName() : "N/A",
                s.getRoom() != null ? s.getRoom().getRoomNumber() : "N/A",
                s.getStatus())));
    }

    private void printInvoiceSummary(Invoice inv) {
        input.println(String.format("ID=%d | Stay=%s | Amount=$%s | Status=%s",
            inv.getInvoiceId(),
            inv.getStayId() != null ? inv.getStayId() : "N/A",
            inv.getAmount() != null ? inv.getAmount() : "0",
            inv.getStatus()));
    }

    private void printInvoiceDetails(Invoice inv) {
        input.println("\n--- Invoice Details ---");
        input.println("Invoice ID:     " + inv.getInvoiceId());
        input.println("Stay ID:        " + (inv.getStayId() != null ? inv.getStayId() : "N/A"));
        input.println("Reservation ID: " + (inv.getReservationId() != null ? inv.getReservationId() : "N/A"));
        input.println("Amount:         $" + (inv.getAmount() != null ? inv.getAmount() : "0"));
        input.println("Status:         " + inv.getStatus());
        input.println("Issued At:      " + inv.getIssuedAt());
    }

    // ============ Utility Methods ============

    private BigDecimal readBigDecimal(String prompt) {
        while (true) {
            String s = input.readLine(prompt);
            try {
                return new BigDecimal(s);
            } catch (NumberFormatException e) {
                input.println("Please enter a valid decimal number.");
            }
        }
    }
}
