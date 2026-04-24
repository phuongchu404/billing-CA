package com.rs.subscription.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PlanResponse {
    private Long planId;
    private String planCode;
    private String planType;
    private String planName;
    private BigDecimal price;
    private String currency;
    private Integer validityDays;
    private Integer validityAmount;
    private String  validityUnit;
    private Integer maxSigningQuota;
    private Integer maxMembers;
    private Boolean allowBulkSigning;
    private Boolean allowApiAccess;
    private Boolean isGroupPlan;
    private String groupMemberValidityMode;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Boolean isVisible;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
