package com.gradge.erp.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage implements Serializable {
    private String toEmail;
    private String subject;
    private String body;
    private UUID tenantId;
    private String type; // WELCOME, INVOICE, SUBSCRIPTION
}
