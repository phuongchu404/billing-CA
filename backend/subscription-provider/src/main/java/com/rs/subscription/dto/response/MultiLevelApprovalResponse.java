package com.rs.subscription.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class MultiLevelApprovalResponse {
    private Long id;
    private String requestType;
    private String customerSegment;
    private String status;
    private String requestedBy;
    private String entityType;
    private String entityId;
    private String description;
    private BigDecimal contractValue;
    private Integer totalLevels;
    private Integer currentLevel;
    private Map<String, Object> payload;
    private List<ApprovalStepResponse> steps;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
