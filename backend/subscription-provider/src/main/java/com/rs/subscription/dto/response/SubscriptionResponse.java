package com.rs.subscription.dto.response;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SubscriptionResponse {
    private Long subscriptionId;
    private String subscriberType;
    private String userId;
    private Long groupId;
    private String planCode;
    private String planName;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate planEffectiveFrom;
    private LocalDate planEffectiveTo;
    private String  planType;
    private Integer planValidityDays;
    private Integer planValidityAmount;
    private String  planValidityUnit;
    private Integer signingQuotaTotal;
    private Integer signingQuotaUsed;
    private String activatedBy;
    private String paymentReference;
    private String certificateId;
    private FeatureFlagsDto featureFlags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class FeatureFlagsDto {
        private Boolean allowBulkSigning;
        private Boolean allowApiAccess;
    }
}
