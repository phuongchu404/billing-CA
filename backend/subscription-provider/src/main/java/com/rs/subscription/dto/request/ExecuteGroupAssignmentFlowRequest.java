package com.rs.subscription.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ExecuteGroupAssignmentFlowRequest {
    @NotBlank
    private String actor;

    private String note;
    private Boolean approveNow = true;
    private Boolean activateNow = true;
    private Boolean issueSubscription = true;
    private Long pricingRuleId;
    private String subscriptionStatus = "ACTIVE";
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer signingQuotaTotal;
    private String paymentReference;
    private Long sourceId;
}
