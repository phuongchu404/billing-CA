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
    // Trả về sau khi "Yêu cầu áp dụng" để frontend điều hướng đến màn approval
    private Long approvalRequestId;
    /** Per-subject display configs (icon, features, displayPrice). */
    private List<SubjectConfigRow> subjectConfigs;

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
        private Long totalFee;
        private Integer sortOrder;
    }

    @Data
    public static class StatusHistoryRow {
        /** AVAILABLE | UNAVAILABLE | PENDING | APPROVED | APPLYING */
        private String status;
        private String updatedAt;
        private String updatedBy;
    }

    @Data
    public static class SubjectConfigRow {
        /** INDIVIDUAL | ORGANIZATION | INDIVIDUAL_OF_ORG */
        private String subjectType;
        /** Full public URL of the icon (resolved from MinIO storagePath). */
        private String iconUrl;
        private String featuresText;
    }
}
