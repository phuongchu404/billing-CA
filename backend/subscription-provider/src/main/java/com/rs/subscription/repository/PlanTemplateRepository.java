package com.rs.subscription.repository;

import com.rs.subscription.entity.PlanTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PlanTemplateRepository extends JpaRepository<PlanTemplate, Long> {
    Optional<PlanTemplate> findByPlanCode(String planCode);
    boolean existsByPlanCode(String planCode);
    List<PlanTemplate> findByCustomerSegment(String customerSegment);
    List<PlanTemplate> findByCustomerSegmentAndStatus(String customerSegment, String status);

    @Query(value = """
            SELECT DISTINCT pt.*
            FROM plan_templates pt
            LEFT JOIN retail_plan_schedules rps
                ON rps.plan_template_id = pt.plan_template_id
                AND rps.schedule_status IN ('REQUESTED', 'APPROVED', 'ACTIVE')
            WHERE pt.customer_segment = :segment
            AND (
                :uiStatus IS NULL
                OR (:uiStatus = 'UNAVAILABLE' AND pt.status = 'INACTIVE')
                OR (:uiStatus = 'APPLYING'    AND rps.schedule_status = 'ACTIVE')
                OR (:uiStatus = 'PENDING'     AND rps.schedule_status = 'REQUESTED')
                OR (:uiStatus = 'APPROVED'    AND rps.schedule_status = 'APPROVED')
                OR (:uiStatus = 'AVAILABLE'   AND pt.status = 'AVAILABLE' AND rps.retail_plan_schedule_id IS NULL)
            )
            AND (:applyFrom IS NULL OR rps.apply_from = :applyFrom)
            AND (:applyUntil IS NULL OR rps.apply_to = :applyUntil)
            AND (:updatedAtStart IS NULL OR pt.updated_at >= :updatedAtStart)
            AND (:updatedAtEnd   IS NULL OR pt.updated_at <  :updatedAtEnd)
            ORDER BY pt.updated_at DESC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT pt.plan_template_id)
            FROM plan_templates pt
            LEFT JOIN retail_plan_schedules rps
                ON rps.plan_template_id = pt.plan_template_id
                AND rps.schedule_status IN ('REQUESTED', 'APPROVED', 'ACTIVE')
            WHERE pt.customer_segment = :segment
            AND (
                :uiStatus IS NULL
                OR (:uiStatus = 'UNAVAILABLE' AND pt.status = 'INACTIVE')
                OR (:uiStatus = 'APPLYING'    AND rps.schedule_status = 'ACTIVE')
                OR (:uiStatus = 'PENDING'     AND rps.schedule_status = 'REQUESTED')
                OR (:uiStatus = 'APPROVED'    AND rps.schedule_status = 'APPROVED')
                OR (:uiStatus = 'AVAILABLE'   AND pt.status = 'AVAILABLE' AND rps.retail_plan_schedule_id IS NULL)
            )
            AND (:applyFrom IS NULL OR rps.apply_from = :applyFrom)
            AND (:applyUntil IS NULL OR rps.apply_to = :applyUntil)
            AND (:updatedAtStart IS NULL OR pt.updated_at >= :updatedAtStart)
            AND (:updatedAtEnd   IS NULL OR pt.updated_at <  :updatedAtEnd)
            """,
            nativeQuery = true)
    Page<PlanTemplate> findWithFilters(
            @Param("segment") String segment,
            @Param("uiStatus") String uiStatus,
            @Param("applyFrom") LocalDate applyFrom,
            @Param("applyUntil") LocalDate applyUntil,
            @Param("updatedAtStart") LocalDateTime updatedAtStart,
            @Param("updatedAtEnd") LocalDateTime updatedAtEnd,
            Pageable pageable);

    @Query("SELECT MAX(pt.updatedAt) FROM PlanTemplate pt WHERE pt.customerSegment = :segment")
    Optional<LocalDateTime> findMaxUpdatedAtByCustomerSegment(@Param("segment") String segment);
}
