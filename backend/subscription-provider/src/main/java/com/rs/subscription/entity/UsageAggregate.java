package com.rs.subscription.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "usage_aggregates",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_usage_aggregate_scope_period",
            columnNames = {"aggregate_scope", "scope_id", "period_type", "period_key"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsageAggregate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long usageAggregateId;

    @Column(nullable = false, length = 30)
    private String aggregateScope;

    @Column(nullable = false)
    private Long scopeId;

    @Column(nullable = false, length = 20)
    private String periodType;

    @Column(nullable = false, length = 20)
    private String periodKey;

    @Column(nullable = false)
    private Integer certificatesCreated;

    @Column(nullable = false)
    private Integer signingUsed;

    @Column(nullable = false)
    private Integer activeCertificates;

    @Column(nullable = false)
    private Integer expiredCertificates;

    @Column(nullable = false)
    private Integer revokedCertificates;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amountDue;

    @Column(nullable = false, columnDefinition = "CHAR(3)")
    private String currency;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
        if (certificatesCreated == null) certificatesCreated = 0;
        if (signingUsed == null) signingUsed = 0;
        if (activeCertificates == null) activeCertificates = 0;
        if (expiredCertificates == null) expiredCertificates = 0;
        if (revokedCertificates == null) revokedCertificates = 0;
        if (amountDue == null) amountDue = BigDecimal.ZERO;
        if (currency == null) currency = "VND";
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
