package com.rs.subscription.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "assignment_audits",
    indexes = {
        @Index(name = "idx_assignment_audit_group_assignment", columnList = "group_plan_assignment_id"),
        @Index(name = "idx_assignment_audit_retail_schedule", columnList = "retail_plan_schedule_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assignmentAuditId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_plan_assignment_id")
    private GroupPlanAssignment groupPlanAssignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retail_plan_schedule_id")
    private RetailPlanSchedule retailPlanSchedule;

    @Column(nullable = false, length = 30)
    private String assignmentType;

    @Column(nullable = false, length = 30)
    private String action;

    @Column(length = 50)
    private String oldStatus;

    @Column(nullable = false, length = 50)
    private String newStatus;

    @Column(nullable = false, length = 100)
    private String actor;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
