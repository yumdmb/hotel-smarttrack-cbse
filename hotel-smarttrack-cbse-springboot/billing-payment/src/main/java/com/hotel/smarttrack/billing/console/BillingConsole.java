package com.hotel.smarttrack.billing.console;

import com.hotel.smarttrack.billing.BillingManager;
import com.hotel.smarttrack.entity.Invoice;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

@Component
public class BillingConsole {

    private final BillingManager billingManager;

    public BillingConsole(BillingManager billingManager) {
        this.billingManager = billingManager;
    }

    public void showMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("\n==============================");
            System.out.println("        BILLING & PAYMENT     ");
            System.out.println("==============================");
            System.out.println("1. List all invoices");
            System.out.println("2. List unpaid invoices");
            System.out.println("3. Generate invoice for stay");
            System.out.println("4. Process payment");
            System.out.println("5. View invoice details");
            System.out.println("0. Back to Main Menu");
            System.out.print("\nEnter your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> listAllInvoices();
                case "2" -> listUnpaidInvoices();
                case "3" -> generateInvoice(scanner);
                case "4" -> processPayment(scanner);
                case "5" -> viewInvoiceDetails(scanner);
                case "0" -> running = false;
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void listAllInvoices() {
        List<Invoice> invoices = billingManager.listAllInvoices();
        if (invoices.isEmpty()) {
            System.out.println("No invoices found.");
            return;
        }
        System.out.println("\n--- All Invoices ---");
        invoices.forEach(this::printInvoiceSummary);
    }

    private void listUnpaidInvoices() {
        List<Invoice> invoices = billingManager.getUnpaidInvoices();
        if (invoices.isEmpty()) {
            System.out.println("No unpaid invoices found.");
            return;
        }
        System.out.println("\n--- Unpaid Invoices ---");
        invoices.forEach(this::printInvoiceSummary);
    }

    private void generateInvoice(Scanner scanner) {
        try {
            System.out.print("Stay ID: ");
            Long stayId = Long.parseLong(scanner.nextLine().trim());

            Invoice invoice = billingManager.generateInvoice(stayId);
            System.out.println("✅ Invoice generated successfully!");
            printInvoiceDetails(invoice);
        } catch (RuntimeException e) {
            System.out.println("❌ Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Invalid input. " + e.getMessage());
        }
    }

    private void processPayment(Scanner scanner) {
        try {
            System.out.print("Invoice ID: ");
            Long invoiceId = Long.parseLong(scanner.nextLine().trim());

            System.out.print("Payment Amount: ");
            BigDecimal amount = new BigDecimal(scanner.nextLine().trim());

            System.out.print("Payment Method (Cash/Credit Card/Debit Card/Digital Wallet): ");
            String paymentMethod = scanner.nextLine().trim();

            billingManager.processPayment(invoiceId, amount, paymentMethod);
            System.out.println("✅ Payment processed successfully!");

            // Show updated invoice
            billingManager.getInvoiceById(invoiceId).ifPresent(this::printInvoiceDetails);
        } catch (RuntimeException e) {
            System.out.println("❌ Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Invalid input. " + e.getMessage());
        }
    }

    private void viewInvoiceDetails(Scanner scanner) {
        try {
            System.out.print("Invoice ID: ");
            Long invoiceId = Long.parseLong(scanner.nextLine().trim());

            billingManager.getInvoiceById(invoiceId).ifPresentOrElse(
                    this::printInvoiceDetails,
                    () -> System.out.println("❌ Invoice not found."));
        } catch (Exception e) {
            System.out.println("❌ Invalid input. " + e.getMessage());
        }
    }

    private void printInvoiceSummary(Invoice inv) {
        System.out.printf("ID=%d | Guest=%s | Total=%s | Paid=%s | Balance=%s | Status=%s%n",
                inv.getInvoiceId(),
                inv.getGuest() != null ? inv.getGuest().getName() : "N/A",
                inv.getTotalAmount(),
                inv.getAmountPaid(),
                inv.getOutstandingBalance(),
                inv.getStatus());
    }

    private void printInvoiceDetails(Invoice inv) {
        System.out.println("\n--- Invoice Details ---");
        System.out.println("Invoice ID:       " + inv.getInvoiceId());
        System.out.println("Guest:            " + (inv.getGuest() != null ? inv.getGuest().getName() : "N/A"));
        System.out.println("Stay ID:          " + (inv.getStay() != null ? inv.getStay().getStayId() : "N/A"));
        System.out.println("Room Charges:     $" + inv.getRoomCharges());
        System.out.println("Incidental:       $" + inv.getIncidentalCharges());
        System.out.println("Taxes:            $" + inv.getTaxes());
        System.out.println("Discounts:        $" + inv.getDiscounts());
        System.out.println("Total Amount:     $" + inv.getTotalAmount());
        System.out.println("Amount Paid:      $" + inv.getAmountPaid());
        System.out.println("Outstanding:      $" + inv.getOutstandingBalance());
        System.out.println("Status:           " + inv.getStatus());
        System.out.println("Issued:           " + inv.getIssuedTime());
    }
}
