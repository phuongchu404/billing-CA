package com.rs.subscription.repository;

import com.rs.subscription.entity.PlanSubjectConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanSubjectConfigRepository extends JpaRepository<PlanSubjectConfig, Long> {

    List<PlanSubjectConfig> findByPlanTemplatePlanTemplateId(Long planTemplateId);
}
