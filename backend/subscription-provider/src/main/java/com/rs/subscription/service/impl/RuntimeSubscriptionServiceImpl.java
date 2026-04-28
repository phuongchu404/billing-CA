package com.rs.subscription.service.impl;

import com.rs.subscription.service.*;

import com.rs.subscription.aop.TrackSubscriptionAudit;
import com.rs.subscription.dto.request.CreateRuntimeSubscriptionRequest;
import com.rs.subscription.dto.response.RuntimeSubscriptionResponse;
import com.rs.subscription.enums.CommercialEnums;
import com.rs.subscription.entity.*;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.repository.GroupRepository;
import com.rs.subscription.repository.PlanPricingRuleRepository;
import com.rs.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RuntimeSubscriptionServiceImpl implements RuntimeSubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final GroupRepository groupRepository;
    private final PlanPricingRuleRepository planPricingRuleRepository;
    private final PlanTemplateService planTemplateService;
    private final GroupPlanAssignmentService groupPlanAssignmentService;
    private final RetailPlanScheduleService retailPlanScheduleService;

    public List<RuntimeSubscriptionResponse> listAll() {
        return subscriptionRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    public RuntimeSubscriptionResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    @Transactional
    @TrackSubscriptionAudit(
        subscriptionId = "#result.subscriptionId",
        actor = "#p0.activatedBy",
        reason = "'Subscription issued'",
        sourceType = "#p0.sourceType"
    )
    public RuntimeSubscriptionResponse create(CreateRuntimeSubscriptionRequest request) {
        PlanTemplate template = planTemplateService.findEntity(request.getPlanTemplateId());
        PlanPricingRule pricingRule = request.getPricingRuleId() == null ? null
            : planPricingRuleRepository.findByPlanPricingRuleIdAndPlanTemplatePlanTemplateId(request.getPricingRuleId(), template.getPlanTemplateId())
                .orElseThrow(() -> new SmsException(
                    ErrorCodes.PRICING_RULE_NOT_FOUND,
                    "Pricing rule not found for plan template: " + request.getPricingRuleId(),
                    404
                ));
        Group group = request.getGroupId() != null
            ? groupRepository.findById(request.getGroupId()).orElseThrow(() -> new SmsException(ErrorCodes.GROUP_NOT_FOUND, "Group not found: " + request.getGroupId(), 404))
            : null;
        Subscription entity = Subscription.builder()
            .subscriberType(CommercialEnums.normalize(request.getSubscriberType(), CommercialEnums.SubscriberType.class, "subscriberType"))
            .userId(request.getUserId())
            .group(group)
            .planTemplate(template)
            .pricingRule(pricingRule)
            .groupPlanAssignment(request.getGroupPlanAssignmentId() != null ? groupPlanAssignmentService.findEntity(request.getGroupPlanAssignmentId()) : null)
            .retailPlanSchedule(request.getRetailPlanScheduleId() != null ? retailPlanScheduleService.findEntity(request.getRetailPlanScheduleId()) : null)
            .sourceType(CommercialEnums.normalize(request.getSourceType(), CommercialEnums.SubscriptionSourceType.class, "sourceType"))
            .sourceId(request.getSourceId())
            .status(CommercialEnums.normalize(request.getStatus(), CommercialEnums.SubscriptionStatus.class, "status"))
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .signingQuotaTotal(request.getSigningQuotaTotal() == null ? 0 : request.getSigningQuotaTotal())
            .signingQuotaUsed(request.getSigningQuotaUsed())
            .activatedBy(request.getActivatedBy())
            .paymentReference(request.getPaymentReference())
            .build();
        return toResponse(subscriptionRepository.save(entity));
    }

    @Transactional
    @TrackSubscriptionAudit(
        subscriptionId = "#p0",
        actor = "'SYSTEM'",
        reason = "'Status updated'"
    )
    public RuntimeSubscriptionResponse updateStatus(Long id, String status) {
        Subscription entity = findEntity(id);
        entity.setStatus(CommercialEnums.normalize(status, CommercialEnums.SubscriptionStatus.class, "status"));
        return toResponse(subscriptionRepository.save(entity));
    }

    public Subscription findEntity(Long id) {
        return subscriptionRepository.findById(id)
            .orElseThrow(() -> new SmsException(ErrorCodes.SUBSCRIPTION_NOT_FOUND, "Subscription not found: " + id, 404));
    }

    private RuntimeSubscriptionResponse toResponse(Subscription entity) {
        RuntimeSubscriptionResponse response = new RuntimeSubscriptionResponse();
        response.setSubscriptionId(entity.getSubscriptionId());
        response.setSubscriberType(entity.getSubscriberType());
        response.setUserId(entity.getUserId());
        response.setGroupId(entity.getGroup() != null ? entity.getGroup().getGroupId() : null);
        response.setPlanTemplateId(entity.getPlanTemplate().getPlanTemplateId());
        response.setPlanCode(entity.getPlanTemplate().getPlanCode());
        response.setPlanName(entity.getPlanTemplate().getPlanName());
        response.setPricingRuleId(entity.getPricingRule() != null ? entity.getPricingRule().getPlanPricingRuleId() : null);
        response.setGroupPlanAssignmentId(entity.getGroupPlanAssignment() != null ? entity.getGroupPlanAssignment().getGroupPlanAssignmentId() : null);
        response.setRetailPlanScheduleId(entity.getRetailPlanSchedule() != null ? entity.getRetailPlanSchedule().getRetailPlanScheduleId() : null);
        response.setSourceType(entity.getSourceType());
        response.setSourceId(entity.getSourceId());
        response.setStatus(entity.getStatus());
        response.setStartDate(entity.getStartDate());
        response.setEndDate(entity.getEndDate());
        response.setSigningQuotaTotal(entity.getSigningQuotaTotal());
        response.setSigningQuotaUsed(entity.getSigningQuotaUsed());
        response.setActivatedBy(entity.getActivatedBy());
        response.setPaymentReference(entity.getPaymentReference());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}


