package com.rs.subscription.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ProvisionGroupRequest {

    @NotBlank
    @Size(max = 200)
    private String groupName;

    private List<String> picEmails;

    private List<String> contactEmails;

    @Size(max = 200)
    private String refContractNo;

    @NotBlank
    @Size(max = 200)
    private String planName;

    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;

    @NotBlank
    private String requestedBy;

    @Valid
    @NotEmpty
    private List<PlanPricingRuleRequest> pricingRules;
}
