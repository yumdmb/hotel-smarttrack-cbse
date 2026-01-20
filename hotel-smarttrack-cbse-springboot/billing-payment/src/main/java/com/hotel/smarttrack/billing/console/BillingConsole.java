package com.hotel.smarttrack.billing.console;

import com.hotel.smarttrack.entity.Invoice;
import com.hotel.smarttrack.entity.Payment;
import com.hotel.smarttrack.service.BillingService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * BillingConsole - terminal UI for Billing & Payment module.
 * Minimal deliverable:
 * 1) List unpaid invoices
 * 2) View invoice by id
 * 3) Generate invoice by stayId
 * 4) Pay invoice
 */
@Component
public class BillingConsole {

    private final BillingService billingService;

    public BillingConsole(BillingService billingService) {
        this.billingService = billingService;
    }

    public void showMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            printBillingMenu();
            System.out.print("\nEnter your choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> listUnpaidInvoices();
                case "2" -> viewInvoiceById(scanner);
                case "3" -> generateInvoiceByStayId(scanner);
                case "4" -> payInvoice(scanner);
                case "0" -> running = false;
                default -> System.out.println("\n⚠ Invalid choice. Please enter a number from 0-4.");
            }
        }
    }

    private void printBillingMenu() {
        System.out.println("\n");
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                      BILLING & PAYMENT                       ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║   1. List unpaid invoices                                    ║");
        System.out.println("║   2. View invoice by invoiceId                               ║");
        System.out.println("║   3. Generate invoice by stayId                              ║");
        System.out.println("║   4. Pay invoice                                             ║");
        System.out.println("║                                                              ║");
        System.out.println("║   0. Back to Main Menu                                       ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }

    private void listUnpaidInvoices() {
        List<Invoice> unpaid = billingService.getUnpaidInvoices();
        System.out.println("\n--- Unpaid Invoices ---");
        if (unpaid.isEmpty()) {
            System.out.println("(none)");
            return;
        }

        for (Invoice inv : unpaid) {
            System.out.println(
                    "InvoiceID=" + safe(inv.getInvoiceId())
                            + " | Status=" + safe(inv.getStatus())
                            + " | Total=" + safe(inv.getTotalAmount())
                            + " | Paid=" + safe(inv.getAmountPaid())
                            + " | Outstanding=" + safe(inv.getOutstandingBalance())
            );
        }
    }

    private void viewInvoiceById(Scanner scanner) {
        System.out.print("Enter invoiceId: ");
        String input = scanner.nextLine().trim();

        Long invoiceId = parseLong(input);
        if (invoiceId == null) return;

        Optional<Invoice> opt = billingService.getInvoiceById(invoiceId);
        if (opt.isEmpty()) {
            System.out.println("Invoice not found.");
            return;
        }

        Invoice inv = opt.get();
        System.out.println("\n--- Invoice Detail ---");
        System.out.println("Invoice ID: " + safe(inv.getInvoiceId()));
        System.out.println("Status: " + safe(inv.getStatus()));
        System.out.println("Room Charges: " + safe(inv.getRoomCharges()));
        System.out.println("Incidental Charges: " + safe(inv.getIncidentalCharges()));
        System.out.println("Taxes: " + safe(inv.getTaxes()));
        System.out.println("Discounts: " + safe(inv.getDiscounts()));
        System.out.println("Total: " + safe(inv.getTotalAmount()));
        System.out.println("Paid: " + safe(inv.getAmountPaid()));
        System.out.println("Outstanding: " + safe(inv.getOutstandingBalance()));

        List<Payment> payments = inv.getPayments();
        System.out.println("\nPayments:");
        if (payments == null || payments.isEmpty()) {
            System.out.println("(none)");
        } else {
            for (Payment p : payments) {
                System.out.println("- Amount=" + safe(p.getAmount())
                        + " | Method=" + safe(p.getPaymentMethod())
                        + " | Status=" + safe(p.getStatus())
                        + " | Ref=" + safe(p.getTransactionReference()));
            }
        }
    }

    private void generateInvoiceByStayId(Scanner scanner) {
        System.out.print("Enter stayId: ");
        String input = scanner.nextLine().trim();

        Long stayId = parseLong(input);
        if (stayId == null) return;

        Invoice inv = billingService.generateInvoice(stayId);
        System.out.println("Generated invoice. InvoiceID=" + safe(inv.getInvoiceId())
                + " | Total=" + safe(inv.getTotalAmount()));
    }

    private void payInvoice(Scanner scanner) {
        System.out.print("Enter invoiceId: ");
        Long invoiceId = parseLong(scanner.nextLine().trim());
        if (invoiceId == null) return;

        System.out.print("Enter amount: ");
        BigDecimal amount = parseBigDecimal(scanner.nextLine().trim());
        if (amount == null) return;

        System.out.print("Enter payment method (Cash/Card/Online): ");
        String method = scanner.nextLine().trim();
        if (method.isBlank()) method = "Cash";

        Payment p = billingService.processPayment(invoiceId, amount, method);
        System.out.println("Payment success. Ref=" + safe(p.getTransactionReference()));
        System.out.println("New outstanding balance = " + safe(billingService.getOutstandingBalance(invoiceId)));
    }

    // ---------- helpers ----------

    private Long parseLong(String s) {
        try {
            return Long.parseLong(s);
        } catch (Exception e) {
            System.out.println("Invalid number.");
            return null;
        }
    }

    private BigDecimal parseBigDecimal(String s) {
        try {
            return new BigDecimal(s);
        } catch (Exception e) {
            System.out.println("Invalid amount.");
            return null;
        }
    }

    private String safe(Object obj) {
        return obj == null ? "null" : String.valueOf(obj);
    }
}
