package com.rs.subscription.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RetailPlanScheduleResponse {
    private Long retailPlanScheduleId;
    private Long planTemplateId;
    private String planCode;
    private String planName;
    private String scheduleStatus;
    private LocalDate applyFrom;
    private LocalDate applyTo;
    private String requestedBy;
    private LocalDateTime requestedAt;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private List<AssignmentAuditResponse> audits;
}
