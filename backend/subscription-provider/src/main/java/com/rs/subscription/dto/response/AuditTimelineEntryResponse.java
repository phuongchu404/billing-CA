package com.rs.subscription.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class AuditTimelineEntryResponse {
    private String source;
    private String eventType;
    private String title;
    private String actor;
    private String oldStatus;
    private String newStatus;
    private String note;
    private LocalDateTime occurredAt;
    private BigDecimal amount;
    private String currency;
    private Map<String, Object> metadata;
}
