package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.request.CreateRuntimeSubscriptionRequest;
import com.rs.subscription.dto.response.RuntimeSubscriptionResponse;
import com.rs.subscription.service.RuntimeSubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/runtime-subscriptions")
@RequiredArgsConstructor
public class RuntimeSubscriptionController {

    private final RuntimeSubscriptionService runtimeSubscriptionService;

    @GetMapping
    @PreAuthorize("hasAuthority('subscription:view')")
    public ApiResponse<List<RuntimeSubscriptionResponse>> list() {
        return ApiResponse.success(runtimeSubscriptionService.listAll(), "Fetched runtime subscriptions");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('subscription:view')")
    public ApiResponse<RuntimeSubscriptionResponse> get(@PathVariable Long id) {
        return ApiResponse.success(runtimeSubscriptionService.getById(id), "Fetched runtime subscription");
    }

    @PostMapping
    @PreAuthorize("hasAuthority('subscription:create')")
    public ApiResponse<RuntimeSubscriptionResponse> create(@Valid @RequestBody CreateRuntimeSubscriptionRequest request) {
        return ApiResponse.success(runtimeSubscriptionService.create(request), "Created runtime subscription");
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('subscription:update')")
    public ApiResponse<RuntimeSubscriptionResponse> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        return ApiResponse.success(runtimeSubscriptionService.updateStatus(id, payload.get("status")), "Updated runtime subscription status");
    }
}
