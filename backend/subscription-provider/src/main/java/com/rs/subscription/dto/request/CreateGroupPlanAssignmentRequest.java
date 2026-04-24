package com.rs.subscription.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateGroupPlanAssignmentRequest {
    private Long groupId;

    @NotNull
    private Long planTemplateId;

    private String assignmentStatus = "REQUESTED";

    @NotBlank
    private String requestedBy;

    private LocalDate applyFrom;
    private LocalDate applyTo;

    private String stopReason;
}
