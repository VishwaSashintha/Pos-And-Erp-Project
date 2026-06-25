package com.gradge.erp.finance.event;

import com.gradge.erp.common.event.PosSaleCreatedEvent;
import com.gradge.erp.common.event.RabbitMQConfig;
import com.gradge.erp.finance.service.LedgerService;
import com.gradge.erp.inventory.entity.Product;
import com.gradge.erp.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

/**
 * Consumes POS_SALE_CREATED events and asynchronously creates accounting journal entries.
 * Decoupled from POS service via RabbitMQ.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountingEventListener {

    private final LedgerService ledgerService;
    private final ProductRepository productRepository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ACCOUNTING)
    @Transactional
    public void onPosSaleCreated(PosSaleCreatedEvent event) {
        log.info("ACCOUNTING: Processing POS sale event for invoice {} tenant {}",
                event.getInvoiceNumber(), event.getTenantId());

        try {
            // Record Sales Revenue journal entry: Dr Accounts Receivable (1200) / Cr Sales Revenue (4000)
            ledgerService.recordTransaction(
                    event.getTenantId(),
                    event.getSaleDate() != null ? event.getSaleDate() : LocalDate.now(),
                    "POS Sale Revenue - " + event.getInvoiceNumber(),
                    event.getInvoiceNumber(),
                    Arrays.asList(
                            new LedgerService.LineRequest("1200", event.getTotal(), true),   // Dr AR
                            new LedgerService.LineRequest("4000", event.getTotal(), false)   // Cr Revenue
                    )
            );

            log.info("ACCOUNTING: Journal entry created for invoice {} total {}",
                    event.getInvoiceNumber(), event.getTotal());

            // Compute and record Cost of Goods Sold (COGS): Dr COGS (5000) / Cr Inventory Asset (1400)
            BigDecimal totalCogs = BigDecimal.ZERO;
            for (PosSaleCreatedEvent.LineItem item : event.getItems()) {
                Product product = productRepository.findByNameAndTenantIdAndDeletedFalse(item.getProductName(), event.getTenantId())
                        .orElse(null);
                if (product != null && product.getCostPrice() != null) {
                    BigDecimal cost = BigDecimal.valueOf(product.getCostPrice())
                            .multiply(BigDecimal.valueOf(item.getQuantity()));
                    totalCogs = totalCogs.add(cost);
                }
            }

            if (totalCogs.compareTo(BigDecimal.ZERO) > 0) {
                ledgerService.recordTransaction(
                        event.getTenantId(),
                        event.getSaleDate() != null ? event.getSaleDate() : LocalDate.now(),
                        "Cost of Goods Sold - " + event.getInvoiceNumber(),
                        event.getInvoiceNumber(),
                        Arrays.asList(
                                new LedgerService.LineRequest("5000", totalCogs, true),   // Dr COGS
                                new LedgerService.LineRequest("1400", totalCogs, false)  // Cr Inventory Asset
                        )
                );
                log.info("ACCOUNTING: COGS journal entry created for invoice {} total {}",
                        event.getInvoiceNumber(), totalCogs);
            }
        } catch (Exception e) {
            log.error("ACCOUNTING: Failed to post journal for invoice {}: {}",
                    event.getInvoiceNumber(), e.getMessage(), e);
            // Rethrow as AmqpRejectAndDontRequeueException to trigger DLQ routing
            throw new AmqpRejectAndDontRequeueException("Unrecoverable error processing accounting event", e);
        }
    }
}
