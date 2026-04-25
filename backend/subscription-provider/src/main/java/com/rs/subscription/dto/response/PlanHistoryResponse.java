package com.rs.subscription.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PlanHistoryResponse {
    private LocalDate applyFrom;
    private LocalDate applyTo;
    private String planName;
    private String assignmentStatus;
    /** Tổng chứng thư đã tạo trong period này */
    private Integer ctsCreated;
    /** % chứng thư đã tạo so với quota */
    private String ctsCreatedPct;
    /** Tổng lượt ký đã dùng */
    private Integer signingUsed;
    /** % lượt ký đã dùng so với quota */
    private String signingUsedPct;
}
