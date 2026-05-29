package com.rs.subscription.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpsertApprovalLevelConfigRequest {

    @NotBlank
    private String customerSegment;

    private BigDecimal minValue;

    private BigDecimal maxValue;

    @NotNull
    @Min(1) @Max(3)
    private Integer requiredLevels;

    private String description;

    private Boolean isActive = true;
}
