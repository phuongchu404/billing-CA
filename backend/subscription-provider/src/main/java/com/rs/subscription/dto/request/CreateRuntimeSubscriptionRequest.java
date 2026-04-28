package com.rs.subscription.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateRuntimeSubscriptionRequest {
    @NotBlank
    private String subscriberType;

    private Long userId;
    private Long groupId;

    @NotNull
    private Long planTemplateId;

    private Long pricingRuleId;
    private Long groupPlanAssignmentId;
    private Long retailPlanScheduleId;

    private String sourceType = "MANUAL";
    private Long sourceId;
    private String status = "PENDING";
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer signingQuotaTotal;
    private Integer signingQuotaUsed = 0;
    private String activatedBy;
    private String paymentReference;
}


