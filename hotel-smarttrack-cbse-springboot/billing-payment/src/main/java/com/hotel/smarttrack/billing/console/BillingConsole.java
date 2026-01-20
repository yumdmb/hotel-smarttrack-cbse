package com.hotel.smarttrack.billing.console;

import com.hotel.smarttrack.billing.BillingManager;
import com.hotel.smarttrack.billing.dto.BillingInvoiceSummary;
import org.springframework.stereotype.Component;

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
            System.out.println("3. Create invoice");
            System.out.println("4. Pay invoice");
            System.out.println("0. Back to Main Menu");
            System.out.print("\nEnter your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> listAllInvoices();
                case "2" -> listUnpaidInvoices();
                case "3" -> createInvoice(scanner);
                case "4" -> payInvoice(scanner);
                case "0" -> running = false;
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void listAllInvoices() {
        List<BillingInvoiceSummary> invoices = billingManager.listAllInvoices();
        if (invoices.isEmpty()) {
            System.out.println("No invoices found.");
            return;
        }
        invoices.forEach(this::printInvoice);
    }

    private void listUnpaidInvoices() {
        List<BillingInvoiceSummary> invoices = billingManager.listUnpaidInvoices();
        if (invoices.isEmpty()) {
            System.out.println("No unpaid invoices found.");
            return;
        }
        invoices.forEach(this::printInvoice);
    }

    private void createInvoice(Scanner scanner) {
        try {
            System.out.print("Reservation ID: ");
            Long reservationId = Long.parseLong(scanner.nextLine().trim());

            System.out.print("Amount: ");
            double amount = Double.parseDouble(scanner.nextLine().trim());

            BillingInvoiceSummary invoice = billingManager.createInvoice(reservationId, amount);
            System.out.println("✅ Created: ID=" + invoice.getId() + " Status=" + invoice.getStatus());
        } catch (Exception e) {
            System.out.println("❌ Invalid input. " + e.getMessage());
        }
    }

    private void payInvoice(Scanner scanner) {
        try {
            System.out.print("Invoice ID to pay: ");
            Long invoiceId = Long.parseLong(scanner.nextLine().trim());

            boolean ok = billingManager.payInvoice(invoiceId);
            if (ok) {
                System.out.println("✅ Payment successful. Invoice marked as PAID.");
            } else {
                System.out.println("❌ Invoice not found.");
            }
        } catch (Exception e) {
            System.out.println("❌ Invalid input. " + e.getMessage());
        }
    }

    private void printInvoice(BillingInvoiceSummary inv) {
        System.out.println(
                "ID=" + inv.getId()
                        + " | ReservationID=" + inv.getReservationId()
                        + " | Amount=" + inv.getAmount()
                        + " | Status=" + inv.getStatus()
                        + " | Date=" + inv.getCreatedDate()
        );
    }
}
