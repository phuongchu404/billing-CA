package com.rs.subscription.dto.response;

import lombok.Data;

@Data
public class ProvisionGroupResponse {
    private GroupDetailResponse group;
    private PlanTemplateResponse planTemplate;
    private GroupPlanAssignmentResponse assignment;
}
