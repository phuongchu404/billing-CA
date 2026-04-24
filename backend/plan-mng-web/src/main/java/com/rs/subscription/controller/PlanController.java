package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.request.CreatePlanRequest;
import com.rs.subscription.dto.response.PlanResponse;
import com.rs.subscription.service.AdminAuditLogService;
import com.rs.subscription.service.PlanService;
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
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
@Tag(name = "Plans", description = "Subscription plan management")
public class PlanController {

    private final PlanService planService;
    private final AdminAuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "List all active plans")
    public ApiResponse<List<PlanResponse>> getAllPlans() {
        return ApiResponse.success(planService.getAllActivePlans(), "Plans retrieved successfully");
    }

    @GetMapping("/{planCode}")
    @Operation(summary = "Get plan by code")
    public ApiResponse<PlanResponse> getPlan(@PathVariable String planCode) {
        return ApiResponse.success(planService.getPlanByCode(planCode), "Plan retrieved successfully");
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Create new plan")
    public ApiResponse<PlanResponse> createPlan(@Valid @RequestBody CreatePlanRequest req,
                                                 @AuthenticationPrincipal String adminId) {
        PlanResponse plan = planService.createPlan(req);
        auditLogService.log(adminId, "CREATE_PLAN", "PLAN", plan.getPlanCode(),
                "Created plan '" + plan.getPlanName() + "'");
        return ApiResponse.success(plan, "Plan created successfully");
    }

    @PutMapping("/{planCode}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Update plan")
    public ApiResponse<PlanResponse> updatePlan(@PathVariable String planCode,
                                                 @Valid @RequestBody CreatePlanRequest req,
                                                 @AuthenticationPrincipal String adminId) {
        PlanResponse plan = planService.updatePlan(planCode, req);
        auditLogService.log(adminId, "UPDATE_PLAN", "PLAN", planCode,
                "Updated plan '" + planCode + "'");
        return ApiResponse.success(plan, "Plan updated successfully");
    }

    @PatchMapping("/{planCode}/deactivate")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Deactivate plan")
    public ApiResponse<Void> deactivatePlan(@PathVariable String planCode,
                                             @AuthenticationPrincipal String adminId) {
        planService.deactivatePlan(planCode);
        auditLogService.log(adminId, "DEACTIVATE_PLAN", "PLAN", planCode,
                "Deactivated plan '" + planCode + "'");
        return ApiResponse.success("Plan deactivated successfully");
    }
}
