package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.response.IndividualUsageTrackingResponse;
import com.rs.subscription.service.IndividualUsageTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/individual/usage-tracking")
@RequiredArgsConstructor
public class IndividualUsageTrackingController {

    private final IndividualUsageTrackingService service;

    @GetMapping
    @PreAuthorize("hasAuthority('report:view') OR hasAuthority('individual:usage:view')")
    public ApiResponse<IndividualUsageTrackingResponse> getUsageTracking(
            @RequestParam(required = false) String purchasedAt,
            @RequestParam(required = false) String ctsType,
            @RequestParam(required = false) String ctsDuration,
            @RequestParam(required = false) String ctsStatus,
            @RequestParam(required = false) String plan) {
        return ApiResponse.success(
            service.getUsageTracking(purchasedAt, ctsType, ctsDuration, ctsStatus, plan),
            "Fetched individual usage tracking");
    }
}
