package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.request.UpsertUsageAggregateRequest;
import com.rs.subscription.dto.response.UsageAggregateResponse;
import com.rs.subscription.service.UsageAggregateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usage-aggregates")
@RequiredArgsConstructor
public class UsageAggregateController {

    private final UsageAggregateService usageAggregateService;

    @GetMapping
    @PreAuthorize("hasAuthority('report:view') or hasAuthority('report:group:view')")
    public ApiResponse<List<UsageAggregateResponse>> list(
        @RequestParam(required = false) String aggregateScope,
        @RequestParam(required = false) Long scopeId
    ) {
        if (aggregateScope != null && scopeId != null) {
            return ApiResponse.success(usageAggregateService.listByScope(aggregateScope, scopeId), "Fetched usage aggregates");
        }
        return ApiResponse.success(usageAggregateService.listAll(), "Fetched usage aggregates");
    }

    @PostMapping
    @PreAuthorize("hasAuthority('report:view')")
    public ApiResponse<UsageAggregateResponse> upsert(@Valid @RequestBody UpsertUsageAggregateRequest request) {
        return ApiResponse.success(usageAggregateService.upsert(request), "Upserted usage aggregate");
    }
}
