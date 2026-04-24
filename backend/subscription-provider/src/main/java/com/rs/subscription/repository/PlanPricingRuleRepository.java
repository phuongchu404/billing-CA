package com.rs.subscription.repository;

import com.rs.subscription.entity.PlanPricingRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanPricingRuleRepository extends JpaRepository<PlanPricingRule, Long> {
    List<PlanPricingRule> findByPlanTemplatePlanTemplateIdOrderBySortOrderAsc(Long planTemplateId);
    List<PlanPricingRule> findByPlanTemplatePlanTemplateIdAndIsActiveTrueOrderBySortOrderAsc(Long planTemplateId);
    Optional<PlanPricingRule> findByPlanPricingRuleIdAndPlanTemplatePlanTemplateId(Long planPricingRuleId, Long planTemplateId);
}
