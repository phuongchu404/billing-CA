package com.rs.subscription.dto.request;

import com.rs.subscription.enums.CommercialEnums;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreatePlanTemplateRequest {
    @NotBlank
    private String planCode;

    @NotBlank
    private String planName;

    private String description;

    @NotBlank
    private String customerSegment;

    private String templateScope = CommercialEnums.TemplateScope.PUBLIC.name();
    private String status = CommercialEnums.TemplateStatus.DRAFT.name();
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Boolean isVisible = true;
    private Boolean allowBulkSigning = false;
    private Boolean allowApiAccess = false;
    private String createdBy;

    @Valid
    @NotEmpty
    private List<PlanPricingRuleRequest> pricingRules;
}
