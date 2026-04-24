package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.service.PaymentRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Webhooks", description = "External webhook endpoints")
public class WebhookController {

    private final PaymentRecordService paymentRecordService;

    @PostMapping("/payment")
    @Operation(summary = "Receive payment webhook from billing system")
    public ApiResponse<Void> payment(
            @RequestBody String payload,
            @RequestHeader(value = "X-Billing-Signature", required = false) String signature) {
        log.info("Received payment webhook");
        paymentRecordService.processWebhook(payload, signature);
        return ApiResponse.success("Webhook processed successfully");
    }
}
