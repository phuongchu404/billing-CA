package com.rs.subscription.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApprovalStepResponse {
    private Long id;
    private Integer stepLevel;
    private String requiredApprovalLevel;
    private String status;
    private String decidedBy;
    private String comment;
    private LocalDateTime decidedAt;
    private LocalDateTime createdAt;
}
