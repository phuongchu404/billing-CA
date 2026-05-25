package com.rs.subscription.entity;

import com.rs.subscription.enums.CommercialEnums;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "plan_pricing_rules",
    indexes = {
        @Index(name = "idx_ppr_template", columnList = "plan_template_id"),
        @Index(name = "idx_ppr_active_sort", columnList = "plan_template_id, is_active, sort_order")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanPricingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long planPricingRuleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_template_id", nullable = false)
    private PlanTemplate planTemplate;

    @Column(nullable = false, length = 30)
    private String subjectType;

    @Column(nullable = false)
    private Integer certificateValidityValue;

    @Column(nullable = false, length = 20)
    private String certificateValidityUnit;

    @Column(nullable = false, length = 30)
    private String pricingMetric;

    @Column(nullable = false)
    private Integer rangeMin;

    private Integer rangeMax;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalPrice;

    @Column(nullable = false, columnDefinition = "CHAR(3)")
    private String currency;

    private Integer quotaTotal;

    @Column(nullable = false)
    private Integer sortOrder;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
        if (certificateValidityUnit == null) certificateValidityUnit = CommercialEnums.ValidityUnit.MONTH.name();
        if (pricingMetric == null) pricingMetric = CommercialEnums.PricingMetric.CERTIFICATE_COUNT.name();
        if (currency == null) currency = "VND";
        if (sortOrder == null) sortOrder = 0;
        if (isActive == null) isActive = true;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
