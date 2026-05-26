package com.rs.subscription.repository;

import com.rs.subscription.entity.RetailPlanSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RetailPlanScheduleRepository extends JpaRepository<RetailPlanSchedule, Long> {
    List<RetailPlanSchedule> findAllByOrderByCreatedAtDesc();
    List<RetailPlanSchedule> findByScheduleStatusOrderByCreatedAtDesc(String scheduleStatus);

    Optional<RetailPlanSchedule> findTopByPlanTemplatePlanTemplateIdAndScheduleStatusInOrderByCreatedAtDesc(
            Long planTemplateId, List<String> statuses);

    List<RetailPlanSchedule> findByPlanTemplatePlanTemplateIdOrderByCreatedAtDesc(Long planTemplateId);

    Optional<RetailPlanSchedule> findTopByScheduleStatusOrderByApplyFromAsc(String scheduleStatus);

    Optional<RetailPlanSchedule> findTopByScheduleStatus(String scheduleStatus);

    List<RetailPlanSchedule> findByScheduleStatusInOrderByApplyFromAsc(List<String> statuses);

    @Query("""
            SELECT DISTINCT s FROM RetailPlanSchedule s
            JOIN FETCH s.planTemplate t
            LEFT JOIN FETCH t.pricingRules
            WHERE s.scheduleStatus IN :statuses AND s.applyFrom <= :today
            ORDER BY s.applyFrom ASC
            """)
    List<RetailPlanSchedule> findCurrentlyApplicableWithPricingRules(
            @Param("statuses") List<String> statuses, @Param("today") LocalDate today);

    @Query("""
            SELECT DISTINCT s FROM RetailPlanSchedule s
            JOIN FETCH s.planTemplate t
            LEFT JOIN FETCH t.subjectConfigs
            WHERE s.scheduleStatus IN :statuses AND s.applyFrom <= :today
            ORDER BY s.applyFrom ASC
            """)
    List<RetailPlanSchedule> findCurrentlyApplicableWithSubjectConfigs(
            @Param("statuses") List<String> statuses, @Param("today") LocalDate today);
}
