package com.rs.subscription.service.impl;

import com.rs.subscription.service.*;

import com.rs.subscription.dto.request.CreatePlanTemplateRequest;
import com.rs.subscription.dto.request.PlanPricingRuleRequest;
import com.rs.subscription.dto.response.PlanPricingRuleResponse;
import com.rs.subscription.dto.response.PlanTemplateResponse;
import com.rs.subscription.enums.CommercialEnums;
import com.rs.subscription.entity.PlanPricingRule;
import com.rs.subscription.entity.PlanTemplate;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.repository.PlanTemplateRepository;
import com.rs.subscription.aop.Auditable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Auditable(entityType = "PLAN_TEMPLATE")
@Service
@RequiredArgsConstructor
public class PlanTemplateServiceImpl implements PlanTemplateService {

    private final PlanTemplateRepository planTemplateRepository;

    public List<PlanTemplateResponse> listAll() {
        return planTemplateRepository.findAll().stream().map(this::toResponse).toList();
    }

    public PlanTemplateResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    @Transactional
    public PlanTemplateResponse create(CreatePlanTemplateRequest request) {
        if (planTemplateRepository.existsByPlanCode(request.getPlanCode())) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED, "Plan code already exists", 400);
        }
        PlanTemplate template = PlanTemplate.builder()
            .planCode(request.getPlanCode())
            .planName(request.getPlanName())
            .description(request.getDescription())
            .customerSegment(parseCustomerSegment(request.getCustomerSegment()))
            .templateScope(parseTemplateScope(request.getTemplateScope()))
            .status(parseTemplateStatus(request.getStatus()))
            .effectiveFrom(request.getEffectiveFrom())
            .effectiveTo(request.getEffectiveTo())
            .isVisible(request.getIsVisible())
            .allowBulkSigning(request.getAllowBulkSigning())
            .allowApiAccess(request.getAllowApiAccess())
            .createdBy(request.getCreatedBy())
            .build();

        request.getPricingRules().stream()
            .map(this::toRuleEntity)
            .forEach(rule -> {
                rule.setPlanTemplate(template);
                template.getPricingRules().add(rule);
            });

        return toResponse(planTemplateRepository.save(template));
    }

    @Transactional
    public PlanTemplateResponse update(Long id, CreatePlanTemplateRequest request) {
        PlanTemplate template = findEntity(id);
        if (!template.getPlanCode().equals(request.getPlanCode()) && planTemplateRepository.existsByPlanCode(request.getPlanCode())) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED, "Plan code already exists", 400);
        }
        template.setPlanCode(request.getPlanCode());
        template.setPlanName(request.getPlanName());
        template.setDescription(request.getDescription());
        template.setCustomerSegment(parseCustomerSegment(request.getCustomerSegment()));
        template.setTemplateScope(parseTemplateScope(request.getTemplateScope()));
        template.setStatus(parseTemplateStatus(request.getStatus()));
        template.setEffectiveFrom(request.getEffectiveFrom());
        template.setEffectiveTo(request.getEffectiveTo());
        template.setIsVisible(request.getIsVisible());
        template.setAllowBulkSigning(request.getAllowBulkSigning());
        template.setAllowApiAccess(request.getAllowApiAccess());
        template.setCreatedBy(request.getCreatedBy());
        template.getPricingRules().clear();
        request.getPricingRules().stream()
            .map(this::toRuleEntity)
            .forEach(rule -> {
                rule.setPlanTemplate(template);
                template.getPricingRules().add(rule);
            });
        return toResponse(planTemplateRepository.save(template));
    }

    @Transactional
    public void delete(Long id) {
        planTemplateRepository.delete(findEntity(id));
    }

    public PlanTemplate findEntity(Long id) {
        return planTemplateRepository.findById(id)
            .orElseThrow(() -> new SmsException(ErrorCodes.PLAN_NOT_FOUND, "Plan template not found: " + id, 404));
    }

    private PlanPricingRule toRuleEntity(PlanPricingRuleRequest request) {
        return PlanPricingRule.builder()
            .subjectType(CommercialEnums.normalize(request.getSubjectType(), CommercialEnums.SubjectType.class, "subjectType"))
            .certificateValidityValue(request.getCertificateValidityValue())
            .certificateValidityUnit(CommercialEnums.normalize(request.getCertificateValidityUnit(), CommercialEnums.ValidityUnit.class, "certificateValidityUnit"))
            .pricingMetric(CommercialEnums.normalize(request.getPricingMetric(), CommercialEnums.PricingMetric.class, "pricingMetric"))
            .rangeMin(request.getRangeMin())
            .rangeMax(request.getRangeMax())
            .unitPrice(request.getUnitPrice())
            .currency(request.getCurrency())
            .quotaTotal(request.getQuotaTotal())
            .sortOrder(request.getSortOrder())
            .isActive(request.getIsActive())
            .build();
    }

    private String parseCustomerSegment(String value) {
        return CommercialEnums.normalize(value, CommercialEnums.CustomerSegment.class, "customerSegment");
    }

    private String parseTemplateScope(String value) {
        return CommercialEnums.normalize(value, CommercialEnums.TemplateScope.class, "templateScope");
    }

    private String parseTemplateStatus(String value) {
        return CommercialEnums.normalize(value, CommercialEnums.TemplateStatus.class, "status");
    }

    private PlanTemplateResponse toResponse(PlanTemplate entity) {
        PlanTemplateResponse response = new PlanTemplateResponse();
        response.setPlanTemplateId(entity.getPlanTemplateId());
        response.setPlanCode(entity.getPlanCode());
        response.setPlanName(entity.getPlanName());
        response.setDescription(entity.getDescription());
        response.setCustomerSegment(entity.getCustomerSegment());
        response.setTemplateScope(entity.getTemplateScope());
        response.setStatus(entity.getStatus());
        response.setEffectiveFrom(entity.getEffectiveFrom());
        response.setEffectiveTo(entity.getEffectiveTo());
        response.setIsVisible(entity.getIsVisible());
        response.setAllowBulkSigning(entity.getAllowBulkSigning());
        response.setAllowApiAccess(entity.getAllowApiAccess());
        response.setCreatedBy(entity.getCreatedBy());
        response.setClonedFromTemplateId(entity.getClonedFromTemplate() != null ? entity.getClonedFromTemplate().getPlanTemplateId() : null);
        response.setVersionNo(entity.getVersionNo());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        response.setPricingRules(entity.getPricingRules().stream().map(this::toRuleResponse).toList());
        return response;
    }

    private PlanPricingRuleResponse toRuleResponse(PlanPricingRule entity) {
        PlanPricingRuleResponse response = new PlanPricingRuleResponse();
        response.setPlanPricingRuleId(entity.getPlanPricingRuleId());
        response.setSubjectType(entity.getSubjectType());
        response.setCertificateValidityValue(entity.getCertificateValidityValue());
        response.setCertificateValidityUnit(entity.getCertificateValidityUnit());
        response.setPricingMetric(entity.getPricingMetric());
        response.setRangeMin(entity.getRangeMin());
        response.setRangeMax(entity.getRangeMax());
        response.setUnitPrice(entity.getUnitPrice());
        response.setCurrency(entity.getCurrency());
        response.setQuotaTotal(entity.getQuotaTotal());
        response.setSortOrder(entity.getSortOrder());
        response.setIsActive(entity.getIsActive());
        return response;
    }
}
