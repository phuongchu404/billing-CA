package com.rs.subscription.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpsertUsageAggregateRequest {
    @NotBlank
    private String aggregateScope;

    @NotNull
    private Long scopeId;

    @NotBlank
    private String periodType;

    @NotBlank
    private String periodKey;

    private Integer certificatesCreated = 0;
    private Integer signingUsed = 0;
    private Integer activeCertificates = 0;
    private Integer expiredCertificates = 0;
    private Integer revokedCertificates = 0;
    private BigDecimal amountDue = BigDecimal.ZERO;
    private String currency = "VND";
}
