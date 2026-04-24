package com.rs.subscription.service;

import com.rs.subscription.dto.request.CreateRuntimeSubscriptionRequest;
import com.rs.subscription.dto.request.ExecuteGroupAssignmentFlowRequest;
import com.rs.subscription.dto.request.ExecuteRetailPlanFlowRequest;
import com.rs.subscription.dto.request.GenerateSettlementFlowRequest;
import com.rs.subscription.dto.request.ReviewCommercialRequest;
import com.rs.subscription.dto.response.CommercialFlowResponse;
import com.rs.subscription.dto.response.RuntimeSubscriptionResponse;
import com.rs.subscription.dto.response.SettlementStatementResponse;
import com.rs.subscription.entity.CertificateUsageRecord;
import com.rs.subscription.entity.Group;
import com.rs.subscription.entity.GroupPlanAssignment;
import com.rs.subscription.entity.PlanPricingRule;
import com.rs.subscription.entity.RetailPlanSchedule;
import com.rs.subscription.entity.SettlementStatement;
import com.rs.subscription.entity.Subscription;
import com.rs.subscription.entity.UsageAggregate;
import com.rs.subscription.enums.CommercialEnums;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.repository.CertificateUsageRecordRepository;
import com.rs.subscription.repository.GroupRepository;
import com.rs.subscription.repository.PlanPricingRuleRepository;
import com.rs.subscription.repository.SettlementStatementRepository;
import com.rs.subscription.repository.SubscriptionRepository;
import com.rs.subscription.repository.UsageAggregateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommercialOrchestrationService {

    private static final DateTimeFormatter RANGE_KEY_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    private final GroupPlanAssignmentService groupPlanAssignmentService;
    private final RetailPlanScheduleService retailPlanScheduleService;
    private final RuntimeSubscriptionService runtimeSubscriptionService;
    private final PlanPricingRuleRepository planPricingRuleRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final GroupRepository groupRepository;
    private final CertificateUsageRecordRepository certificateUsageRecordRepository;
    private final UsageAggregateRepository usageAggregateRepository;
    private final SettlementStatementRepository settlementStatementRepository;

    @Transactional
    public CommercialFlowResponse executeGroupAssignmentFlow(Long assignmentId, ExecuteGroupAssignmentFlowRequest request) {
        GroupPlanAssignment assignment = groupPlanAssignmentService.findEntity(assignmentId);

        if (Boolean.TRUE.equals(request.getApproveNow()) && "REQUESTED".equals(assignment.getAssignmentStatus())) {
            groupPlanAssignmentService.review(assignmentId, reviewRequest(request.getActor(), "APPROVE", request.getNote()));
            assignment = groupPlanAssignmentService.findEntity(assignmentId);
        }
        if (Boolean.TRUE.equals(request.getActivateNow()) && "APPROVED".equals(assignment.getAssignmentStatus())) {
            groupPlanAssignmentService.review(assignmentId, reviewRequest(request.getActor(), "ACTIVATE", request.getNote()));
            assignment = groupPlanAssignmentService.findEntity(assignmentId);
        }
        if (!"ACTIVE".equals(assignment.getAssignmentStatus())
            && Boolean.TRUE.equals(request.getIssueSubscription())) {
            throw new SmsException(
                ErrorCodes.INVALID_STATUS_TRANSITION,
                "Group plan assignment must be ACTIVE before issuing subscription",
                409
            );
        }

        RuntimeSubscriptionResponse subscription = Boolean.TRUE.equals(request.getIssueSubscription())
            ? issueGroupAssignmentSubscription(assignment, request)
            : null;

        CommercialFlowResponse response = new CommercialFlowResponse();
        response.setFlowType("GROUP_ASSIGNMENT");
        response.setEntityId(assignmentId);
        response.setFinalStatus(groupPlanAssignmentService.getById(assignmentId).getAssignmentStatus());
        response.setGroupPlanAssignment(groupPlanAssignmentService.getById(assignmentId));
        response.setSubscription(subscription);
        return response;
    }

    @Transactional
    public CommercialFlowResponse executeRetailPlanFlow(Long scheduleId, ExecuteRetailPlanFlowRequest request) {
        RetailPlanSchedule schedule = retailPlanScheduleService.findEntity(scheduleId);

        if (Boolean.TRUE.equals(request.getApproveNow())
            && !"APPROVED".equals(schedule.getScheduleStatus())
            && !"ACTIVE".equals(schedule.getScheduleStatus())) {
            retailPlanScheduleService.review(scheduleId, reviewRequest(request.getActor(), "APPROVE", request.getNote()));
            schedule = retailPlanScheduleService.findEntity(scheduleId);
        }
        if (Boolean.TRUE.equals(request.getActivateNow()) && "APPROVED".equals(schedule.getScheduleStatus())) {
            retailPlanScheduleService.review(scheduleId, reviewRequest(request.getActor(), "ACTIVATE", request.getNote()));
            schedule = retailPlanScheduleService.findEntity(scheduleId);
        }
        if (!"ACTIVE".equals(schedule.getScheduleStatus())
            && Boolean.TRUE.equals(request.getIssueSubscription())) {
            throw new SmsException(
                ErrorCodes.INVALID_STATUS_TRANSITION,
                "Retail plan schedule must be ACTIVE before issuing subscription",
                409
            );
        }

        RuntimeSubscriptionResponse subscription = Boolean.TRUE.equals(request.getIssueSubscription())
            ? issueRetailSubscription(schedule, request)
            : null;

        CommercialFlowResponse response = new CommercialFlowResponse();
        response.setFlowType("RETAIL_PLAN");
        response.setEntityId(scheduleId);
        response.setFinalStatus(retailPlanScheduleService.getById(scheduleId).getScheduleStatus());
        response.setRetailPlanSchedule(retailPlanScheduleService.getById(scheduleId));
        response.setSubscription(subscription);
        return response;
    }

    @Transactional
    public CommercialFlowResponse generateSettlement(Long groupId, GenerateSettlementFlowRequest request) {
        if (request.getFromDate().isAfter(request.getToDate())) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED, "fromDate must be before or equal to toDate", 400);
        }

        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new SmsException(ErrorCodes.GROUP_NOT_FOUND, "Group not found: " + groupId, 404));

        LocalDateTime from = request.getFromDate().atStartOfDay();
        LocalDateTime to = request.getToDate().plusDays(1).atStartOfDay();
        List<CertificateUsageRecord> usageRecords =
            certificateUsageRecordRepository.findBySubscriptionGroupGroupIdAndUsedAtGreaterThanEqualAndUsedAtLessThanOrderByUsedAtAsc(
                groupId,
                from,
                to
            );

        SettlementMetrics metrics = summarizeUsage(usageRecords, request.getCurrency());
        upsertGroupAggregate(groupId, request.getFromDate(), request.getToDate(), metrics);

        SettlementStatement statement = settlementStatementRepository.save(SettlementStatement.builder()
            .group(group)
            .fromDate(request.getFromDate())
            .toDate(request.getToDate())
            .status(Boolean.TRUE.equals(request.getFinalizeNow())
                ? CommercialEnums.StatementStatus.FINALIZED.name()
                : CommercialEnums.StatementStatus.DRAFT.name())
            .totalCertificates(metrics.totalCertificates())
            .totalSignings(metrics.totalSignings())
            .totalAmount(metrics.totalAmount())
            .currency(metrics.currency())
            .generatedBy(request.getActor())
            .generatedAt(LocalDateTime.now())
            .build());

        SettlementStatementResponse settlementResponse = new SettlementStatementResponse();
        settlementResponse.setSettlementStatementId(statement.getSettlementStatementId());
        settlementResponse.setGroupId(group.getGroupId());
        settlementResponse.setGroupCode(group.getGroupCode());
        settlementResponse.setGroupName(group.getGroupName());
        settlementResponse.setFromDate(statement.getFromDate());
        settlementResponse.setToDate(statement.getToDate());
        settlementResponse.setStatus(statement.getStatus());
        settlementResponse.setTotalCertificates(statement.getTotalCertificates());
        settlementResponse.setTotalSignings(statement.getTotalSignings());
        settlementResponse.setTotalAmount(statement.getTotalAmount());
        settlementResponse.setCurrency(statement.getCurrency());
        settlementResponse.setGeneratedAt(statement.getGeneratedAt());
        settlementResponse.setGeneratedBy(statement.getGeneratedBy());
        settlementResponse.setCreatedAt(statement.getCreatedAt());
        settlementResponse.setUpdatedAt(statement.getUpdatedAt());

        CommercialFlowResponse response = new CommercialFlowResponse();
        response.setFlowType("SETTLEMENT");
        response.setEntityId(groupId);
        response.setFinalStatus(statement.getStatus());
        response.setFromDate(request.getFromDate());
        response.setToDate(request.getToDate());
        response.setTotalCertificates(metrics.totalCertificates());
        response.setTotalSignings(metrics.totalSignings());
        response.setTotalAmount(metrics.totalAmount());
        response.setCurrency(metrics.currency());
        response.setSettlementStatement(settlementResponse);
        return response;
    }

    private RuntimeSubscriptionResponse issueGroupAssignmentSubscription(GroupPlanAssignment assignment, ExecuteGroupAssignmentFlowRequest request) {
        Subscription existing = subscriptionRepository.findFirstByGroupPlanAssignmentGroupPlanAssignmentIdOrderByCreatedAtDesc(
            assignment.getGroupPlanAssignmentId()
        );
        if (existing != null
            && !"CANCELLED".equals(existing.getStatus())
            && !"EXPIRED".equals(existing.getStatus())) {
            return runtimeSubscriptionService.getById(existing.getSubscriptionId());
        }

        PlanPricingRule pricingRule = resolvePricingRule(
            assignment.getPlanTemplate().getPlanTemplateId(),
            request.getPricingRuleId(),
            CommercialEnums.PricingMetric.SIGNING_COUNT.name()
        );
        CreateRuntimeSubscriptionRequest createRequest = new CreateRuntimeSubscriptionRequest();
        createRequest.setSubscriberType(CommercialEnums.SubscriberType.GROUP.name());
        createRequest.setGroupId(assignment.getGroup().getGroupId());
        createRequest.setPlanTemplateId(assignment.getPlanTemplate().getPlanTemplateId());
        createRequest.setPricingRuleId(pricingRule != null ? pricingRule.getPlanPricingRuleId() : null);
        createRequest.setGroupPlanAssignmentId(assignment.getGroupPlanAssignmentId());
        createRequest.setSourceType(CommercialEnums.SubscriptionSourceType.GROUP_ASSIGNMENT.name());
        createRequest.setSourceId(request.getSourceId() != null ? request.getSourceId() : assignment.getGroupPlanAssignmentId());
        createRequest.setStatus(request.getSubscriptionStatus());
        createRequest.setStartDate(request.getStartDate() != null ? request.getStartDate() : defaultStartDate(assignment.getApplyFrom()));
        createRequest.setEndDate(request.getEndDate() != null ? request.getEndDate() : assignment.getApplyTo());
        createRequest.setSigningQuotaTotal(resolveQuota(request.getSigningQuotaTotal(), pricingRule));
        createRequest.setSigningQuotaUsed(0);
        createRequest.setActivatedBy(request.getActor());
        createRequest.setPaymentReference(request.getPaymentReference());
        return runtimeSubscriptionService.create(createRequest);
    }

    private RuntimeSubscriptionResponse issueRetailSubscription(RetailPlanSchedule schedule, ExecuteRetailPlanFlowRequest request) {
        Subscription existing = subscriptionRepository.findFirstByRetailPlanScheduleRetailPlanScheduleIdAndUserIdOrderByCreatedAtDesc(
            schedule.getRetailPlanScheduleId(),
            request.getUserId()
        );
        if (existing != null
            && !"CANCELLED".equals(existing.getStatus())
            && !"EXPIRED".equals(existing.getStatus())) {
            return runtimeSubscriptionService.getById(existing.getSubscriptionId());
        }

        PlanPricingRule pricingRule = resolvePricingRule(
            schedule.getPlanTemplate().getPlanTemplateId(),
            request.getPricingRuleId(),
            CommercialEnums.PricingMetric.CERTIFICATE_COUNT.name()
        );
        CreateRuntimeSubscriptionRequest createRequest = new CreateRuntimeSubscriptionRequest();
        createRequest.setSubscriberType(CommercialEnums.SubscriberType.INDIVIDUAL.name());
        createRequest.setUserId(request.getUserId());
        createRequest.setPlanTemplateId(schedule.getPlanTemplate().getPlanTemplateId());
        createRequest.setPricingRuleId(pricingRule != null ? pricingRule.getPlanPricingRuleId() : null);
        createRequest.setRetailPlanScheduleId(schedule.getRetailPlanScheduleId());
        createRequest.setSourceType(CommercialEnums.SubscriptionSourceType.RETAIL_PURCHASE.name());
        createRequest.setSourceId(request.getSourceId() != null ? request.getSourceId() : schedule.getRetailPlanScheduleId());
        createRequest.setStatus(request.getSubscriptionStatus());
        createRequest.setStartDate(request.getStartDate() != null ? request.getStartDate() : defaultStartDate(schedule.getApplyFrom()));
        createRequest.setEndDate(request.getEndDate() != null ? request.getEndDate() : schedule.getApplyTo());
        createRequest.setSigningQuotaTotal(resolveQuota(request.getSigningQuotaTotal(), pricingRule));
        createRequest.setSigningQuotaUsed(0);
        createRequest.setActivatedBy(request.getActor());
        createRequest.setPaymentReference(request.getPaymentReference());
        return runtimeSubscriptionService.create(createRequest);
    }

    private ReviewCommercialRequest reviewRequest(String actor, String decision, String note) {
        ReviewCommercialRequest request = new ReviewCommercialRequest();
        request.setActor(actor);
        request.setDecision(decision);
        request.setNote(note);
        return request;
    }

    private PlanPricingRule resolvePricingRule(Long planTemplateId, Long pricingRuleId, String preferredMetric) {
        if (pricingRuleId != null) {
            return planPricingRuleRepository.findByPlanPricingRuleIdAndPlanTemplatePlanTemplateId(pricingRuleId, planTemplateId)
                .orElseThrow(() -> new SmsException(
                    ErrorCodes.PRICING_RULE_NOT_FOUND,
                    "Pricing rule not found for plan template: " + pricingRuleId,
                    404
                ));
        }

        List<PlanPricingRule> activeRules = planPricingRuleRepository.findByPlanTemplatePlanTemplateIdAndIsActiveTrueOrderBySortOrderAsc(planTemplateId);
        return activeRules.stream()
            .filter(rule -> preferredMetric.equals(rule.getPricingMetric()))
            .min(Comparator.comparing(PlanPricingRule::getSortOrder))
            .orElse(activeRules.isEmpty() ? null : activeRules.get(0));
    }

    private Integer resolveQuota(Integer requestedQuota, PlanPricingRule pricingRule) {
        if (requestedQuota != null) {
            return requestedQuota;
        }
        if (pricingRule != null && pricingRule.getQuotaTotal() != null) {
            return pricingRule.getQuotaTotal();
        }
        return 0;
    }

    private LocalDate defaultStartDate(LocalDate configuredStartDate) {
        return configuredStartDate != null ? configuredStartDate : LocalDate.now();
    }

    private SettlementMetrics summarizeUsage(List<CertificateUsageRecord> usageRecords, String defaultCurrency) {
        int totalCertificates = 0;
        int totalSignings = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        String currency = defaultCurrency == null || defaultCurrency.isBlank() ? "VND" : defaultCurrency;

        for (CertificateUsageRecord record : usageRecords) {
            int quantity = record.getQuantity() == null ? 0 : record.getQuantity();
            if ("SIGNING".equals(record.getUsageType())) {
                totalSignings += quantity;
            }
            if ("CERTIFICATE_CREATED".equals(record.getUsageType())
                || "CERTIFICATE_RENEWED".equals(record.getUsageType())) {
                totalCertificates += quantity;
            }

            PlanPricingRule pricingRule = record.getSubscription() != null ? record.getSubscription().getPricingRule() : null;
            if (pricingRule != null) {
                currency = pricingRule.getCurrency();
                if ("SIGNING".equals(record.getUsageType())
                    && CommercialEnums.PricingMetric.SIGNING_COUNT.name().equals(pricingRule.getPricingMetric())) {
                    totalAmount = totalAmount.add(pricingRule.getUnitPrice().multiply(BigDecimal.valueOf(quantity)));
                }
                if (("CERTIFICATE_CREATED".equals(record.getUsageType())
                    || "CERTIFICATE_RENEWED".equals(record.getUsageType()))
                    && CommercialEnums.PricingMetric.CERTIFICATE_COUNT.name().equals(pricingRule.getPricingMetric())) {
                    totalAmount = totalAmount.add(pricingRule.getUnitPrice().multiply(BigDecimal.valueOf(quantity)));
                }
            }
        }

        return new SettlementMetrics(totalCertificates, totalSignings, totalAmount, currency);
    }

    private void upsertGroupAggregate(Long groupId, LocalDate fromDate, LocalDate toDate, SettlementMetrics metrics) {
        String periodKey = buildRangeKey(fromDate, toDate);
        UsageAggregate aggregate = usageAggregateRepository.findByAggregateScopeAndScopeIdAndPeriodTypeAndPeriodKey(
                CommercialEnums.AggregateScope.GROUP.name(),
                groupId,
                CommercialEnums.PeriodType.MONTH.name(),
                periodKey
            )
            .orElseGet(UsageAggregate::new);

        aggregate.setAggregateScope(CommercialEnums.AggregateScope.GROUP.name());
        aggregate.setScopeId(groupId);
        aggregate.setPeriodType(CommercialEnums.PeriodType.MONTH.name());
        aggregate.setPeriodKey(periodKey);
        aggregate.setCertificatesCreated(metrics.totalCertificates());
        aggregate.setSigningUsed(metrics.totalSignings());
        aggregate.setActiveCertificates(metrics.totalCertificates());
        aggregate.setExpiredCertificates(0);
        aggregate.setRevokedCertificates(0);
        aggregate.setAmountDue(metrics.totalAmount());
        aggregate.setCurrency(metrics.currency());
        usageAggregateRepository.save(aggregate);
    }

    private String buildRangeKey(LocalDate fromDate, LocalDate toDate) {
        return fromDate.format(RANGE_KEY_FORMATTER) + "-" + toDate.format(RANGE_KEY_FORMATTER);
    }

    private record SettlementMetrics(int totalCertificates, int totalSignings, BigDecimal totalAmount, String currency) {
    }
}
