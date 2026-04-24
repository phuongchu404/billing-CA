package com.rs.subscription.repository;

import com.rs.subscription.entity.RetailPlanSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RetailPlanScheduleRepository extends JpaRepository<RetailPlanSchedule, Long> {
    List<RetailPlanSchedule> findAllByOrderByCreatedAtDesc();
    List<RetailPlanSchedule> findByScheduleStatusOrderByCreatedAtDesc(String scheduleStatus);
}
