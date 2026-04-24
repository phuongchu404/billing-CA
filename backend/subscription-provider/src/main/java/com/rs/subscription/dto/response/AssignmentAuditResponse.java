package com.rs.subscription.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AssignmentAuditResponse {
    private Long assignmentAuditId;
    private String assignmentType;
    private String action;
    private String oldStatus;
    private String newStatus;
    private String actor;
    private String note;
    private LocalDateTime createdAt;
}
