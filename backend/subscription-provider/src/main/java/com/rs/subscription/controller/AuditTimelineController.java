package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
import com.rs.subscription.dto.response.AuditTimelineResponse;
import com.rs.subscription.service.AuditTimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/audit-timelines")
@RequiredArgsConstructor
public class AuditTimelineController {

    private final AuditTimelineService auditTimelineService;

    @GetMapping("/subscriptions/{subscriptionId}")
    public ApiResponse<AuditTimelineResponse> getSubscriptionTimeline(@PathVariable Long subscriptionId) {
        return ApiResponse.success(
            auditTimelineService.getSubscriptionTimeline(subscriptionId),
            "Fetched subscription audit timeline"
        );
    }

    @GetMapping("/group-plan-assignments/{assignmentId}")
    public ApiResponse<AuditTimelineResponse> getGroupAssignmentTimeline(@PathVariable Long assignmentId) {
        return ApiResponse.success(
            auditTimelineService.getGroupAssignmentTimeline(assignmentId),
            "Fetched group assignment audit timeline"
        );
    }

    @GetMapping("/retail-plan-schedules/{scheduleId}")
    public ApiResponse<AuditTimelineResponse> getRetailScheduleTimeline(@PathVariable Long scheduleId) {
        return ApiResponse.success(
            auditTimelineService.getRetailScheduleTimeline(scheduleId),
            "Fetched retail schedule audit timeline"
        );
    }

    @GetMapping("/settlement-statements/{settlementStatementId}")
    public ApiResponse<AuditTimelineResponse> getSettlementTimeline(@PathVariable Long settlementStatementId) {
        return ApiResponse.success(
            auditTimelineService.getSettlementTimeline(settlementStatementId),
            "Fetched settlement audit timeline"
        );
    }
}
