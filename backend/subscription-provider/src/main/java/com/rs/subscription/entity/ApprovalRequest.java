package com.rs.subscription.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "approval_requests",
    indexes = {
        @Index(name = "idx_ar_status", columnList = "status"),
        @Index(name = "idx_ar_entity", columnList = "entity_type, entity_id")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private String requestType;

    // Multi-level: DRAFT | IN_APPROVAL | NEED_REVISION | APPROVED | REJECTED
    // Legacy single-level: PENDING | APPROVED | DENIED (vẫn tương thích)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private String status;

    // INDIVIDUAL (retail) hoặc GROUP (đại lý)
    @Column(nullable = false, length = 20)
    private String customerSegment;

    @Column(nullable = false, length = 100)
    private String requestedBy;

    @Column(nullable = false, length = 50)
    private String entityType;

    @Column(nullable = false, length = 150)
    private String entityId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String requestPayload;

    @Column(nullable = false, length = 500)
    private String description;

    // Giá trị hợp đồng dùng để tính số cấp duyệt
    @Column(precision = 20, scale = 2)
    private BigDecimal contractValue;

    // Multi-level approval fields
    @Column(nullable = false)
    private Integer totalLevels;

    @Column(nullable = false)
    private Integer currentLevel;

    // Legacy single-level fields (giữ để tương thích)
    @Column(length = 100)
    private String reviewedBy;

    @Column(columnDefinition = "TEXT")
    private String reviewNote;

    @Column
    private LocalDateTime reviewedAt;

    @OneToMany(mappedBy = "approvalRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ApprovalRequestStep> steps = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
        if (status == null) status = "DRAFT";
        if (totalLevels == null) totalLevels = 1;
        if (currentLevel == null) currentLevel = 0;
        if (customerSegment == null) customerSegment = "INDIVIDUAL";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
