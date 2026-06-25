package com.gradge.erp.pos.service;

import com.gradge.erp.billing.guard.FeatureGuard;
import com.gradge.erp.billing.model.FeatureKey;
import com.gradge.erp.common.event.EventPublisher;
import com.gradge.erp.common.event.PosSaleCreatedEvent;
import com.gradge.erp.notification.events.EventPublisherService;
import com.gradge.erp.notification.events.SystemEvent;
import com.gradge.erp.notification.events.SystemEventType;
import com.gradge.erp.notification.service.NotificationService;
import com.gradge.erp.pos.entity.Invoice;
import com.gradge.erp.pos.enums.InvoiceStatus;
import com.gradge.erp.pos.repository.InvoiceRepository;
import com.gradge.erp.finance.service.LedgerService;
import com.gradge.erp.common.audit.Auditable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final FeatureGuard featureGuard;
    private final LedgerService ledgerService;
    private final EventPublisher eventPublisher;
    private final EventPublisherService eventPublisherService;
    private final NotificationService notificationService;

    public Invoice createInvoice(Invoice invoice, UUID tenantId) {
        featureGuard.check(tenantId, FeatureKey.POS);
        invoice.setTenantId(tenantId);
        if (invoice.getTenant() == null) {
            invoice.setTenant(new com.gradge.erp.tenant.entity.Tenant());
            invoice.getTenant().setId(tenantId);
        }
        return invoiceRepository.save(invoice);
    }

    public List<Invoice> getAll(UUID tenantId) {
        return invoiceRepository.findByTenant_IdAndDeletedFalse(tenantId);
    }

    public Invoice getById(UUID id, UUID tenantId) {
        return invoiceRepository.findByIdAndTenant_Id(id, tenantId);
    }

    @Transactional
    @Auditable(action = "INVOICE_CONFIRMED")
    public Invoice confirmInvoice(UUID id, UUID tenantId) {
        Invoice invoice = invoiceRepository.findByIdAndTenant_Id(id, tenantId);
        if (invoice == null) {
            throw new IllegalArgumentException("Invoice not found");
        }
        invoice.setStatus(InvoiceStatus.CONFIRMED);
        Invoice saved = invoiceRepository.save(invoice);

        // ── Publish async event — Inventory and Analytics consumers will react ──
        List<PosSaleCreatedEvent.LineItem> lineItems = saved.getItems().stream()
                .map(item -> PosSaleCreatedEvent.LineItem.builder()
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .lineTotal(item.getLineTotal())
                        .build())
                .collect(Collectors.toList());

        PosSaleCreatedEvent event = PosSaleCreatedEvent.builder()
                .tenantId(tenantId)
                .invoiceId(saved.getId())
                .invoiceNumber(saved.getInvoiceNumber())
                .customerId(saved.getCustomer() != null ? saved.getCustomer().getId() : null)
                .total(saved.getTotal())
                .saleDate(LocalDate.now())
                .items(lineItems)
                .build();

        eventPublisher.publishPosSaleCreated(event);
        log.info("POS_SALE_CREATED event queued for invoice {}", saved.getInvoiceNumber());

        // ── WebSocket push ───────────────────────────────────────────────────────
        eventPublisherService.publish(SystemEvent.builder()
                .type(SystemEventType.INVOICE_CREATED)
                .tenantId(tenantId)
                .message("Invoice " + saved.getInvoiceNumber() + " has been confirmed.")
                .data(saved.getId())
                .timestamp(LocalDateTime.now())
                .build());

        // ── Email notification ───────────────────────────────────────────────────
        if (saved.getCustomer() != null && saved.getCustomer().getEmail() != null
                && !saved.getCustomer().getEmail().isBlank()) {
            String subject = "Invoice Confirmed — " + saved.getInvoiceNumber();
            String body = "Dear " + saved.getCustomer().getName() + ",\n\n"
                    + "Your invoice " + saved.getInvoiceNumber() + " has been confirmed.\n"
                    + "Total Amount: $" + saved.getTotal() + "\n\n"
                    + "Thank you for choosing Gradge ERP.\n\nBest regards,\nGradge ERP Team";
            notificationService.sendEmail(saved.getCustomer().getEmail(), subject, body);
        }

        return saved;
    }

    @Transactional
    public Invoice markPaid(UUID id, UUID tenantId, Double amount) {
        Invoice invoice = invoiceRepository.findByIdAndTenant_Id(id, tenantId);
        if (invoice == null) {
            throw new IllegalArgumentException("Invoice not found");
        }

        BigDecimal paid = BigDecimal.valueOf(amount != null ? amount : 0.0);
        invoice.setPaidAmount(invoice.getPaidAmount().add(paid));
        invoice.setStatus(InvoiceStatus.PAID);
        Invoice saved = invoiceRepository.save(invoice);

        if (paid.compareTo(BigDecimal.ZERO) > 0) {
            ledgerService.recordTransaction(
                    tenantId,
                    LocalDate.now(),
                    "Invoice Payment Received - " + saved.getInvoiceNumber(),
                    saved.getInvoiceNumber(),
                    Arrays.asList(
                            new LedgerService.LineRequest("1000", paid, true),
                            new LedgerService.LineRequest("1200", paid, false)
                    )
            );
        }

        // ── WebSocket push ───────────────────────────────────────────────────────
        eventPublisherService.publish(SystemEvent.builder()
                .type(SystemEventType.INVOICE_PAID)
                .tenantId(tenantId)
                .message("Invoice " + saved.getInvoiceNumber() + " has been paid — $" + paid)
                .data(saved.getId())
                .timestamp(LocalDateTime.now())
                .build());

        // ── Email notification ───────────────────────────────────────────────────
        if (saved.getCustomer() != null && saved.getCustomer().getEmail() != null
                && !saved.getCustomer().getEmail().isBlank()) {
            String subject = "Payment Received — " + saved.getInvoiceNumber();
            String body = "Dear " + saved.getCustomer().getName() + ",\n\n"
                    + "We have received your payment of $" + paid
                    + " for invoice " + saved.getInvoiceNumber() + ".\n"
                    + "Status: " + saved.getStatus().name() + "\n\n"
                    + "Thank you for your business!\n\nBest regards,\nGradge ERP Team";
            notificationService.sendEmail(saved.getCustomer().getEmail(), subject, body);
        }

        return saved;
    }
}
