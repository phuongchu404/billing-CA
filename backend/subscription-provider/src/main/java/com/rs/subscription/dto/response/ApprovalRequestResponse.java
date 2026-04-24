package com.rs.subscription.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ApprovalRequestResponse {
    private Long id;
    private String requestType;
    private String status;
    private String requestedBy;
    private String description;
    private Map<String, Object> payload;
    private String reviewedBy;
    private String reviewNote;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
