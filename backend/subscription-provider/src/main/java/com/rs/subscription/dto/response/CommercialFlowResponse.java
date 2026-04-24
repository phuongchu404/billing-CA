package com.rs.subscription.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CommercialFlowResponse {
    private String flowType;
    private Long entityId;
    private String finalStatus;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Integer totalCertificates;
    private Integer totalSignings;
    private BigDecimal totalAmount;
    private String currency;
    private GroupPlanAssignmentResponse groupPlanAssignment;
    private RetailPlanScheduleResponse retailPlanSchedule;
    private RuntimeSubscriptionResponse subscription;
    private SettlementStatementResponse settlementStatement;
}
