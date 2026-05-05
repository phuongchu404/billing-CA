package com.rs.subscription.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportHealthResponse {
    private String periodKey;
    private long rawCertificates;
    private long rawSignings;
    private long aggregateCertificates;
    private long aggregateSignings;
    private long certificateDelta;
    private long signingDelta;
    private LocalDateTime lastRollupAt;
    private String lastRollupStatus;
    private Integer lastRollupGroupsUpdated;
    private String lastRollupError;
}
