package com.gradge.erp.common.event;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "bos.events.exchange";
    public static final String ROUTING_KEY_POS_SALE = "pos.sale.created";

    public static final String QUEUE_INVENTORY = "bos.inventory.pos-sale";
    public static final String QUEUE_ACCOUNTING = "bos.accounting.pos-sale";
    public static final String QUEUE_ANALYTICS = "bos.analytics.pos-sale";

    public static final String EXCHANGE_DLX = "bos.dlx";
    public static final String QUEUE_DLQ = "bos.dlq";

    @Bean
    public DirectExchange eventsExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(EXCHANGE_DLX);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(QUEUE_DLQ).build();
    }

    @Bean
    public Binding bindingDlq() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with("dlq");
    }

    @Bean
    public Queue inventoryQueue() {
        return QueueBuilder.durable(QUEUE_INVENTORY)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", "dlq")
                .build();
    }

    @Bean
    public Queue accountingQueue() {
        return QueueBuilder.durable(QUEUE_ACCOUNTING)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", "dlq")
                .build();
    }

    @Bean
    public Queue analyticsQueue() {
        return QueueBuilder.durable(QUEUE_ANALYTICS)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", "dlq")
                .build();
    }

    @Bean
    public Binding bindingInventory() {
        return BindingBuilder.bind(inventoryQueue()).to(eventsExchange()).with(ROUTING_KEY_POS_SALE);
    }

    @Bean
    public Binding bindingAccounting() {
        return BindingBuilder.bind(accountingQueue()).to(eventsExchange()).with(ROUTING_KEY_POS_SALE);
    }

    @Bean
    public Binding bindingAnalytics() {
        return BindingBuilder.bind(analyticsQueue()).to(eventsExchange()).with(ROUTING_KEY_POS_SALE);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
