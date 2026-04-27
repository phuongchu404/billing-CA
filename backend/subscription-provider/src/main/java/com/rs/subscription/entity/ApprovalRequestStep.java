package com.rs.subscription.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "approval_request_steps",
    indexes = {
        @Index(name = "idx_ars_request_level", columnList = "approval_request_id, step_level"),
        @Index(name = "idx_ars_status", columnList = "status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalRequestStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_request_id", nullable = false)
    private ApprovalRequest approvalRequest;

    @Column(nullable = false)
    private Integer stepLevel;

    // Role yêu cầu để duyệt step này: LEVEL_1, LEVEL_2, LEVEL_3
    @Column(nullable = false, length = 30)
    private String requiredApprovalLevel;

    // PENDING | APPROVED | REJECTED | SKIPPED
    @Column(nullable = false, length = 20)
    private String status;

    @Column(length = 100)
    private String decidedBy;

    @Column(columnDefinition = "TEXT")
    private String comment;

    private LocalDateTime decidedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
        if (status == null) status = "PENDING";
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
