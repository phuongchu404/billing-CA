package com.rs.subscription.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateIndividualPlanConfigRequest {

    @NotBlank(message = "Tên gói cước không được để trống")
    private String name;

    private String applyFrom;
    private String applyUntil;
    private String requestedBy;

    @Valid
    private List<PricingRuleRequest> pricingRules;

    /** Per-subject display config: icon, features, displayPrice for each card in frontend-public. */
    private List<SubjectConfigRequest> subjectConfigs;

    @Data
    public static class PricingRuleRequest {
        /** INDIVIDUAL | ORGANIZATION | INDIVIDUAL_OF_ORG */
        private String subject;

        @NotNull(message = "Số tháng thời hạn chứng thư không được để trống")
        @Min(value = 1, message = "Số tháng thời hạn chứng thư phải lớn hơn 0")
        private Integer durationMonths;

        /** SIGNING_COUNT | CERTIFICATE_COUNT */
        @NotBlank(message = "Điều kiện tính phí không được để trống")
        private String condition;

        @Min(value = 1, message = "Giá trị min phải lớn hơn 0")
        private Integer minValue;

        private Integer maxValue;

        @Min(value = 0, message = "Phí/điều kiện không được âm")
        private Long fee;

        @NotNull(message = "Tổng tiền không được để trống")
        @Min(value = 0, message = "Tổng tiền không được âm")
        private Long totalFee;

        private Integer sortOrder;
    }

    @Data
    public static class SubjectConfigRequest {
        /** INDIVIDUAL | ORGANIZATION | INDIVIDUAL_OF_ORG */
        private String subjectType;
        /** MinIO storagePath returned by /api/v1/upload/plan-icon */
        private String iconUrl;
        /** Newline-separated list of features shown in public price card */
        private String featuresText;
    }
}
