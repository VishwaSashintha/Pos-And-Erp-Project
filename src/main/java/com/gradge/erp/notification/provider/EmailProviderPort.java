package com.gradge.erp.notification.provider;

public interface EmailProviderPort {

    void send(
            String recipient,
            String subject,
            String body
    );

}