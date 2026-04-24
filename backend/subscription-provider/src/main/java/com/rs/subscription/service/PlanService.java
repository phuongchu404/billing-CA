package com.rs.subscription.service;

import com.rs.subscription.dto.request.CreatePlanRequest;
import com.rs.subscription.dto.response.PlanResponse;
import com.rs.subscription.entity.Plan;
import com.rs.subscription.entity.Subscription;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.repository.PlanRepository;
import com.rs.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlanService {

    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;

    public List<PlanResponse> getAllActivePlans() {
        return planRepository.findByIsActiveTrue().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public List<PlanResponse> getIndividualVisiblePlans() {
        return planRepository.findActiveIndividualPlans().stream()
            .filter(p -> Boolean.TRUE.equals(p.getIsVisible()))
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public PlanResponse getPlanByCode(String planCode) {
        Plan plan = planRepository.findByPlanCode(planCode)
            .orElseThrow(() -> new SmsException(ErrorCodes.PLAN_NOT_FOUND, "Plan not found: " + planCode, 404));
        return toResponse(plan);
    }

    @Transactional
    public PlanResponse createPlan(CreatePlanRequest req) {
        if (planRepository.existsByPlanCode(req.getPlanCode())) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED, "Plan code already exists: " + req.getPlanCode(), 400);
        }
        Plan.PlanType planType = parsePlanType(req.getPlanType());
        boolean isGroup = Boolean.TRUE.equals(req.getIsGroupPlan());
        validateQuotaFields(planType, isGroup, req.getMaxSigningQuota(), req.getMaxMembers());
        Plan plan = Plan.builder()
            .planCode(req.getPlanCode())
            .planType(planType)
            .planName(req.getPlanName())
            .price(req.getPrice())
            .currency(req.getCurrency())
            .validityDays(req.getValidityDays())
            .validityAmount(req.getValidityAmount())
            .validityUnit(req.getValidityUnit())
            .maxSigningQuota(planType == Plan.PlanType.COMBINED ? req.getMaxSigningQuota() : Integer.MAX_VALUE)
            .maxMembers(isGroup ? req.getMaxMembers() : null)
            .allowBulkSigning(Boolean.TRUE.equals(req.getAllowBulkSigning()))
            .allowApiAccess(Boolean.TRUE.equals(req.getAllowApiAccess()))
            .isGroupPlan(isGroup)
            .groupMemberValidityMode(parseValidityMode(req.getGroupMemberValidityMode()))
            .effectiveFrom(req.getEffectiveFrom())
            .effectiveTo(req.getEffectiveTo())
            .isVisible(req.getIsVisible() == null || req.getIsVisible())
            .isActive(true)
            .build();
        return toResponse(planRepository.save(plan));
    }

    @Transactional
    public PlanResponse updatePlan(String planCode, CreatePlanRequest req) {
        Plan plan = planRepository.findByPlanCode(planCode)
            .orElseThrow(() -> new SmsException(ErrorCodes.PLAN_NOT_FOUND, "Plan not found: " + planCode, 404));
        Plan.PlanType planType = parsePlanType(req.getPlanType());
        boolean isGroup = Boolean.TRUE.equals(req.getIsGroupPlan());
        validateQuotaFields(planType, isGroup, req.getMaxSigningQuota(), req.getMaxMembers());
        plan.setPlanType(planType);
        plan.setPlanName(req.getPlanName());
        plan.setPrice(req.getPrice());
        plan.setCurrency(req.getCurrency());
        plan.setValidityDays(req.getValidityDays());
        plan.setValidityAmount(req.getValidityAmount());
        plan.setValidityUnit(req.getValidityUnit());
        plan.setMaxSigningQuota(planType == Plan.PlanType.COMBINED ? req.getMaxSigningQuota() : Integer.MAX_VALUE);
        plan.setMaxMembers(isGroup ? req.getMaxMembers() : null);
        plan.setAllowBulkSigning(Boolean.TRUE.equals(req.getAllowBulkSigning()));
        plan.setAllowApiAccess(Boolean.TRUE.equals(req.getAllowApiAccess()));
        plan.setIsGroupPlan(isGroup);
        plan.setGroupMemberValidityMode(parseValidityMode(req.getGroupMemberValidityMode()));
        plan.setEffectiveFrom(req.getEffectiveFrom());
        plan.setEffectiveTo(req.getEffectiveTo());
        plan.setIsVisible(req.getIsVisible() == null || req.getIsVisible());
        return toResponse(planRepository.save(plan));
    }


    @Transactional
    public void deactivatePlan(String planCode) {
        Plan plan = planRepository.findByPlanCode(planCode)
            .orElseThrow(() -> new SmsException(ErrorCodes.PLAN_NOT_FOUND, "Plan not found: " + planCode, 404));
        long activeCount = subscriptionRepository.countByStatus(Subscription.SubscriptionStatus.ACTIVE);
        // Check if any active subscriptions use this plan
        boolean inUse = subscriptionRepository.findAll().stream()
            .anyMatch(s -> s.getPlan().getPlanId().equals(plan.getPlanId())
                && s.getStatus() == Subscription.SubscriptionStatus.ACTIVE);
        if (inUse) {
            throw new SmsException(ErrorCodes.PLAN_IN_USE, "Plan has active subscriptions and cannot be deactivated", 409);
        }
        plan.setIsActive(false);
        planRepository.save(plan);
    }

    private void validateQuotaFields(Plan.PlanType planType, boolean isGroup, Integer maxSigningQuota, Integer maxMembers) {
        if (planType == Plan.PlanType.COMBINED && (maxSigningQuota == null || maxSigningQuota < 1)) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED, "maxSigningQuota is required for COMBINED plan type", 400);
        }
        // maxMembers is optional for group plans; null means unlimited
    }

    private Plan.PlanType parsePlanType(String value) {
        if (value == null) return Plan.PlanType.VALIDITY_PERIOD;
        try {
            return Plan.PlanType.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED, "Invalid planType: " + value, 400);
        }
    }

    private Plan.GroupMemberValidityMode parseValidityMode(String value) {
        if (value == null) return Plan.GroupMemberValidityMode.GROUP_FOLLOWS;
        try {
            return Plan.GroupMemberValidityMode.valueOf(value);
        } catch (IllegalArgumentException e) {
            return Plan.GroupMemberValidityMode.GROUP_FOLLOWS;
        }
    }

    public Plan findPlanByCodeEntity(String planCode) {
        return planRepository.findByPlanCode(planCode)
            .orElseThrow(() -> new SmsException(ErrorCodes.PLAN_NOT_FOUND, "Plan not found: " + planCode, 404));
    }

    private PlanResponse toResponse(Plan plan) {
        PlanResponse r = new PlanResponse();
        r.setPlanId(plan.getPlanId());
        r.setPlanCode(plan.getPlanCode());
        r.setPlanType(plan.getPlanType() != null ? plan.getPlanType().name() : Plan.PlanType.VALIDITY_PERIOD.name());
        r.setPlanName(plan.getPlanName());
        r.setPrice(plan.getPrice());
        r.setCurrency(plan.getCurrency());
        r.setValidityDays(plan.getValidityDays());
        r.setValidityAmount(plan.getValidityAmount());
        r.setValidityUnit(plan.getValidityUnit());
        r.setMaxSigningQuota(plan.getMaxSigningQuota());
        r.setMaxMembers(plan.getMaxMembers());
        r.setAllowBulkSigning(plan.getAllowBulkSigning());
        r.setAllowApiAccess(plan.getAllowApiAccess());
        r.setIsGroupPlan(plan.getIsGroupPlan());
        r.setGroupMemberValidityMode(plan.getGroupMemberValidityMode() != null
            ? plan.getGroupMemberValidityMode().name() : Plan.GroupMemberValidityMode.GROUP_FOLLOWS.name());
        r.setEffectiveFrom(plan.getEffectiveFrom());
        r.setEffectiveTo(plan.getEffectiveTo());
        r.setIsVisible(plan.getIsVisible() != null ? plan.getIsVisible() : true);
        r.setIsActive(plan.getIsActive());
        r.setCreatedAt(plan.getCreatedAt());
        r.setUpdatedAt(plan.getUpdatedAt());
        return r;
    }
}
