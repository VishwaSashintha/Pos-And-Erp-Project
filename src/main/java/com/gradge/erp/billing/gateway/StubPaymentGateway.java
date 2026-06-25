package com.gradge.erp.billing.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Stub / sandbox gateway — safe to use in development & testing.
 * Replace with a real Stripe / PayHere adapter in production by
 * removing @Primary from this class and adding it to the real impl.
 */
@Slf4j
@Component
public class StubPaymentGateway implements PaymentGateway {

    @Override
    public Map<String, String> initiatePayment(String tenantId, BigDecimal amount, String currency, String description) {
        String ref = "STUB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("[STUB GATEWAY] Payment initiated. Tenant={}, Amount={} {}, Ref={}", tenantId, amount, currency, ref);

        Map<String, String> result = new HashMap<>();
        result.put("paymentRef", ref);
        result.put("paymentUrl", "https://sandbox.payment.example.com/pay/" + ref);
        result.put("status", "PENDING");
        return result;
    }

    @Override
    public boolean verifyWebhook(String payload, String signature) {
        // In a real gateway, compare HMAC signature of payload against the signature header
        log.info("[STUB GATEWAY] Webhook verified (stub — always true)");
        return true;
    }

    @Override
    public String confirmPayment(String transactionId) {
        log.info("[STUB GATEWAY] Payment confirmed for transactionId={}", transactionId);
        return transactionId;
    }
}
