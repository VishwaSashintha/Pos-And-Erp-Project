package com.gradge.erp.notification.worker;

import com.gradge.erp.common.event.RabbitMQConfig;
import com.gradge.erp.notification.model.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationWorker {

    private final JavaMailSender mailSender;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void processNotification(NotificationMessage message) {
        log.info("Processing email for {}: {}", message.getToEmail(), message.getSubject());
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(message.getToEmail());
            mailMessage.setSubject(message.getSubject());
            mailMessage.setText(message.getBody());
            mailMessage.setFrom("noreply@lightbusiness.com");

            mailSender.send(mailMessage);
            log.info("Successfully sent email to {}", message.getToEmail());
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", message.getToEmail(), e.getMessage());
            // Depending on configuration, this might throw to trigger a RabbitMQ retry
            // throw new RuntimeException("Email delivery failed", e);
        }
    }
}
