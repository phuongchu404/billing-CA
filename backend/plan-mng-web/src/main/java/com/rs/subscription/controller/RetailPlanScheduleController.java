package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.request.CreateRetailPlanScheduleRequest;
import com.rs.subscription.dto.request.ReviewCommercialRequest;
import com.rs.subscription.dto.response.RetailPlanScheduleResponse;
import com.rs.subscription.service.RetailPlanScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/retail-plan-schedules")
@RequiredArgsConstructor
public class RetailPlanScheduleController {

    private final RetailPlanScheduleService retailPlanScheduleService;

    @GetMapping
    @PreAuthorize("hasAuthority('plan:view')")
    public ApiResponse<List<RetailPlanScheduleResponse>> list() {
        return ApiResponse.success(retailPlanScheduleService.listAll(), "Fetched retail plan schedules");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('plan:view','approval:level1','approval:level2','approval:level3')")
    public ApiResponse<RetailPlanScheduleResponse> get(@PathVariable Long id) {
        return ApiResponse.success(retailPlanScheduleService.getById(id), "Fetched retail plan schedule");
    }

    @PostMapping
    @PreAuthorize("hasAuthority('plan:create')")
    public ApiResponse<RetailPlanScheduleResponse> create(@Valid @RequestBody CreateRetailPlanScheduleRequest request) {
        return ApiResponse.success(retailPlanScheduleService.create(request), "Created retail plan schedule");
    }

    @PostMapping("/{id}/review")
    @PreAuthorize("hasAuthority('plan:update')")
    public ApiResponse<RetailPlanScheduleResponse> review(@PathVariable Long id, @Valid @RequestBody ReviewCommercialRequest request) {
        return ApiResponse.success(retailPlanScheduleService.review(id, request), "Reviewed retail plan schedule");
    }
}
