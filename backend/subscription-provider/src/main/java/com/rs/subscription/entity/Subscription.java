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

    @Column(nullable = false, length = 20)
    private String subscriberType;

    @Convert(converter = UuidBinaryConverter.class)
    @Column(columnDefinition = "BINARY(16)")
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_template_id", nullable = false)
    private PlanTemplate planTemplate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pricing_rule_id")
    private PlanPricingRule pricingRule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_plan_assignment_id")
    private GroupPlanAssignment groupPlanAssignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retail_plan_schedule_id")
    private RetailPlanSchedule retailPlanSchedule;

    @Column(nullable = false, length = 30)
    private String sourceType;

    @Column
    private Long sourceId;

    @Column(nullable = false, length = 30)
    private String status;

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
        if (sourceType == null) sourceType = "MANUAL";
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
