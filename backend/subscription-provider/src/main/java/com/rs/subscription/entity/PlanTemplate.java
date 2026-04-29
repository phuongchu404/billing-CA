package com.rs.subscription.entity;

import com.rs.subscription.enums.CommercialEnums;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plan_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long planTemplateId;

    @Column(nullable = false, unique = true, length = 50)
    private String planCode;

    @Column(nullable = false, length = 150)
    private String planName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 30)
    private String customerSegment;

    @Column(nullable = false, length = 30)
    private String templateScope;

    @Column(nullable = false, length = 30)
    private String status;

    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;

    @Column(nullable = false)
    private Boolean isVisible;

    @Column(nullable = false)
    private Boolean allowBulkSigning;

    @Column(nullable = false)
    private Boolean allowApiAccess;

    @Column(length = 100)
    private String createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cloned_from_template_id")
    private PlanTemplate clonedFromTemplate;

    @Column(nullable = false)
    private Integer versionNo;

    @OneToMany(mappedBy = "planTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PlanPricingRule> pricingRules = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
        if (customerSegment == null) customerSegment = CommercialEnums.CustomerSegment.INDIVIDUAL.name();
        if (templateScope == null) templateScope = CommercialEnums.TemplateScope.PUBLIC.name();
        if (status == null) status = CommercialEnums.TemplateStatus.DRAFT.name();
        if (isVisible == null) isVisible = true;
        if (allowBulkSigning == null) allowBulkSigning = false;
        if (allowApiAccess == null) allowApiAccess = false;
        if (versionNo == null) versionNo = 1;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
