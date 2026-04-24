package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.request.ExecuteGroupAssignmentFlowRequest;
import com.rs.subscription.dto.request.ExecuteRetailPlanFlowRequest;
import com.rs.subscription.dto.request.GenerateSettlementFlowRequest;
import com.rs.subscription.dto.response.CommercialFlowResponse;
import com.rs.subscription.service.CommercialOrchestrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/commercial-flows")
@RequiredArgsConstructor
public class CommercialOrchestrationController {

    private final CommercialOrchestrationService commercialOrchestrationService;

    @PostMapping("/group-assignments/{assignmentId}/execute")
    public ApiResponse<CommercialFlowResponse> executeGroupAssignmentFlow(
        @PathVariable Long assignmentId,
        @Valid @RequestBody ExecuteGroupAssignmentFlowRequest request
    ) {
        return ApiResponse.success(
            commercialOrchestrationService.executeGroupAssignmentFlow(assignmentId, request),
            "Executed group assignment flow"
        );
    }

    @PostMapping("/retail-plan-schedules/{scheduleId}/execute")
    public ApiResponse<CommercialFlowResponse> executeRetailPlanFlow(
        @PathVariable Long scheduleId,
        @Valid @RequestBody ExecuteRetailPlanFlowRequest request
    ) {
        return ApiResponse.success(
            commercialOrchestrationService.executeRetailPlanFlow(scheduleId, request),
            "Executed retail plan flow"
        );
    }

    @PostMapping("/groups/{groupId}/settlement")
    public ApiResponse<CommercialFlowResponse> generateSettlement(
        @PathVariable Long groupId,
        @Valid @RequestBody GenerateSettlementFlowRequest request
    ) {
        return ApiResponse.success(
            commercialOrchestrationService.generateSettlement(groupId, request),
            "Generated settlement flow"
        );
    }
}
