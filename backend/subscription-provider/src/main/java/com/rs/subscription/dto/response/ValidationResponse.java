package com.rs.subscription.dto.response;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ValidationResponse {
    private Long userId;
    private Long subscriptionId;
    private String status;
    private Integer quotaRemaining;
    private LocalDate endDate;
    private FeatureFlagsDto featureFlags;

    @Data
    public static class FeatureFlagsDto {
        private Boolean allowBulkSigning;
        private Boolean allowApiAccess;
    }
}


