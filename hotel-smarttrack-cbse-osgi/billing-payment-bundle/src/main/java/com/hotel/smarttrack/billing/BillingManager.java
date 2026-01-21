package com.hotel.smarttrack.billing;

import com.hotel.smarttrack.entity.Invoice;
import com.hotel.smarttrack.entity.Payment;
import com.hotel.smarttrack.service.BillingService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component(service = BillingService.class, immediate = true)
public class BillingManager implements BillingService {

    private final InvoiceRepository repo = new InvoiceRepository();

    @Activate
    public void activate() {
        System.out.println("==============================================");
        System.out.println("[BillingManager] Bundle ACTIVATED ✅");
        System.out.println("  - Service Registered: BillingService");
        System.out.println("  - UC17 Generate Billing Documents (Invoice)");
        System.out.println("  - UC18 Process & Record Payments");
        System.out.println("  - UC19 Manage Outstanding Balances");
        System.out.println("  - UC20 Compute Total Charges");
        System.out.println("==============================================");

        // ✅ 最稳 seed：只保存一个空 Invoice（不调用任何可能缺失的 setter）
        try {
            repo.save(new Invoice());
            System.out.println("[BillingManager] Seed: created 1 empty invoice ✅");
        } catch (Throwable t) {
            System.out.println("[BillingManager] Seed skipped: " + t.getClass().getSimpleName() + " - " + t.getMessage());
        }
    }

    @Deactivate
    public void deactivate() {
        System.out.println("[BillingManager] Bundle DEACTIVATED");
    }

    // ===================== UC17 =====================

    @Override
    public Invoice generateInvoice(Long stayId) {
        // 不依赖 stay bundle：这里只按 stayId 生成 invoice
        Invoice inv = new Invoice();
        inv.setStayId(stayId);

        // 没有 reservationId 的情况下先置 null
        inv.setReservationId(null);

        // Demo 先给一个默认金额
        inv.setAmount(new BigDecimal("100.00"));
        inv.setStatus("UNPAID");
        inv.setIssuedAt(LocalDateTime.now());

        return repo.save(inv);
    }

    @Override
    public Optional<Invoice> getInvoiceById(Long invoiceId) {
        return repo.findById(invoiceId);
    }

    @Override
    public Optional<Invoice> getInvoiceByStay(Long stayId) {
        return repo.findByStayId(stayId);
    }

    @Override
    public List<Invoice> getInvoicesByGuest(Long guestId) {
        // 你现在的 Invoice 实体里没有 guestId 字段，所以先返回空（保守）
        return List.of();
    }

    @Override
    public List<Invoice> getAllInvoices() {
        return repo.findAll();
    }

    @Override
    public Invoice regenerateInvoice(Long invoiceId) {
        // 简化：重新计算 = 直接返回原 invoice
        return repo.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));
    }

    // ===================== UC20 =====================

    @Override
    public BigDecimal computeTotalCharges(Long stayId) {
        // Demo：如果该 stay 有 invoice，用 invoice.amount 当 total
        Optional<Invoice> opt = repo.findByStayId(stayId);
        if (opt.isEmpty()) return BigDecimal.ZERO;
        Invoice inv = opt.get();
        return inv.getAmount() == null ? BigDecimal.ZERO : inv.getAmount();
    }

    @Override
    public BigDecimal computeRoomCharges(Long stayId) {
        // Demo：先当成 total 的 100% 都是 room charge
        return computeTotalCharges(stayId);
    }

    @Override
    public BigDecimal computeTax(BigDecimal subtotal, BigDecimal taxRate) {
        if (subtotal == null || taxRate == null) return BigDecimal.ZERO;
        return subtotal.multiply(taxRate);
    }

    // ===================== UC18 =====================

    @Override
    public Payment processPayment(Long invoiceId, BigDecimal amount, String paymentMethod) {
        return repo.addPayment(invoiceId, amount, paymentMethod, null);
    }

    @Override
    public Payment processPaymentWithReference(Long invoiceId, BigDecimal amount, String paymentMethod, String transactionReference) {
        return repo.addPayment(invoiceId, amount, paymentMethod, transactionReference);
    }

    @Override
    public List<Payment> getPaymentsForInvoice(Long invoiceId) {
        return repo.getPayments(invoiceId);
    }

    @Override
    public void refundPayment(Long paymentId) {
        repo.refundPayment(paymentId);
    }

    @Override
    public Optional<Payment> getPaymentById(Long paymentId) {
        return repo.getPaymentById(paymentId);
    }

    // ===================== UC19 =====================

    @Override
    public BigDecimal getOutstandingBalance(Long invoiceId) {
        return repo.getOutstandingBalance(invoiceId);
    }

    @Override
    public List<Invoice> getUnpaidInvoices() {
        return repo.findByStatus("UNPAID");
    }

    @Override
    public List<Invoice> getPartiallyPaidInvoices() {
        return repo.findByStatus("PARTIALLY_PAID");
    }

    @Override
    public List<Invoice> getOverdueInvoices() {
        // 你们 Invoice 里没有 dueDate，所以先返回空
        return List.of();
    }

    @Override
    public void updateInvoiceStatus(Long invoiceId, String status) {
        repo.updateStatus(invoiceId, status);
    }

    // ===================== Discounts / Reports（简化） =====================

    @Override
    public void applyDiscount(Long invoiceId, BigDecimal discountAmount, String reason) {
        // 你们 Invoice 没有 discount 字段，先不实现
    }

    @Override
    public void removeDiscount(Long invoiceId) {
        // 先不实现
    }

    @Override
    public BigDecimal getTotalRevenue(String startDate, String endDate) {
        // Demo：统计所有 PAID invoice 的 amount
        BigDecimal sum = BigDecimal.ZERO;
        for (Invoice inv : repo.findAll()) {
            if ("PAID".equalsIgnoreCase(inv.getStatus()) && inv.getAmount() != null) {
                sum = sum.add(inv.getAmount());
            }
        }
        return sum;
    }

    @Override
    public List<Invoice> getInvoicesByStatus(String status) {
        return new ArrayList<>(repo.findByStatus(status));
    }
}