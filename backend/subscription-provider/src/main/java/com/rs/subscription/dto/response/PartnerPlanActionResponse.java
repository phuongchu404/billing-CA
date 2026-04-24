package com.rs.subscription.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PartnerPlanActionResponse {
    private Long id;
    private Long subscriptionId;
    private String planCode;
    private String planName;
    private String action;
    private String actor;
    private String oldStatus;
    private String newStatus;
    private String reason;
    private LocalDateTime createdAt;
}
