package com.rs.subscription.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApprovalLevelConfigResponse {
    private Long id;
    private String customerSegment;
    private BigDecimal minValue;
    private BigDecimal maxValue;
    private Integer requiredLevels;
    private String description;
    private Boolean isActive;
}
