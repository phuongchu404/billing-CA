package com.rs.subscription.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RuntimeSubscriptionResponse {
    private Long subscriptionId;
    private String subscriberType;
    private Long userId;
    private Long groupId;
    private Long planTemplateId;
    private String planCode;
    private String planName;
    private Long pricingRuleId;
    private Long groupPlanAssignmentId;
    private Long retailPlanScheduleId;
    private String sourceType;
    private Long sourceId;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer signingQuotaTotal;
    private Integer signingQuotaUsed;
    private String activatedBy;
    private String paymentReference;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


