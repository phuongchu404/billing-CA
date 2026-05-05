package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.response.ReportHealthResponse;
import com.rs.subscription.service.ReportHealthService;
import com.rs.subscription.service.UsageAggregateSchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/usage-aggregates")
@RequiredArgsConstructor
public class AdminUsageAggregateController {

    private final UsageAggregateSchedulerService usageAggregateSchedulerService;
    private final ReportHealthService reportHealthService;

    @PostMapping("/rollup")
    @PreAuthorize("hasAuthority('report:view') or hasAuthority('report:group:view')")
    public ApiResponse<Map<String, Object>> rollup(@RequestParam String periodKey) {
        int groupsUpdated = usageAggregateSchedulerService.rollupPeriod(periodKey);
        return ApiResponse.success(
            Map.of("periodKey", periodKey, "groupsUpdated", groupsUpdated),
            "Rolled up usage aggregates"
        );
    }

    @GetMapping("/health")
    @PreAuthorize("hasAuthority('report:view') or hasAuthority('report:group:view')")
    public ApiResponse<ReportHealthResponse> health(@RequestParam String periodKey) {
        return ApiResponse.success(reportHealthService.getHealth(periodKey), "Fetched report health");
    }
}
