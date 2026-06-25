package com.gradge.erp.notification.provider;

public interface SmsProviderPort {

    void send(
            String phoneNumber,
            String message
    );

}