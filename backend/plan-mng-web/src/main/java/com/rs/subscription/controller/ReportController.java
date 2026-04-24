package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.response.SubscriptionResponse;
import com.rs.subscription.entity.Subscription;
import com.rs.subscription.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Statistics and reporting")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/subscriptions/summary")
    @Operation(summary = "Subscription summary by status; pass subscriberType=INDIVIDUAL or GROUP for per-type counts")
    public ApiResponse<Map<String, Object>> summary(
            @RequestParam(required = false) String subscriberType) {
        if (subscriberType != null) {
            try {
                Subscription.SubscriberType type = Subscription.SubscriberType.valueOf(subscriberType.toUpperCase());
                return ApiResponse.success(reportService.getSubscriptionSummaryByType(type), "Summary retrieved successfully");
            } catch (IllegalArgumentException e) {
                return ApiResponse.error("INVALID_PARAM", "Invalid subscriberType: " + subscriberType, null);
            }
        }
        return ApiResponse.success(reportService.getSubscriptionSummary(), "Summary retrieved successfully");
    }

    @GetMapping("/subscriptions/expiring-soon")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Subscriptions expiring soon; pass subscriberType=INDIVIDUAL or GROUP to filter")
    public ApiResponse<List<SubscriptionResponse>> expiringSoon(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(required = false) String subscriberType) {
        if (subscriberType != null) {
            try {
                Subscription.SubscriberType type = Subscription.SubscriberType.valueOf(subscriberType.toUpperCase());
                return ApiResponse.success(reportService.getExpiringSoonByType(days, type), "Expiring subscriptions retrieved");
            } catch (IllegalArgumentException e) {
                return ApiResponse.error("INVALID_PARAM", "Invalid subscriberType: " + subscriberType, null);
            }
        }
        return ApiResponse.success(reportService.getExpiringSoon(days), "Expiring subscriptions retrieved");
    }

    @GetMapping("/subscriptions/all")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "All subscriptions for reporting")
    public ApiResponse<List<SubscriptionResponse>> all() {
        return ApiResponse.success(reportService.getAllSubscriptions(), "Subscriptions retrieved successfully");
    }
}
