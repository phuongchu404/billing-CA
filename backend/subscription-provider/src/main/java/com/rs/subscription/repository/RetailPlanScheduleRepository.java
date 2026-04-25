package com.rs.subscription.repository;

import com.rs.subscription.entity.RetailPlanSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
