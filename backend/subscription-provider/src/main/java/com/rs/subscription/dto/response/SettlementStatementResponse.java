package com.rs.subscription.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SettlementStatementResponse {
    private Long settlementStatementId;
    private Long groupId;
    private String groupCode;
    private String groupName;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String status;
    private Integer totalCertificates;
    private Integer totalSignings;
    private BigDecimal totalAmount;
    private String currency;
    private LocalDateTime generatedAt;
    private String generatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
