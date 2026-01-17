package com.hotel.smarttrack.billing;

import com.hotel.smarttrack.entity.Invoice;
import com.hotel.smarttrack.entity.Payment;
import com.hotel.smarttrack.service.BillingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Demo runner to test BillingService functionality.
 * Runs automatically when Spring Boot starts.
 */
@Component
@Order(2) // Run after RoomDemoRunner (Order 1)
public class BillingDemoRunner implements CommandLineRunner {

    private final BillingService billingService;

    public BillingDemoRunner(BillingService billingService) {
        this.billingService = billingService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n========================================");
        System.out.println("    BILLING & PAYMENT DEMO");
        System.out.println("========================================\n");

        // 1. Generate an invoice (using dummy stayId for demo)
        System.out.println(">>> Generating Invoice for Stay ID: 1");
        Invoice invoice = billingService.generateInvoice(1L);
        System.out.println("Created: " + invoice);
        System.out.println("Outstanding Balance: $" + invoice.getOutstandingBalance());

        // 2. Process a partial payment
        System.out.println("\n>>> Processing partial payment of $100 via Credit Card");
        Payment payment1 = billingService.processPayment(
                invoice.getInvoiceId(),
                BigDecimal.valueOf(100.00),
                "Credit Card");
        System.out.println("Payment processed: " + payment1);

        // 3. Check outstanding balance
        BigDecimal balance = billingService.getOutstandingBalance(invoice.getInvoiceId());
        System.out.println("Remaining balance: $" + balance);

        // 4. Process remaining payment
        System.out.println("\n>>> Processing remaining payment of $" + balance + " via Cash");
        Payment payment2 = billingService.processPayment(
                invoice.getInvoiceId(),
                balance,
                "Cash");
        System.out.println("Payment processed: " + payment2);

        // 5. Verify invoice is fully paid
        Invoice updatedInvoice = billingService.getInvoiceById(invoice.getInvoiceId()).orElse(null);
        System.out.println("\n>>> Final Invoice Status: " +
                (updatedInvoice != null ? updatedInvoice.getStatus() : "Not found"));

        System.out.println("\n========================================");
        System.out.println("    BILLING DEMO COMPLETE!");
        System.out.println("========================================\n");
    }
}
