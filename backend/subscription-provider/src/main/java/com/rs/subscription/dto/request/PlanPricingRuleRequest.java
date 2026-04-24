package com.rs.subscription.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlanPricingRuleRequest {
    @NotBlank
    private String subjectType;

    @NotNull
    @Min(1)
    private Integer certificateValidityValue;

    @NotBlank
    private String certificateValidityUnit;

    @NotBlank
    private String pricingMetric;

    @NotNull
    @Min(1)
    private Integer rangeMin;

    private Integer rangeMax;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal unitPrice;

    @Size(min = 3, max = 3)
    private String currency = "VND";

    private Integer quotaTotal;

    private Integer sortOrder = 0;

    private Boolean isActive = true;
}
