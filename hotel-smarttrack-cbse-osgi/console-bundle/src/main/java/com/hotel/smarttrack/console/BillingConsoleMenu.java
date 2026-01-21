package com.hotel.smarttrack.console;

import com.hotel.smarttrack.entity.Invoice;
import com.hotel.smarttrack.service.BillingService;
import com.hotel.smarttrack.service.StayService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

/**
 * Billing & Payment Console Menu.
 * Provides invoice generation and payment processing via terminal UI.
 */
public class BillingConsoleMenu {

    private final BillingService billingService;
    private final StayService stayService;

    public BillingConsoleMenu(BillingService billingService, StayService stayService) {
        this.billingService = billingService;
        this.stayService = stayService;
    }

    public void showMenu(Scanner scanner) {
        if (billingService == null) {
            System.out.println("\n⚠ Billing Service is not available.");
            System.out.println("Make sure the billing-payment-bundle is installed and started.");
            return;
        }

        boolean running = true;
        while (running) {
            System.out.println("\n==============================");
            System.out.println("      BILLING & PAYMENT       ");
            System.out.println("==============================");
            System.out.println("1. List All Invoices");
            System.out.println("2. List Unpaid Invoices");
            System.out.println("3. Generate Invoice for Stay");
            System.out.println("4. Process Payment");
            System.out.println("5. View Invoice Details");
            System.out.println("6. View Stays Ready for Invoicing");
            System.out.println("0. Back to Main Menu");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> listAllInvoices();
                    case "2" -> listUnpaidInvoices();
                    case "3" -> generateInvoice(scanner);
                    case "4" -> processPayment(scanner);
                    case "5" -> viewInvoiceDetails(scanner);
                    case "6" -> viewStaysReadyForInvoicing();
                    case "0" -> running = false;
                    default -> System.out.println("Invalid option.");
                }
            } catch (IllegalArgumentException ex) {
                System.out.println("❌ Error: " + ex.getMessage());
            } catch (Exception ex) {
                System.out.println("❌ Unexpected error: " + ex.getMessage());
            }
        }
    }

    private void listAllInvoices() {
        // OSGi API: getAllInvoices() not listAllInvoices()
        List<Invoice> invoices = billingService.getAllInvoices();
        if (invoices.isEmpty()) {
            System.out.println("No invoices found.");
            return;
        }
        System.out.println("\n--- All Invoices ---");
        invoices.forEach(this::printInvoiceSummary);
    }

    private void listUnpaidInvoices() {
        List<Invoice> invoices = billingService.getUnpaidInvoices();
        if (invoices.isEmpty()) {
            System.out.println("No unpaid invoices found.");
            return;
        }
        System.out.println("\n--- Unpaid Invoices ---");
        invoices.forEach(this::printInvoiceSummary);
    }

    private void generateInvoice(Scanner scanner) {
        // Show stays that can be invoiced
        viewStaysReadyForInvoicing();
        
        Long stayId = readLong(scanner, "Stay ID to generate invoice for: ");
        
        Invoice invoice = billingService.generateInvoice(stayId);
        System.out.println("✅ Invoice generated successfully!");
        printInvoiceDetails(invoice);
    }

    private void processPayment(Scanner scanner) {
        // Show unpaid invoices
        listUnpaidInvoices();
        
        Long invoiceId = readLong(scanner, "Invoice ID: ");
        BigDecimal amount = readBigDecimal(scanner, "Payment Amount: ");
        System.out.print("Payment Method (Cash/Credit Card/Debit Card/Digital Wallet): ");
        String paymentMethod = scanner.nextLine().trim();
        
        billingService.processPayment(invoiceId, amount, paymentMethod);
        System.out.println("✅ Payment processed successfully!");
        
        // Show updated invoice
        billingService.getInvoiceById(invoiceId).ifPresent(this::printInvoiceDetails);
    }

    private void viewInvoiceDetails(Scanner scanner) {
        Long invoiceId = readLong(scanner, "Invoice ID: ");
        billingService.getInvoiceById(invoiceId).ifPresentOrElse(
            this::printInvoiceDetails,
            () -> System.out.println("❌ Invoice not found.")
        );
    }

    private void viewStaysReadyForInvoicing() {
        if (stayService == null) {
            System.out.println("Stay service not available.");
            return;
        }
        
        System.out.println("\n--- Active Stays (may need invoicing) ---");
        stayService.getActiveStays().forEach(s ->
            System.out.printf("ID=%d | Guest: %s | Room: %s | Status: %s%n",
                s.getStayId(),
                s.getGuest() != null ? s.getGuest().getName() : "N/A",
                s.getRoom() != null ? s.getRoom().getRoomNumber() : "N/A",
                s.getStatus()));
    }

    private void printInvoiceSummary(Invoice inv) {
        System.out.printf("ID=%d | Stay=%s | Amount=$%s | Status=%s%n",
            inv.getInvoiceId(),
            inv.getStayId() != null ? inv.getStayId() : "N/A",
            inv.getAmount() != null ? inv.getAmount() : "0",
            inv.getStatus());
    }

    private void printInvoiceDetails(Invoice inv) {
        System.out.println("\n--- Invoice Details ---");
        System.out.println("Invoice ID:     " + inv.getInvoiceId());
        System.out.println("Stay ID:        " + (inv.getStayId() != null ? inv.getStayId() : "N/A"));
        System.out.println("Reservation ID: " + (inv.getReservationId() != null ? inv.getReservationId() : "N/A"));
        System.out.println("Amount:         $" + (inv.getAmount() != null ? inv.getAmount() : "0"));
        System.out.println("Status:         " + inv.getStatus());
        System.out.println("Issued At:      " + inv.getIssuedAt());
    }

    // ============ Utility Methods ============

    private Long readLong(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = scanner.nextLine().trim();
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private BigDecimal readBigDecimal(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = scanner.nextLine().trim();
            try {
                return new BigDecimal(s);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid decimal number.");
            }
        }
    }
}
