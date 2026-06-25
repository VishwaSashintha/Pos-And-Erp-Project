package com.gradge.erp.notification.service;

import com.gradge.erp.common.event.RabbitMQConfig;
import com.gradge.erp.notification.model.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final RabbitTemplate rabbitTemplate;

    public void sendEmailAsync(NotificationMessage message) {
        log.info("Queuing email to {}", message.getToEmail());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                message
        );
    }
    public void sendEmail(String to, String subject, String body) {
        log.info("Queuing legacy email call to {}", to);
        NotificationMessage msg = NotificationMessage.builder()
                .toEmail(to)
                .subject(subject)
                .body(body)
                .type("GENERAL")
                .build();
        sendEmailAsync(msg);
    }
}
