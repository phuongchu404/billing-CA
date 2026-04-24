package com.rs.subscription.repository;

import com.rs.subscription.entity.PlanTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanTemplateRepository extends JpaRepository<PlanTemplate, Long> {
    Optional<PlanTemplate> findByPlanCode(String planCode);
    boolean existsByPlanCode(String planCode);
    List<PlanTemplate> findByCustomerSegment(String customerSegment);
    List<PlanTemplate> findByCustomerSegmentAndStatus(String customerSegment, String status);
}
