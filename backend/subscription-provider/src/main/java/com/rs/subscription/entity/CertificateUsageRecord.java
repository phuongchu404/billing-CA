package com.rs.subscription.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "certificate_usage_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateUsageRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "certificate_id", nullable = false, length = 200)
    private String certificateId;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_plan_assignment_id")
    private GroupPlanAssignment groupPlanAssignment;

    @Column(name = "usage_type", nullable = false, length = 30)
    private String usageType;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "used_at", nullable = false, updatable = false)
    private LocalDateTime usedAt;

    @PrePersist
    void onCreate() {
        if (usedAt == null) usedAt = LocalDateTime.now();
        if (usageType == null) usageType = "SIGNING";
        if (quantity == null) quantity = 1;
    }
}
