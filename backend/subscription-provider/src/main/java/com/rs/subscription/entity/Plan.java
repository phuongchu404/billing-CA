package com.rs.subscription.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long planId;

    @Column(unique = true, nullable = false, length = 50)
    private String planCode;

    @Column(nullable = false, length = 150)
    private String planName;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, columnDefinition = "CHAR(3)")
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanType planType;

    @Column(nullable = false)
    private Integer validityDays;

    /** Original amount entered by the user (display only). e.g. 3 when user typed "3 Months". */
    private Integer validityAmount;

    /** Original unit chosen by the user (display only). One of: DAYS, MONTHS, YEARS. */
    @Column(length = 10)
    private String validityUnit;

    @Column(nullable = false)
    private Integer maxSigningQuota;

    /** Maximum number of members allowed in the group. Only meaningful when isGroupPlan=true. */
    private Integer maxMembers;

    /** Optional date from which this plan becomes available (inclusive). */
    private LocalDate effectiveFrom;

    /** Optional date after which this plan is no longer available (inclusive). */
    private LocalDate effectiveTo;

    /** Whether the plan is visible to clients. Hidden plans can still be assigned by operators. */
    @Column(nullable = false)
    private Boolean isVisible;

    @Column(nullable = false)
    private Boolean allowBulkSigning;

    @Column(nullable = false)
    private Boolean allowApiAccess;

    @Column(nullable = false)
    private Boolean isGroupPlan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupMemberValidityMode groupMemberValidityMode;

    @Column(nullable = false)
    private Boolean isActive;

    public enum PlanType {
        /** Subscription expires only when the validity period (end date) passes. */
        VALIDITY_PERIOD,
        /** Subscription expires when EITHER the validity period ends OR the signing quota is exhausted. */
        COMBINED
    }

    public enum GroupMemberValidityMode {
        /** All members share the group subscription's start/end dates. */
        GROUP_FOLLOWS,
        /** Each member gets their own validity window starting from their join date. */
        INDIVIDUAL_START
    }

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
        if (planType == null) planType = PlanType.VALIDITY_PERIOD;
        if (isActive == null) isActive = true;
        if (allowBulkSigning == null) allowBulkSigning = false;
        if (allowApiAccess == null) allowApiAccess = false;
        if (isGroupPlan == null) isGroupPlan = false;
        if (groupMemberValidityMode == null) groupMemberValidityMode = GroupMemberValidityMode.GROUP_FOLLOWS;
        if (isVisible == null) isVisible = true;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
