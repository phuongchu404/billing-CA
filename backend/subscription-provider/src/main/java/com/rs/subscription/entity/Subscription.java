package com.rs.subscription.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subscriptionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriberType subscriberType;

    @Column(length = 100)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    private LocalDate startDate;
    private LocalDate endDate;

    @Column(nullable = false)
    private Integer signingQuotaTotal;

    @Column(nullable = false)
    private Integer signingQuotaUsed;

    @Column(length = 100)
    private String activatedBy;

    @Column(length = 200)
    private String paymentReference;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
        if (signingQuotaUsed == null) signingQuotaUsed = 0;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum SubscriberType { INDIVIDUAL, GROUP }

    public enum SubscriptionStatus { PENDING, ACTIVE, EXPIRED, CANCELLED, SUSPENDED }
}
