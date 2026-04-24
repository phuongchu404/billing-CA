package com.rs.subscription.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PlanTemplateResponse {
    private Long planTemplateId;
    private String planCode;
    private String planName;
    private String description;
    private String customerSegment;
    private String templateScope;
    private String status;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Boolean isVisible;
    private Boolean allowBulkSigning;
    private Boolean allowApiAccess;
    private String createdBy;
    private Long clonedFromTemplateId;
    private Integer versionNo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PlanPricingRuleResponse> pricingRules;
}
