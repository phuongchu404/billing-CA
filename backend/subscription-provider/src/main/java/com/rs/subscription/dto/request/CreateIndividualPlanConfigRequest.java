package com.rs.subscription.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CreateIndividualPlanConfigRequest {

    @NotBlank(message = "Tên gói cước không được để trống")
    private String name;

    private String applyFrom;
    private String applyUntil;
    private String requestedBy;

    private List<PricingRuleRequest> pricingRules;

    @Data
    public static class PricingRuleRequest {
        /** INDIVIDUAL | ORGANIZATION | INDIVIDUAL_OF_ORG */
        private String subject;
        private Integer durationMonths;
        /** SIGNING_COUNT | CERTIFICATE_COUNT */
        private String condition;
        private Integer minValue;
        private Integer maxValue;
        private Long fee;
        private Integer sortOrder;
    }
}
