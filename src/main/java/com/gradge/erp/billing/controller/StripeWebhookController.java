package com.gradge.erp.billing.controller;

import com.gradge.erp.billing.service.BillingService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/webhooks/stripe")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final BillingService billingService;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @PostMapping
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Stripe signature verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Signature Verification Failed");
        } catch (Exception e) {
            log.error("Webhook error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook Error");
        }

        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        
        switch (event.getType()) {
            case "checkout.session.completed":
                if (dataObjectDeserializer.getObject().isPresent()) {
                    Session session = (Session) dataObjectDeserializer.getObject().get();
                    handleCheckoutSessionCompleted(session);
                }
                break;
            case "invoice.payment_failed":
                log.warn("Invoice payment failed for event: {}", event.getId());
                // Handle failed payment (e.g., downgrade plan, lock account)
                break;
            default:
                log.info("Unhandled event type: {}", event.getType());
        }

        return ResponseEntity.ok("Success");
    }

    private void handleCheckoutSessionCompleted(Session session) {
        String tenantIdStr = session.getMetadata().get("tenantId");
        if (tenantIdStr != null) {
            log.info("Processing successful payment for tenant {}", tenantIdStr);
            UUID tenantId = UUID.fromString(tenantIdStr);
            // In a real scenario, you would look up the pending billing record by session ID and mark it paid.
            // For now, let's just log it. The BillingController has /pay/{id} for manual marking.
            // billingService.markAsPaidByTransactionRef(session.getId());
        }
    }
}
