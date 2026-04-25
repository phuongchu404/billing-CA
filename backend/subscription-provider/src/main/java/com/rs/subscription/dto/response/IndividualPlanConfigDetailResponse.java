package com.rs.subscription.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class IndividualPlanConfigDetailResponse {
    private Long id;
    private String name;
    /** AVAILABLE | UNAVAILABLE | PENDING | APPROVED | APPLYING */
    private String status;
    private String applyFrom;
    private String applyUntil;
    private String applyHistory;
    private String createdBy;
    private String createdAt;
    private String updatedAt;
    private List<PricingRuleRow> pricingRules;
    private List<StatusHistoryRow> statusHistory;

    @Data
    public static class PricingRuleRow {
        private Long id;
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

    @Data
    public static class StatusHistoryRow {
        /** AVAILABLE | UNAVAILABLE | PENDING | APPROVED | APPLYING */
        private String status;
        private String updatedAt;
        private String updatedBy;
    }
}
