package com.rs.subscription.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "settlement_statements",
    indexes = {
        @Index(name = "idx_settlement_group_period", columnList = "group_id, from_date, to_date"),
        @Index(name = "idx_settlement_status", columnList = "status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long settlementStatementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    private LocalDate fromDate;
    private LocalDate toDate;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private Integer totalCertificates;

    @Column(nullable = false)
    private Integer totalSignings;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, columnDefinition = "CHAR(3)")
    private String currency;

    private LocalDateTime generatedAt;

    @Column(length = 100)
    private String generatedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
        if (status == null) status = "DRAFT";
        if (totalCertificates == null) totalCertificates = 0;
        if (totalSignings == null) totalSignings = 0;
        if (totalAmount == null) totalAmount = BigDecimal.ZERO;
        if (currency == null) currency = "VND";
        if (generatedAt == null && generatedBy != null) generatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
