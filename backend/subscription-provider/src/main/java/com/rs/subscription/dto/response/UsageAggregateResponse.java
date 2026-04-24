package com.rs.subscription.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UsageAggregateResponse {
    private Long usageAggregateId;
    private String aggregateScope;
    private Long scopeId;
    private String periodType;
    private String periodKey;
    private Integer certificatesCreated;
    private Integer signingUsed;
    private Integer activeCertificates;
    private Integer expiredCertificates;
    private Integer revokedCertificates;
    private BigDecimal amountDue;
    private String currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
