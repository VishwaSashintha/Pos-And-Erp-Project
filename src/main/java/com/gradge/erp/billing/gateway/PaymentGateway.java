package com.gradge.erp.billing.gateway;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Payment gateway abstraction — plug in any real provider (Stripe, PayHere, etc.)
 * by implementing this interface and marking it as the active @Primary bean.
 */
public interface PaymentGateway {

    /**
     * Initiates a payment intent/session.
     * @return A map containing at minimum "paymentUrl" and "paymentRef"
     */
    Map<String, String> initiatePayment(String tenantId, BigDecimal amount, String currency, String description);

    /**
     * Verifies a webhook/callback payload sent by the gateway.
     * @return true if the signature / payload is valid
     */
    boolean verifyWebhook(String payload, String signature);

    /**
     * Confirms a payment given a gateway transaction ID.
     * @return The resolved transaction reference string
     */
    String confirmPayment(String transactionId);
}
