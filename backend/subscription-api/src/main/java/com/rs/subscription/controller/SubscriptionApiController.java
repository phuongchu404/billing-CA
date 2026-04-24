package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.request.CreateSubscriptionRequest;
import com.rs.subscription.dto.response.SubscriptionResponse;
import com.rs.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscriptions", description = "Subscribe to plans and view own subscriptions")
public class SubscriptionApiController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Subscribe to an individual plan")
    public ApiResponse<SubscriptionResponse> subscribe(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateSubscriptionRequest req) {
        return ApiResponse.success(
            subscriptionService.initiateIndividualSubscription(userId, req),
            "Subscription initiated successfully"
        );
    }

    @GetMapping("/me")
    @Operation(summary = "List all subscriptions for the given user")
    public ApiResponse<List<SubscriptionResponse>> mySubscriptions(
            @RequestHeader("X-User-Id") String userId) {
        return ApiResponse.success(
            subscriptionService.getUserSubscriptions(userId),
            "Subscriptions retrieved successfully"
        );
    }
}
