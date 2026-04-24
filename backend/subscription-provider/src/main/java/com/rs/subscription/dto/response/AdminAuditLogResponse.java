package com.rs.subscription.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminAuditLogResponse {
    private Long id;
    private String actor;
    private String action;
    private String entityType;
    private String entityId;
    private String details;
    private LocalDateTime createdAt;
}
