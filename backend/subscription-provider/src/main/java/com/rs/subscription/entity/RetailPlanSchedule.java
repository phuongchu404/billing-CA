package com.rs.subscription.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "retail_plan_schedules",
    indexes = {
        @Index(name = "idx_rps_status", columnList = "schedule_status"),
        @Index(name = "idx_rps_apply_range", columnList = "apply_from, apply_to")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RetailPlanSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long retailPlanScheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_template_id", nullable = false)
    private PlanTemplate planTemplate;

    @Column(nullable = false, length = 30)
    private String scheduleStatus;

    private LocalDate applyFrom;
    private LocalDate applyTo;

    @Column(length = 100)
    private String requestedBy;

    private LocalDateTime requestedAt;

    @Column(length = 100)
    private String approvedBy;

    private LocalDateTime approvedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
        if (scheduleStatus == null) scheduleStatus = "AVAILABLE";
        if (requestedAt == null && requestedBy != null) requestedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
