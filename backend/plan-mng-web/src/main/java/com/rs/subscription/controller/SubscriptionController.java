package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.PagedResponse;
import com.rs.subscription.dto.request.AssignGroupPlanRequest;
import com.rs.subscription.dto.request.CreateSubscriptionRequest;
import com.rs.subscription.dto.response.AuditLogResponse;
import com.rs.subscription.dto.response.SubscriptionResponse;
import com.rs.subscription.service.AdminAuditLogService;
import com.rs.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscriptions", description = "Subscription management")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final AdminAuditLogService auditLogService;

    @GetMapping("/me")
    @Operation(summary = "Get caller's subscriptions")
    public ApiResponse<List<SubscriptionResponse>> getMySubscriptions(@AuthenticationPrincipal String userId) {
        return ApiResponse.success(subscriptionService.getMySubscriptions(userId), "Subscriptions retrieved successfully");
    }

    @PostMapping("/individual")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Initiate individual subscription")
    public ApiResponse<SubscriptionResponse> initiateIndividual(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody CreateSubscriptionRequest req) {
        return ApiResponse.success(subscriptionService.initiateIndividualSubscription(userId, req),
            "Subscription initiated successfully");
    }

    @PostMapping("/group/{groupId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Assign plan to group")
    public ApiResponse<SubscriptionResponse> assignGroupPlan(
            @PathVariable Long groupId,
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody AssignGroupPlanRequest req) {
        SubscriptionResponse sub = subscriptionService.assignPlanToGroup(groupId, req, userId);
        auditLogService.log(userId, "ASSIGN_GROUP_PLAN", "SUBSCRIPTION", String.valueOf(sub.getSubscriptionId()),
                "Assigned plan '" + req.getPlanCode() + "' to partner #" + groupId);
        return ApiResponse.success(sub, "Plan assigned to group successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get subscription by ID")
    public ApiResponse<SubscriptionResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(subscriptionService.getSubscriptionById(id), "Subscription retrieved successfully");
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "List subscriptions with filters")
    public ApiResponse<PagedResponse<SubscriptionResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String subscriberType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(subscriptionService.listSubscriptions(status, query, subscriberType, page, size), "Subscriptions retrieved successfully");
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Approve (activate) a PENDING individual subscription")
    public ApiResponse<Void> approve(@PathVariable Long id, @AuthenticationPrincipal String userId) {
        subscriptionService.approveSubscription(id, userId);
        auditLogService.log(userId, "APPROVE_SUBSCRIPTION", "SUBSCRIPTION", String.valueOf(id),
                "Manually approved subscription #" + id);
        return ApiResponse.success("Subscription approved successfully");
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a subscription")
    public ApiResponse<Void> cancel(@PathVariable Long id, @AuthenticationPrincipal String userId) {
        subscriptionService.cancelSubscription(id, userId);
        auditLogService.log(userId, "CANCEL_SUBSCRIPTION", "SUBSCRIPTION", String.valueOf(id),
                "Cancelled subscription #" + id);
        return ApiResponse.success("Subscription cancelled successfully");
    }

    @PatchMapping("/{id}/suspend")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Suspend a subscription")
    public ApiResponse<Void> suspend(@PathVariable Long id, @AuthenticationPrincipal String userId) {
        subscriptionService.suspendSubscription(id, userId);
        auditLogService.log(userId, "SUSPEND_SUBSCRIPTION", "SUBSCRIPTION", String.valueOf(id),
                "Suspended subscription #" + id);
        return ApiResponse.success("Subscription suspended successfully");
    }

    @PatchMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Reactivate a subscription")
    public ApiResponse<Void> reactivate(@PathVariable Long id, @AuthenticationPrincipal String userId) {
        subscriptionService.reactivateSubscription(id, userId);
        auditLogService.log(userId, "REACTIVATE_SUBSCRIPTION", "SUBSCRIPTION", String.valueOf(id),
                "Reactivated subscription #" + id);
        return ApiResponse.success("Subscription reactivated successfully");
    }

    @GetMapping("/{id}/audit")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get audit trail for subscription")
    public ApiResponse<List<AuditLogResponse>> getAuditLog(@PathVariable Long id) {
        return ApiResponse.success(subscriptionService.getAuditLog(id), "Audit log retrieved successfully");
    }
}
