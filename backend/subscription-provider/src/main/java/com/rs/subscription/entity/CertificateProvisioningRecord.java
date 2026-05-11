package com.rs.subscription.entity;

import com.rs.subscription.enums.CommercialEnums;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "certificate_provisioning_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateProvisioningRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Subscription subscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_plan_assignment_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private GroupPlanAssignment groupPlanAssignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pricing_rule_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private PlanPricingRule pricingRule;
    @Column(nullable = false)
    private Long userId;

    @Column(unique = true, nullable = false, length = 36)
    private String requestId;

    @Column(nullable = false, length = 30)
    private String status;

    @Convert(converter = CertTypeConverter.class)
    @Column(nullable = false, columnDefinition = "TINYINT")
    private CertType certType;

    @Column(length = 200)
    private String certificateId;

    @Column(length = 200)
    private String keyId;

    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Integer retryCount;

    @Column(name = "usage_count", nullable = false)
    private Integer usageCount;

    private LocalDateTime lastAttemptedAt;

    @Column(columnDefinition = "TEXT")
    private String failureReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
        if (retryCount == null) retryCount = 0;
        if (usageCount == null) usageCount = 0;
        if (status == null) status = CommercialEnums.ProvisioningStatus.PENDING.name();
        if (certType == null) certType = CertType.INDIVIDUAL;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    public enum CertType {
        INDIVIDUAL(1), INDIVIDUAL_OF_ORGANIZATION(2), ORGANIZATION(3);

        private final int value;
        CertType(int value) { this.value = value; }
        public int getValue() { return value; }
    }
}


