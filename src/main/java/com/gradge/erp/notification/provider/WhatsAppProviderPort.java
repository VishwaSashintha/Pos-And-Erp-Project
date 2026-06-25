package com.gradge.erp.notification.provider;

public interface WhatsAppProviderPort {

    void send(
            String phoneNumber,
            String message
    );

}