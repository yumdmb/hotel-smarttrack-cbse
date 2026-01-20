package com.hotel.smarttrack.billing;

import com.hotel.smarttrack.billing.dto.BillingInvoiceSummary;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class BillingManager {

    private final List<BillingInvoiceSummary> invoices = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1000);

    public List<BillingInvoiceSummary> listAllInvoices() {
        return new ArrayList<>(invoices);
    }

    public List<BillingInvoiceSummary> listUnpaidInvoices() {
        List<BillingInvoiceSummary> result = new ArrayList<>();
        for (BillingInvoiceSummary inv : invoices) {
            if (!"PAID".equalsIgnoreCase(inv.getStatus())) {
                result.add(inv);
            }
        }
        return result;
    }

    public BillingInvoiceSummary createInvoice(Long reservationId, double amount) {
        BillingInvoiceSummary invoice = new BillingInvoiceSummary(
                idGenerator.incrementAndGet(),
                reservationId,
                amount,
                "UNPAID",
                LocalDate.now()
        );
        invoices.add(invoice);
        return invoice;
    }

    public boolean payInvoice(Long invoiceId) {
        Optional<BillingInvoiceSummary> opt = findById(invoiceId);
        if (opt.isPresent()) {
            opt.get().setStatus("PAID");
            return true;
        }
        return false;
    }

    public Optional<BillingInvoiceSummary> findById(Long id) {
        for (BillingInvoiceSummary inv : invoices) {
            if (inv.getId().equals(id)) {
                return Optional.of(inv);
            }
        }
        return Optional.empty();
    }
}
