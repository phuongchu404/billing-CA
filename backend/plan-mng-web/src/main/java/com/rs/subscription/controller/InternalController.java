package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.response.ValidationResponse;
import com.rs.subscription.service.SubscriptionService;
import com.rs.subscription.service.ValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
@Tag(name = "Internal", description = "Internal service-to-service endpoints")
public class InternalController {

    private final ValidationService validationService;
    private final SubscriptionService subscriptionService;

    @GetMapping("/subscriptions/validate")
    @Operation(summary = "Validate subscription status")
    public ApiResponse<ValidationResponse> validate(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) Long groupId,
            @RequestHeader(value = "X-Service-Token", required = false) String serviceToken) {
        return ApiResponse.success(validationService.validateSubscription(userId, groupId),
            "Subscription is active");
    }

    @PostMapping("/subscriptions/{id}/quota/decrement")
    @Operation(summary = "Decrement signing quota")
    public ApiResponse<Void> decrementQuota(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int count,
            @RequestHeader(value = "X-Service-Token", required = false) String serviceToken) {
        subscriptionService.decrementQuota(id, count);
        return ApiResponse.success("Quota decremented successfully");
    }
}
