package com.gradge.erp.common.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Publish a PosSaleCreatedEvent to the BOS events exchange.
     * All bound queues (inventory, accounting, analytics) will receive a copy.
     */
    public void publishPosSaleCreated(PosSaleCreatedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY_POS_SALE,
                    event
            );
            log.info("Published PosSaleCreatedEvent for invoice {} tenant {}",
                    event.getInvoiceNumber(), event.getTenantId());
        } catch (Exception e) {
            // Graceful degradation — log but do not block the HTTP response
            log.error("Failed to publish PosSaleCreatedEvent: {}", e.getMessage(), e);
        }
    }
}
