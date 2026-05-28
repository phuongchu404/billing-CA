package com.rs.subscription.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GroupPlanAssignmentResponse {
    private Long groupPlanAssignmentId;
    private Long groupId;
    private String groupCode;
    private String groupName;
    private Long planTemplateId;
    private String planCode;
    private String planName;
    private String assignmentStatus;
    private String requestedBy;
    private LocalDateTime requestedAt;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String rejectedBy;
    private LocalDateTime rejectedAt;
    private LocalDate applyFrom;
    private LocalDate applyTo;
    private LocalDateTime activatedAt;
    private LocalDateTime stoppedAt;
    private String stopReason;
    private LocalDateTime updatedAt;
    private List<AssignmentAuditResponse> audits;
    // ID của ApprovalRequest multi-level được tạo khi submit yêu cầu áp dụng
    private Long approvalRequestId;
}
