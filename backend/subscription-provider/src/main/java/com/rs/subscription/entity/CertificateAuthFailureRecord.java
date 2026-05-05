package com.rs.subscription.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "certificate_auth_failure_records",
    indexes = {
        @Index(name = "idx_cafr_subscription_failed", columnList = "subscription_id, failed_at"),
        @Index(name = "idx_cafr_user_failed", columnList = "user_id, failed_at"),
        @Index(name = "idx_cafr_certificate_failed", columnList = "certificate_id, failed_at"),
        @Index(name = "idx_cafr_type_failed", columnList = "failure_type, failed_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateAuthFailureRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @Column(name = "certificate_id", length = 200)
    private String certificateId;

    @Column(name = "failure_type", nullable = false, length = 30)
    private String failureType;

    @Column(name = "reason_code", length = 100)
    private String reasonCode;

    @Column(name = "failed_at", nullable = false)
    private LocalDateTime failedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (failedAt == null) failedAt = LocalDateTime.now();
        createdAt = LocalDateTime.now();
    }
}
