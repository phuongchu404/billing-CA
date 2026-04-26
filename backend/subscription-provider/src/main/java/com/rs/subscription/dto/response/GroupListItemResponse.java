package com.rs.subscription.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class GroupListItemResponse {
    private Long groupId;
    private String groupCode;
    private String groupName;
    private String status;
    /** Tên gói cước đang ACTIVE (null nếu chưa có) */
    private String currentPlan;
    /** Ngày kết thúc gói ACTIVE */
    private LocalDate applyUntil;
    /** Tổng chứng thư đã tạo (tổng hợp từ usage_aggregates scope GROUP_ASSIGNMENT) */
    private Integer ctsCreated;
    /** % chứng thư đã tạo so với quota (null nếu không giới hạn) */
    private String ctsCreatedPct;
    /** Tổng lượt ký đã dùng */
    private Integer signingUsed;
    /** % lượt ký đã dùng so với quota (null nếu không giới hạn) */
    private String signingUsedPct;
    private LocalDateTime updatedAt;
    /** userId nhân viên phụ trách (null nếu chưa gán) */
    private String ownerUserId;
    private String ownerName;
}
