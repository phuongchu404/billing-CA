package com.rs.subscription.dto.response;

import lombok.Data;

@Data
public class ProvisionGroupResponse {
    private GroupDetailResponse group;
    private PlanTemplateResponse planTemplate;
    private GroupPlanAssignmentResponse assignment;
    /** Có giá trị khi request gửi kèm partnerAccount. */
    private UserResponse partnerUser;
}
