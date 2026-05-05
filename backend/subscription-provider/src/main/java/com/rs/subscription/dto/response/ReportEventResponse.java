package com.rs.subscription.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportEventResponse {
    private Long id;
    private String eventType;
    private Long userId;
    private Long subscriptionId;
    private Long groupPlanAssignmentId;
    private String certificateId;
    private String documentId;
    private String usageType;
    private Integer quantity;
    private String status;
    private String failureType;
    private String reasonCode;
    private LocalDateTime eventAt;
}
