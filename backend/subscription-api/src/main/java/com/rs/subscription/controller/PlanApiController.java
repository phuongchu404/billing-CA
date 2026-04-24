package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.response.PlanResponse;
import com.rs.subscription.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
@Tag(name = "Plans", description = "Browse available subscription plans")
public class PlanApiController {

    private final PlanService planService;

    @GetMapping
    @Operation(summary = "List all active, visible individual plans")
    public ApiResponse<List<PlanResponse>> list() {
        return ApiResponse.success(planService.getIndividualVisiblePlans(), "Plans retrieved successfully");
    }
}
