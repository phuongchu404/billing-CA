package com.rs.subscription.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssignGroupPlanRequest {
    @NotBlank(message = "Plan code is required")
    private String planCode;
    private String activatedBy;
}
