package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.response.IndividualUsageTrackingResponse;
import com.rs.subscription.service.IndividualUsageTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/individual/usage-tracking")
@RequiredArgsConstructor
public class IndividualUsageTrackingController {

    private final IndividualUsageTrackingService service;

    @GetMapping
    @PreAuthorize("hasAuthority('report:view')")
    public ApiResponse<IndividualUsageTrackingResponse> getUsageTracking() {
        return ApiResponse.success(service.getUsageTracking(), "Fetched individual usage tracking");
    }
}
