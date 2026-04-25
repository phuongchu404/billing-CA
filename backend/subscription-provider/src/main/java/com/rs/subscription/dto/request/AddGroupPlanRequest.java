package com.rs.subscription.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AddGroupPlanRequest {

    @NotBlank
    @Size(max = 200)
    private String planName;

    private LocalDate applyFrom;

    private LocalDate applyTo;

    @NotBlank
    private String requestedBy;

    @Valid
    @NotEmpty
    private List<PlanPricingRuleRequest> pricingRules;
}
