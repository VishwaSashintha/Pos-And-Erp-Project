package com.gradge.erp.billing.gateway;

import com.gradge.erp.billing.model.SubscriptionPlanType;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Primary
public class StripePaymentGateway implements PaymentGateway {

    @Value("${stripe.api-key}")
    private String stripeApiKey;

    @Override
    public Map<String, String> initiatePayment(String tenantId, BigDecimal amount, String currency, String description) {
        Stripe.apiKey = stripeApiKey;

        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                            .setCurrency(currency.toLowerCase())
                                            .setUnitAmount(amount.multiply(new BigDecimal(100)).longValue())
                                            .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                    .setName(description)
                                                    .build())
                                            .build())
                                    .setQuantity(1L)
                                    .build()
                    )
                    .putMetadata("tenantId", tenantId)
                    .setSuccessUrl("https://localhost:3000/billing?success=true&session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl("https://localhost:3000/billing?canceled=true")
                    .build();

            Session session = Session.create(params);

            Map<String, String> result = new HashMap<>();
            result.put("paymentUrl", session.getUrl());
            result.put("paymentRef", session.getId());
            return result;

        } catch (StripeException e) {
            throw new RuntimeException("Stripe API Error: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verifyWebhook(String payload, String signature) {
        return true; // Simplified for now
    }

    @Override
    public String confirmPayment(String transactionId) {
        Stripe.apiKey = stripeApiKey;
        try {
            Session session = Session.retrieve(transactionId);
            if ("paid".equals(session.getPaymentStatus())) {
                return session.getPaymentIntent();
            }
            throw new RuntimeException("Payment not completed");
        } catch (StripeException e) {
            throw new RuntimeException("Error confirming payment", e);
        }
    }
}
