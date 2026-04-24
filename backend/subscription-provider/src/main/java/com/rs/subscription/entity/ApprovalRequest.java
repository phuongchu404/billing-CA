package com.rs.subscription.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "approval_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private RequestType requestType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private ApprovalStatus status;

    @Column(nullable = false, length = 100)
    private String requestedBy;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String requestPayload;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(length = 100)
    private String reviewedBy;

    @Column(columnDefinition = "TEXT")
    private String reviewNote;

    @Column
    private LocalDateTime reviewedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum RequestType {
        CREATE_PLAN,
        ASSIGN_GROUP_PLAN,
        CANCEL_SUBSCRIPTION,
        SUSPEND_SUBSCRIPTION
    }

    public enum ApprovalStatus {
        PENDING,
        APPROVED,
        DENIED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
        if (status == null) status = ApprovalStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
