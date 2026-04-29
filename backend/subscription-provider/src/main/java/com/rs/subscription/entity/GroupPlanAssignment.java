package com.rs.subscription.entity;

import com.rs.subscription.enums.CommercialEnums;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "group_plan_assignments",
    indexes = {
        @Index(name = "idx_gpa_group_status", columnList = "group_id, assignment_status"),
        @Index(name = "idx_gpa_apply_range", columnList = "apply_from, apply_to")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupPlanAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long groupPlanAssignmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_template_id", nullable = false)
    private PlanTemplate planTemplate;

    @Column(nullable = false, length = 30)
    private String assignmentStatus;

    @Column(length = 100)
    private String requestedBy;

    private LocalDateTime requestedAt;

    @Column(length = 100)
    private String approvedBy;

    private LocalDateTime approvedAt;

    @Column(length = 100)
    private String rejectedBy;

    private LocalDateTime rejectedAt;

    private LocalDate applyFrom;
    private LocalDate applyTo;

    private LocalDateTime activatedAt;
    private LocalDateTime stoppedAt;

    @Column(columnDefinition = "TEXT")
    private String stopReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "groupPlanAssignment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AssignmentAudit> audits = new ArrayList<>();

    @PrePersist
    void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
        if (assignmentStatus == null) assignmentStatus = CommercialEnums.AssignmentStatus.REQUESTED.name();
        if (requestedAt == null && requestedBy != null) requestedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
