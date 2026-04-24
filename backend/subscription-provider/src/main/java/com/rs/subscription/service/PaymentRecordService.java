package com.rs.subscription.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rs.subscription.entity.PaymentRecord;
import com.rs.subscription.entity.Subscription;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.repository.PaymentRecordRepository;
import com.rs.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentRecordService {

    private final PaymentRecordRepository paymentRecordRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionService subscriptionService;

    @Value("${sms.webhook.secret:dev-webhook-secret-change-in-prod}")
    private String webhookSecret;

    @Transactional
    public void processWebhook(String payload, String signature) {
        // Verify HMAC-SHA256 signature
        if (!verifySignature(payload, signature)) {
            throw new SmsException(ErrorCodes.INVALID_WEBHOOK_SIGNATURE,
                "Invalid webhook signature", 401);
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> body = mapper.readValue(payload, Map.class);

            String externalReference = (String) body.get("externalReference");
            String paymentStatusStr = (String) body.get("paymentStatus");
            String paymentReference = (String) body.get("paymentReference");

            // Check idempotency
            if (paymentRecordRepository.existsByExternalReference(externalReference)) {
                throw new SmsException(ErrorCodes.DUPLICATE_PAYMENT_REFERENCE,
                    "Duplicate payment reference: " + externalReference, 409);
            }

            // Find subscription
            Subscription sub = subscriptionService.findByPaymentReference(paymentReference);

            PaymentRecord.PaymentStatus status = PaymentRecord.PaymentStatus.valueOf(paymentStatusStr);
            PaymentRecord record = PaymentRecord.builder()
                .subscription(sub)
                .externalReference(externalReference)
                .amount(new java.math.BigDecimal(body.get("amount").toString()))
                .currency((String) body.getOrDefault("currency", "VND"))
                .paymentStatus(status)
                .paymentMethod((String) body.getOrDefault("paymentMethod", "UNKNOWN"))
                .paidAt(LocalDateTime.now())
                .rawPayload(payload)
                .build();

            paymentRecordRepository.save(record);

            if (status == PaymentRecord.PaymentStatus.SUCCESS) {
                subscriptionService.activateIndividualSubscription(sub, externalReference);
            }
        } catch (SmsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error processing payment webhook", e);
            throw new SmsException(ErrorCodes.INTERNAL_ERROR, "Error processing webhook: " + e.getMessage(), 500);
        }
    }

    private boolean verifySignature(String payload, String signature) {
        if (signature == null) return false;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String expected = HexFormat.of().formatHex(hash);
            return expected.equalsIgnoreCase(signature);
        } catch (Exception e) {
            log.error("Signature verification failed", e);
            return false;
        }
    }
}
