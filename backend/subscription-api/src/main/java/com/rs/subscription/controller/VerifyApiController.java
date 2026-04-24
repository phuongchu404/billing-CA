package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.response.SubscriptionVerifyResponse;
import com.rs.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Verification", description = "Subscription verification for external systems")
public class VerifyApiController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/verify/{subscriptionId}")
    @Operation(summary = "Verify a subscription by ID; optionally cross-check userId ownership")
    public ApiResponse<SubscriptionVerifyResponse> verify(
            @PathVariable Long subscriptionId,
            @RequestParam(required = false) String userId) {
        return ApiResponse.success(
            subscriptionService.verifySubscription(subscriptionId, userId),
            "Verification completed"
        );
    }

    @GetMapping("/users/{userId}/active")
    @Operation(summary = "Get the active subscription for a given user")
    public ApiResponse<SubscriptionVerifyResponse> activeForUser(@PathVariable String userId) {
        return ApiResponse.success(
            subscriptionService.getActiveSubscriptionForUser(userId),
            "Active subscription retrieved"
        );
    }
}
