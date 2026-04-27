package com.rs.subscription.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Cấu hình số cấp duyệt theo loại khách hàng và ngưỡng giá trị.
 *
 * Ví dụ cho INDIVIDUAL (retail):
 *   minValue=null, maxValue=5_000_000     → requiredLevels=1
 *   minValue=5_000_000, maxValue=50_000_000 → requiredLevels=2
 *   minValue=50_000_000, maxValue=null    → requiredLevels=3
 *
 * Ví dụ cho GROUP (đại lý):
 *   minValue=null, maxValue=50_000_000       → requiredLevels=1
 *   minValue=50_000_000, maxValue=500_000_000 → requiredLevels=2
 *   minValue=500_000_000, maxValue=null      → requiredLevels=3
 */
@Entity
@Table(
    name = "approval_level_configs",
    indexes = {
        @Index(name = "idx_alc_segment", columnList = "customer_segment")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalLevelConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // INDIVIDUAL hoặc GROUP
    @Column(nullable = false, length = 20)
    private String customerSegment;

    // Giá trị tối thiểu (null = không giới hạn dưới)
    @Column(precision = 20, scale = 2)
    private BigDecimal minValue;

    // Giá trị tối đa (null = không giới hạn trên)
    @Column(precision = 20, scale = 2)
    private BigDecimal maxValue;

    // Số cấp duyệt yêu cầu
    @Column(nullable = false)
    private Integer requiredLevels;

    @Column(length = 200)
    private String description;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
        if (isActive == null) isActive = true;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
