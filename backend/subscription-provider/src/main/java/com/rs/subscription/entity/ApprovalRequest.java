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

    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private String requestType;

    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private String status;

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

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
        if (status == null) status = "PENDING";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
