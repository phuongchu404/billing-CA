package com.rs.subscription.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Stores per-subject display metadata for a plan template:
 * icon image (MinIO) and features list.
 * One row per (plan_template_id, subject_type).
 * Display price is computed from PlanPricingRule (min fee of that subject type).
 */
@Entity
@Table(
    name = "plan_subject_config",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_psc_template_subject",
        columnNames = {"plan_template_id", "subject_type"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanSubjectConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_template_id", nullable = false)
    private PlanTemplate planTemplate;

    /** INDIVIDUAL | ORGANIZATION | INDIVIDUAL_OF_ORG */
    @Column(name = "subject_type", nullable = false, length = 30)
    private String subjectType;

    /** MinIO storagePath, e.g. public/images/plans/2026/05/12/uuid.png */
    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    /** Newline-separated list of feature strings shown in public price card. */
    @Column(name = "features_text", columnDefinition = "TEXT")
    private String featuresText;
}
