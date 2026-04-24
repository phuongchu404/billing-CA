package com.rs.subscription.dto.response;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SubscriptionVerifyResponse {
    private Long subscriptionId;
    private String userId;
    private String status;
    private boolean valid;
    private String invalidReason;
    private String planCode;
    private String planName;
    private String planType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer signingQuotaTotal;    // null = unlimited (VALIDITY_PERIOD plans)
    private Integer signingQuotaUsed;
    private Integer signingQuotaRemaining;
}
