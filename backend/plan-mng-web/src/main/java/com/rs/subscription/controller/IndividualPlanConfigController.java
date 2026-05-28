package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.request.CreateIndividualPlanConfigRequest;
import com.rs.subscription.dto.request.IndividualApproveRequest;
import com.rs.subscription.dto.request.IndividualRequestApplyRequest;
import com.rs.subscription.dto.response.IndividualPlanConfigDetailResponse;
import com.rs.subscription.dto.response.IndividualPlanConfigSummaryResponse;
import com.rs.subscription.service.IndividualPlanConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/individual/plan-configs")
@RequiredArgsConstructor
public class IndividualPlanConfigController {

    private final IndividualPlanConfigService service;

    @GetMapping
    @PreAuthorize("hasAuthority('plan:view')")
    public ApiResponse<IndividualPlanConfigSummaryResponse> getSummary(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String applyFrom,
            @RequestParam(required = false) String applyUntil,
            @RequestParam(required = false) String updatedAt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(service.getSummary(status, applyFrom, applyUntil, updatedAt, page, size), "Fetched individual plan configs");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('plan:view')")
    public ApiResponse<IndividualPlanConfigDetailResponse> getDetail(@PathVariable Long id) {
        return ApiResponse.success(service.getDetail(id), "Fetched individual plan config detail");
    }

    @PostMapping
    @PreAuthorize("hasAuthority('plan:create')")
    public ApiResponse<IndividualPlanConfigDetailResponse> create(
            @Valid @RequestBody CreateIndividualPlanConfigRequest req) {
        return ApiResponse.success(service.create(req), "Created individual plan config");
    }

    @PostMapping("/{id}/request-apply")
    @PreAuthorize("hasAuthority('plan:update')")
    public ApiResponse<IndividualPlanConfigDetailResponse> requestApply(
            @PathVariable Long id,
            @Valid @RequestBody IndividualRequestApplyRequest req) {
        return ApiResponse.success(service.requestApply(id, req), "Requested apply for plan config");
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('plan:update')")
    public ApiResponse<IndividualPlanConfigDetailResponse> approve(
            @PathVariable Long id,
            @RequestBody IndividualApproveRequest req) {
        return ApiResponse.success(service.approve(id, req), "Approved plan config");
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('plan:update')")
    public ApiResponse<IndividualPlanConfigDetailResponse> reject(
            @PathVariable Long id,
            @RequestParam(defaultValue = "system") String actor) {
        return ApiResponse.success(service.reject(id, actor), "Rejected plan config");
    }

    @PostMapping("/{id}/stop")
    @PreAuthorize("hasAuthority('plan:update')")
    public ApiResponse<IndividualPlanConfigDetailResponse> stop(
            @PathVariable Long id,
            @RequestParam(defaultValue = "system") String actor) {
        return ApiResponse.success(service.stop(id, actor), "Stopped plan config");
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('plan:update')")
    public ApiResponse<IndividualPlanConfigDetailResponse> deactivate(
            @PathVariable Long id,
            @RequestParam(defaultValue = "system") String actor) {
        return ApiResponse.success(service.deactivate(id, actor), "Deactivated plan config");
    }
}
