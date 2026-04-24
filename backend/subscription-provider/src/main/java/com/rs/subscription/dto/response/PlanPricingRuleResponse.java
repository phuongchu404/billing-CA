package com.rs.subscription.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlanPricingRuleResponse {
    private Long planPricingRuleId;
    private String subjectType;
    private Integer certificateValidityValue;
    private String certificateValidityUnit;
    private String pricingMetric;
    private Integer rangeMin;
    private Integer rangeMax;
    private BigDecimal unitPrice;
    private String currency;
    private Integer quotaTotal;
    private Integer sortOrder;
    private Boolean isActive;
}
