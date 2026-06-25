package com.gradge.erp.inventory.event;

import com.gradge.erp.common.event.PosSaleCreatedEvent;
import com.gradge.erp.common.event.RabbitMQConfig;
import com.gradge.erp.inventory.entity.Product;
import com.gradge.erp.inventory.entity.StockMovement;
import com.gradge.erp.inventory.enums.StockMovementType;
import com.gradge.erp.inventory.repository.ProductRepository;
import com.gradge.erp.inventory.repository.StockMovementRepository;
import com.gradge.erp.tenant.entity.Tenant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Consumes POS_SALE_CREATED events and asynchronously reduces product stock.
 * Decoupled from POS service via RabbitMQ.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventListener {

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_INVENTORY)
    @Transactional
    public void onPosSaleCreated(PosSaleCreatedEvent event) {
        log.info("INVENTORY: Processing POS sale event for invoice {} tenant {}",
                event.getInvoiceNumber(), event.getTenantId());

        Tenant tenant = new Tenant();
        tenant.setId(event.getTenantId());

        try {
            for (PosSaleCreatedEvent.LineItem lineItem : event.getItems()) {
                Optional<Product> productOpt = productRepository
                        .findByNameAndTenantIdAndDeletedFalse(lineItem.getProductName(), event.getTenantId());

                if (productOpt.isEmpty()) {
                    log.warn("INVENTORY: Product '{}' not found for tenant {}. Skipping.",
                            lineItem.getProductName(), event.getTenantId());
                    continue;
                }

                Product product = productOpt.get();
                int currentStock = product.getQuantity() != null ? product.getQuantity() : 0;
                int deductQty = lineItem.getQuantity();

                if (currentStock < deductQty) {
                    log.warn("INVENTORY: Insufficient stock for '{}'. Available: {}, Requested: {}",
                            product.getName(), currentStock, deductQty);
                }

                product.setQuantity(currentStock - deductQty);
                productRepository.save(product);

                StockMovement movement = StockMovement.builder()
                        .product(product)
                        .quantity((double) deductQty)
                        .type(StockMovementType.OUT)
                        .reference("POS Sale - " + event.getInvoiceNumber())
                        .tenant(tenant)
                        .createdAt(LocalDateTime.now())
                        .build();
                stockMovementRepository.save(movement);

                log.info("INVENTORY: Deducted {} units of '{}'. New stock: {}",
                        deductQty, product.getName(), product.getQuantity());
            }
        } catch (Exception e) {
            log.error("INVENTORY: Failed to process event for invoice {}: {}", event.getInvoiceNumber(), e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException("Unrecoverable error processing inventory event", e);
        }
    }
}
