package com.rs.subscription.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class AuditTimelineResponse {
    private String entityType;
    private Long entityId;
    private String currentStatus;
    private List<AuditTimelineEntryResponse> items;
}
