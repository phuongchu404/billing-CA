package com.rs.subscription.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AuditLogResponse {
    private Long id;
    private Long subscriptionId;
    private String actor;
    private String oldStatus;
    private String newStatus;
    private String reason;
    private LocalDateTime createdAt;
}
