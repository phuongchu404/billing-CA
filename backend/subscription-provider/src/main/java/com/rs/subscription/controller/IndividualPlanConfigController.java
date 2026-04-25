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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/individual/plan-configs")
@RequiredArgsConstructor
public class IndividualPlanConfigController {

    private final IndividualPlanConfigService service;

    @GetMapping
    public ApiResponse<IndividualPlanConfigSummaryResponse> getSummary() {
        return ApiResponse.success(service.getSummary(), "Fetched individual plan configs");
    }

    @GetMapping("/{id}")
    public ApiResponse<IndividualPlanConfigDetailResponse> getDetail(@PathVariable Long id) {
        return ApiResponse.success(service.getDetail(id), "Fetched individual plan config detail");
    }

    @PostMapping
    public ApiResponse<IndividualPlanConfigDetailResponse> create(
            @Valid @RequestBody CreateIndividualPlanConfigRequest req) {
        return ApiResponse.success(service.create(req), "Created individual plan config");
    }

    @PostMapping("/{id}/request-apply")
    public ApiResponse<IndividualPlanConfigDetailResponse> requestApply(
            @PathVariable Long id,
            @Valid @RequestBody IndividualRequestApplyRequest req) {
        return ApiResponse.success(service.requestApply(id, req), "Requested apply for plan config");
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<IndividualPlanConfigDetailResponse> approve(
            @PathVariable Long id,
            @RequestBody IndividualApproveRequest req) {
        return ApiResponse.success(service.approve(id, req), "Approved plan config");
    }

    @PostMapping("/{id}/reject")
    public ApiResponse<IndividualPlanConfigDetailResponse> reject(
            @PathVariable Long id,
            @RequestParam(defaultValue = "system") String actor) {
        return ApiResponse.success(service.reject(id, actor), "Rejected plan config");
    }

    @PostMapping("/{id}/stop")
    public ApiResponse<IndividualPlanConfigDetailResponse> stop(
            @PathVariable Long id,
            @RequestParam(defaultValue = "system") String actor) {
        return ApiResponse.success(service.stop(id, actor), "Stopped plan config");
    }

    @PostMapping("/{id}/deactivate")
    public ApiResponse<IndividualPlanConfigDetailResponse> deactivate(
            @PathVariable Long id,
            @RequestParam(defaultValue = "system") String actor) {
        return ApiResponse.success(service.deactivate(id, actor), "Deactivated plan config");
    }
}
