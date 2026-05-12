package com.rs.subscription.service.impl;

import com.rs.subscription.service.*;

import com.rs.subscription.dto.request.CreateIndividualPlanConfigRequest;
import com.rs.subscription.dto.request.IndividualApproveRequest;
import com.rs.subscription.dto.request.IndividualRequestApplyRequest;
import com.rs.subscription.dto.response.IndividualPlanConfigDetailResponse;
import com.rs.subscription.dto.response.IndividualPlanConfigListItemResponse;
import com.rs.subscription.dto.response.IndividualPlanConfigSummaryResponse;
import com.rs.subscription.entity.ApprovalRequest;
import com.rs.subscription.entity.AssignmentAudit;
import com.rs.subscription.entity.PlanPricingRule;
import com.rs.subscription.entity.PlanSubjectConfig;
import com.rs.subscription.entity.PlanTemplate;
import com.rs.subscription.entity.RetailPlanSchedule;
import com.rs.subscription.enums.CommercialEnums;
import com.rs.subscription.event.PlanUpdatedEvent;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.repository.AssignmentAuditRepository;
import com.rs.subscription.repository.PlanTemplateRepository;
import com.rs.subscription.repository.RetailPlanScheduleRepository;
import com.rs.subscription.aop.Auditable;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Auditable(entityType = "INDIVIDUAL_PLAN")
@Service
@RequiredArgsConstructor
public class IndividualPlanConfigServiceImpl implements IndividualPlanConfigService {

    private static final String CUSTOMER_SEGMENT = CommercialEnums.CustomerSegment.INDIVIDUAL.name();
    private static final List<String> ACTIVE_SCHEDULE_STATUSES = List.of(
        CommercialEnums.ScheduleStatus.REQUESTED.name(),
        CommercialEnums.ScheduleStatus.APPROVED.name(),
        CommercialEnums.ScheduleStatus.ACTIVE.name()
    );
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final PlanTemplateRepository planTemplateRepository;
    private final RetailPlanScheduleRepository scheduleRepository;
    private final AssignmentAuditRepository auditRepository;
    private final MultiLevelApprovalService multiLevelApprovalService;
    private final MinioStorageService minioStorageService;
    private final ApplicationEventPublisher eventPublisher;

    public IndividualPlanConfigSummaryResponse getSummary(String status, String applyFrom, String applyUntil, String updatedAt) {
        List<PlanTemplate> templates = planTemplateRepository.findByCustomerSegment(CUSTOMER_SEGMENT);

        List<IndividualPlanConfigListItemResponse> items = templates.stream()
                .sorted(Comparator.comparing(PlanTemplate::getUpdatedAt).reversed())
                .map(this::toListItem)
                .filter(item -> matchesFilter(item, status, applyFrom, applyUntil, updatedAt))
                .collect(Collectors.toList());

        LocalDateTime lastUpdated = templates.stream()
                .map(PlanTemplate::getUpdatedAt)
                .max(Comparator.naturalOrder())
                .orElse(null);

        IndividualPlanConfigSummaryResponse summary = new IndividualPlanConfigSummaryResponse();
        summary.setList(items);
        summary.setLastUpdated(lastUpdated != null ? lastUpdated.format(DATETIME_FMT) : null);

        // currentPlan = template whose schedule is ACTIVE
        scheduleRepository.findTopByScheduleStatus(CommercialEnums.ScheduleStatus.ACTIVE.name()).ifPresent(s -> {
            IndividualPlanConfigSummaryResponse.CurrentPlanInfo cp = new IndividualPlanConfigSummaryResponse.CurrentPlanInfo();
            cp.setName(s.getPlanTemplate().getPlanName());
            cp.setApplyUntil(s.getApplyTo() != null ? s.getApplyTo().format(DATE_FMT) : null);
            summary.setCurrentPlan(cp);
        });

        // nextPlan = template whose schedule is APPROVED (earliest future applyFrom)
        scheduleRepository.findTopByScheduleStatusOrderByApplyFromAsc(CommercialEnums.ScheduleStatus.APPROVED.name()).ifPresent(s -> {
            IndividualPlanConfigSummaryResponse.NextPlanInfo np = new IndividualPlanConfigSummaryResponse.NextPlanInfo();
            np.setName(s.getPlanTemplate().getPlanName());
            np.setApplyFrom(s.getApplyFrom() != null ? s.getApplyFrom().format(DATE_FMT) : null);
            summary.setNextPlan(np);
        });

        return summary;
    }

    public IndividualPlanConfigDetailResponse getDetail(Long id) {
        PlanTemplate template = findTemplate(id);
        Optional<RetailPlanSchedule> activeSchedule = findActiveSchedule(id);

        IndividualPlanConfigDetailResponse detail = new IndividualPlanConfigDetailResponse();
        detail.setId(template.getPlanTemplateId());
        detail.setName(template.getPlanName());
        detail.setStatus(deriveStatus(template, activeSchedule));
        detail.setCreatedBy(template.getCreatedBy());
        detail.setCreatedAt(template.getCreatedAt() != null ? template.getCreatedAt().format(DATETIME_FMT) : null);
        detail.setUpdatedAt(template.getUpdatedAt() != null ? template.getUpdatedAt().format(DATETIME_FMT) : null);

        activeSchedule.ifPresent(s -> {
            detail.setApplyFrom(s.getApplyFrom() != null ? s.getApplyFrom().format(DATE_FMT) : null);
            detail.setApplyUntil(s.getApplyTo() != null ? s.getApplyTo().format(DATE_FMT) : null);
        });

        // applyHistory: tổng hợp lịch sử từ các schedule đã INACTIVE
        List<RetailPlanSchedule> allSchedules = scheduleRepository.findByPlanTemplatePlanTemplateIdOrderByCreatedAtDesc(id);
        String applyHistory = allSchedules.stream()
                .filter(s -> CommercialEnums.ScheduleStatus.INACTIVE.name().equals(s.getScheduleStatus()) && s.getApplyFrom() != null && s.getApplyTo() != null)
                .sorted(Comparator.comparing(RetailPlanSchedule::getApplyFrom))
                .map(s -> s.getApplyFrom().format(DATE_FMT) + " - " + s.getApplyTo().format(DATE_FMT))
                .collect(Collectors.joining(", "));
        detail.setApplyHistory(applyHistory.isEmpty() ? null : applyHistory);

        // subjectConfigs
        List<IndividualPlanConfigDetailResponse.SubjectConfigRow> subjectConfigRows = template.getSubjectConfigs().stream()
                .map(this::toSubjectConfigRow)
                .collect(Collectors.toList());
        detail.setSubjectConfigs(subjectConfigRows);

        // pricingRules
        List<IndividualPlanConfigDetailResponse.PricingRuleRow> rules = template.getPricingRules().stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsActive()))
                .sorted(Comparator.comparingInt(PlanPricingRule::getSortOrder))
                .map(this::toPricingRuleRow)
                .collect(Collectors.toList());
        detail.setPricingRules(rules);

        // statusHistory: từ AssignmentAudit của tất cả schedules của template này
        List<IndividualPlanConfigDetailResponse.StatusHistoryRow> history = allSchedules.stream()
                .flatMap(s -> auditRepository.findByRetailPlanScheduleRetailPlanScheduleIdOrderByCreatedAtDesc(
                        s.getRetailPlanScheduleId()).stream())
                .sorted(Comparator.comparing(AssignmentAudit::getCreatedAt).reversed())
                .map(this::toStatusHistoryRow)
                .collect(Collectors.toList());
        detail.setStatusHistory(history);

        return detail;
    }

    @Transactional
    public IndividualPlanConfigDetailResponse create(CreateIndividualPlanConfigRequest req) {
        if (planTemplateRepository.findAll().stream()
                .filter(t -> CUSTOMER_SEGMENT.equals(t.getCustomerSegment()))
                .anyMatch(t -> t.getPlanName().equals(req.getName()))) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED, "Tên gói cước đã tồn tại: " + req.getName(), 400);
        }

        String planCode = generatePlanCode();
        PlanTemplate template = PlanTemplate.builder()
                .planCode(planCode)
                .planName(req.getName())
                .customerSegment(CUSTOMER_SEGMENT)
                .templateScope(CommercialEnums.TemplateScope.PUBLIC.name())
                .status(CommercialEnums.TemplateStatus.AVAILABLE.name())
                .isVisible(true)
                .allowBulkSigning(false)
                .allowApiAccess(false)
                .createdBy(req.getRequestedBy())
                .versionNo(1)
                .build();

        if (req.getPricingRules() != null) {
            int sortOrder = 0;
            for (CreateIndividualPlanConfigRequest.PricingRuleRequest rr : req.getPricingRules()) {
                PlanPricingRule rule = toPricingRuleEntity(rr, sortOrder++);
                rule.setPlanTemplate(template);
                template.getPricingRules().add(rule);
            }
        }

        if (req.getSubjectConfigs() != null) {
            for (CreateIndividualPlanConfigRequest.SubjectConfigRequest sc : req.getSubjectConfigs()) {
                PlanSubjectConfig config = PlanSubjectConfig.builder()
                        .planTemplate(template)
                        .subjectType(sc.getSubjectType())
                        .iconUrl(sc.getIconUrl())
                        .featuresText(sc.getFeaturesText())
                        .build();
                template.getSubjectConfigs().add(config);
            }
        }

        PlanTemplate saved = planTemplateRepository.save(template);

        // Confirm images so cleanup job won't delete them
        if (req.getSubjectConfigs() != null) {
            req.getSubjectConfigs().stream()
                    .filter(sc -> sc.getIconUrl() != null && !sc.getIconUrl().isBlank())
                    .forEach(sc -> minioStorageService.confirmByStoragePath(sc.getIconUrl()));
        }

        eventPublisher.publishEvent(new PlanUpdatedEvent(this, saved.getPlanTemplateId(), "CREATED"));
        return getDetail(saved.getPlanTemplateId());
    }

    @Transactional
    public IndividualPlanConfigDetailResponse requestApply(Long id, IndividualRequestApplyRequest req) {
        PlanTemplate template = findTemplate(id);
        ensureStatus(template, CommercialEnums.TemplateStatus.AVAILABLE.name(), "Chỉ có thể yêu cầu áp dụng khi gói cước đang ở trạng thái Khả dụng");

        Optional<RetailPlanSchedule> existing = findActiveSchedule(id);
        if (existing.isPresent()) {
            throw new SmsException(ErrorCodes.INVALID_STATUS_TRANSITION,
                    "Gói cước đã có yêu cầu áp dụng đang chờ xử lý", 400);
        }

        RetailPlanSchedule saved = createSchedule(template, req.getApplyFrom(), req.getApplyUntil(), req.getRequestedBy(), CommercialEnums.ScheduleStatus.REQUESTED.name());

        // Tính giá trị hợp đồng từ unitPrice tối đa của pricing rules
        BigDecimal contractValue = template.getPricingRules().stream()
            .filter(r -> Boolean.TRUE.equals(r.getIsActive()) && r.getUnitPrice() != null)
            .map(PlanPricingRule::getUnitPrice)
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);

        // Tạo multi-level ApprovalRequest và auto-submit → gửi email approver Level 1
        ApprovalRequest draft = ApprovalRequest.builder()
            .requestType(CommercialEnums.RequestType.REQUEST_RETAIL_PLAN_SCHEDULE.name())
            .status(CommercialEnums.MultiApprovalRequestStatus.DRAFT.name())
            .customerSegment(CommercialEnums.CustomerSegment.INDIVIDUAL.name())
            .requestedBy(req.getRequestedBy() != null ? req.getRequestedBy() : "system")
            .entityType("RETAIL_PLAN_SCHEDULE")
            .entityId(String.valueOf(saved.getRetailPlanScheduleId()))
            .requestPayload("{\"planTemplateId\":" + template.getPlanTemplateId() + "}")
            .description("Yêu cầu áp dụng gói cước phổ thông: " + template.getPlanName()
                + " từ " + req.getApplyFrom() + " đến " + req.getApplyUntil())
            .contractValue(contractValue)
            .totalLevels(normalizeApprovalLevel(req.getApprovalLevel()))
            .currentLevel(0)
            .build();

        Long approvalId = multiLevelApprovalService.createAndSubmit(draft);

        IndividualPlanConfigDetailResponse response = getDetail(id);
        response.setApprovalRequestId(approvalId);
        return response;
    }

    private int normalizeApprovalLevel(Integer approvalLevel) {
        if (approvalLevel == null) return 1;
        if (approvalLevel < 1 || approvalLevel > 3) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED, "approvalLevel must be between 1 and 3", 400);
        }
        return approvalLevel;
    }

    @Transactional
    public IndividualPlanConfigDetailResponse approve(Long id, IndividualApproveRequest req) {
        RetailPlanSchedule schedule = findActiveScheduleOrThrow(id, CommercialEnums.ScheduleStatus.REQUESTED.name(),
                "Chỉ có thể duyệt khi gói cước đang chờ duyệt");

        if (req.getApplyFrom() != null) schedule.setApplyFrom(LocalDate.parse(req.getApplyFrom()));
        if (req.getApplyUntil() != null) schedule.setApplyTo(LocalDate.parse(req.getApplyUntil()));
        schedule.setScheduleStatus(CommercialEnums.ScheduleStatus.APPROVED.name());
        schedule.setApprovedBy(req.getApprovedBy());
        schedule.setApprovedAt(LocalDateTime.now());
        scheduleRepository.save(schedule);

        recordAudit(schedule, CommercialEnums.AuditAction.APPROVE.name(),
            CommercialEnums.ScheduleStatus.REQUESTED.name(), CommercialEnums.ScheduleStatus.APPROVED.name(), req.getApprovedBy(), null);
        eventPublisher.publishEvent(new PlanUpdatedEvent(this, id, "APPROVED"));
        return getDetail(id);
    }

    @Transactional
    public IndividualPlanConfigDetailResponse reject(Long id, String actor) {
        RetailPlanSchedule schedule = findActiveScheduleOrThrow(id, CommercialEnums.ScheduleStatus.REQUESTED.name(),
                "Chỉ có thể từ chối khi gói cước đang chờ duyệt");
        schedule.setScheduleStatus(CommercialEnums.ScheduleStatus.INACTIVE.name());
        scheduleRepository.save(schedule);
        recordAudit(schedule, CommercialEnums.AuditAction.REJECT.name(),
            CommercialEnums.ScheduleStatus.REQUESTED.name(), CommercialEnums.ScheduleStatus.INACTIVE.name(), actor, null);
        return getDetail(id);
    }

    @Transactional
    public IndividualPlanConfigDetailResponse stop(Long id, String actor) {
        Optional<RetailPlanSchedule> scheduleOpt = findActiveSchedule(id);
        if (scheduleOpt.isEmpty() ||
                (!scheduleOpt.get().getScheduleStatus().equals(CommercialEnums.ScheduleStatus.APPROVED.name()) &&
                 !scheduleOpt.get().getScheduleStatus().equals(CommercialEnums.ScheduleStatus.ACTIVE.name()))) {
            throw new SmsException(ErrorCodes.INVALID_STATUS_TRANSITION,
                    "Chỉ có thể dừng khi gói cước đang ở trạng thái Đã duyệt hoặc Đang áp dụng", 400);
        }
        RetailPlanSchedule schedule = scheduleOpt.get();
        String oldStatus = schedule.getScheduleStatus();
        schedule.setScheduleStatus(CommercialEnums.ScheduleStatus.INACTIVE.name());
        scheduleRepository.save(schedule);
        recordAudit(schedule, CommercialEnums.AuditAction.STOP.name(), oldStatus, CommercialEnums.ScheduleStatus.INACTIVE.name(), actor, null);
        eventPublisher.publishEvent(new PlanUpdatedEvent(this, id, "STOPPED"));
        return getDetail(id);
    }

    @Transactional
    public IndividualPlanConfigDetailResponse deactivate(Long id, String actor) {
        PlanTemplate template = findTemplate(id);
        template.setStatus(CommercialEnums.TemplateStatus.INACTIVE.name());
        planTemplateRepository.save(template);

        findActiveSchedule(id).ifPresent(s -> {
            s.setScheduleStatus(CommercialEnums.ScheduleStatus.INACTIVE.name());
            scheduleRepository.save(s);
        });

        return getDetail(id);
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private PlanTemplate findTemplate(Long id) {
        PlanTemplate template = planTemplateRepository.findById(id)
                .orElseThrow(() -> new SmsException(ErrorCodes.PLAN_NOT_FOUND, "Không tìm thấy gói cước: " + id, 404));
        if (!CUSTOMER_SEGMENT.equals(template.getCustomerSegment())) {
            throw new SmsException(ErrorCodes.PLAN_NOT_FOUND, "Gói cước không thuộc phân khúc cá nhân", 404);
        }
        return template;
    }

    private String generatePlanCode() {
        String planCode = "PLN_" + CUSTOMER_SEGMENT + "_" + System.currentTimeMillis();
        if (planTemplateRepository.existsByPlanCode(planCode)) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED, "Plan code already exists, please retry", 400);
        }
        return planCode;
    }

    private Optional<RetailPlanSchedule> findActiveSchedule(Long templateId) {
        return scheduleRepository.findTopByPlanTemplatePlanTemplateIdAndScheduleStatusInOrderByCreatedAtDesc(
                templateId, ACTIVE_SCHEDULE_STATUSES);
    }

    private RetailPlanSchedule findActiveScheduleOrThrow(Long templateId, String expectedStatus, String message) {
        RetailPlanSchedule schedule = findActiveSchedule(templateId)
                .orElseThrow(() -> new SmsException(ErrorCodes.INVALID_STATUS_TRANSITION, message, 400));
        if (!expectedStatus.equals(schedule.getScheduleStatus())) {
            throw new SmsException(ErrorCodes.INVALID_STATUS_TRANSITION, message, 400);
        }
        return schedule;
    }

    private void ensureStatus(PlanTemplate template, String expectedStatus, String message) {
        if (!expectedStatus.equals(template.getStatus())) {
            throw new SmsException(ErrorCodes.INVALID_STATUS_TRANSITION, message, 400);
        }
    }

    private RetailPlanSchedule createSchedule(PlanTemplate template, String applyFrom, String applyUntil,
                                               String requestedBy, String status) {
        RetailPlanSchedule schedule = RetailPlanSchedule.builder()
                .planTemplate(template)
                .scheduleStatus(status)
                .applyFrom(LocalDate.parse(applyFrom))
                .applyTo(LocalDate.parse(applyUntil))
                .requestedBy(requestedBy)
                .requestedAt(LocalDateTime.now())
                .build();
        RetailPlanSchedule saved = scheduleRepository.save(schedule);
        recordAudit(saved, CommercialEnums.AuditAction.REQUEST.name(), null, status, requestedBy, null);
        return saved;
    }

    private void recordAudit(RetailPlanSchedule schedule, String action,
                              String oldStatus, String newStatus, String actor, String note) {
        AssignmentAudit audit = AssignmentAudit.builder()
                .retailPlanSchedule(schedule)
                .assignmentType(CommercialEnums.AssignmentType.RETAIL_PLAN.name())
                .action(action)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .actor(actor != null ? actor : "system")
                .note(note)
                .build();
        auditRepository.save(audit);
    }

    private String deriveStatus(PlanTemplate template, Optional<RetailPlanSchedule> activeSchedule) {
        if (CommercialEnums.TemplateStatus.INACTIVE.name().equals(template.getStatus())) {
            return CommercialEnums.IndividualPlanStatus.UNAVAILABLE.name();
        }
        if (activeSchedule.isPresent()) {
            String scheduleStatus = activeSchedule.get().getScheduleStatus();
            if (CommercialEnums.ScheduleStatus.REQUESTED.name().equals(scheduleStatus)) {
                return CommercialEnums.IndividualPlanStatus.PENDING.name();
            }
            if (CommercialEnums.ScheduleStatus.APPROVED.name().equals(scheduleStatus)) {
                return CommercialEnums.IndividualPlanStatus.APPROVED.name();
            }
            if (CommercialEnums.ScheduleStatus.ACTIVE.name().equals(scheduleStatus)) {
                return CommercialEnums.IndividualPlanStatus.APPLYING.name();
            }
            return CommercialEnums.IndividualPlanStatus.AVAILABLE.name();
        }
        return CommercialEnums.IndividualPlanStatus.AVAILABLE.name();
    }

    private IndividualPlanConfigListItemResponse toListItem(PlanTemplate template) {
        Optional<RetailPlanSchedule> activeSchedule = findActiveSchedule(template.getPlanTemplateId());

        IndividualPlanConfigListItemResponse item = new IndividualPlanConfigListItemResponse();
        item.setId(template.getPlanTemplateId());
        item.setName(template.getPlanName());
        item.setStatus(deriveStatus(template, activeSchedule));
        item.setUpdatedAt(template.getUpdatedAt() != null ? template.getUpdatedAt().format(DATETIME_FMT) : null);

        activeSchedule.ifPresent(s -> {
            item.setApplyFrom(s.getApplyFrom() != null ? s.getApplyFrom().format(DATE_FMT) : null);
            item.setApplyUntil(s.getApplyTo() != null ? s.getApplyTo().format(DATE_FMT) : null);
        });

        return item;
    }

    private IndividualPlanConfigDetailResponse.PricingRuleRow toPricingRuleRow(PlanPricingRule rule) {
        IndividualPlanConfigDetailResponse.PricingRuleRow row = new IndividualPlanConfigDetailResponse.PricingRuleRow();
        row.setId(rule.getPlanPricingRuleId());
        row.setSubject(rule.getSubjectType());
        row.setDurationMonths(rule.getCertificateValidityValue());
        row.setCondition(rule.getPricingMetric());
        row.setMinValue(rule.getRangeMin());
        row.setMaxValue(rule.getRangeMax());
        row.setFee(rule.getUnitPrice() != null ? rule.getUnitPrice().longValue() : 0L);
        row.setSortOrder(rule.getSortOrder());
        return row;
    }

    private IndividualPlanConfigDetailResponse.StatusHistoryRow toStatusHistoryRow(AssignmentAudit audit) {
        IndividualPlanConfigDetailResponse.StatusHistoryRow row = new IndividualPlanConfigDetailResponse.StatusHistoryRow();
        row.setStatus(mapScheduleStatusToUi(audit.getNewStatus()));
        row.setUpdatedAt(audit.getCreatedAt() != null ? audit.getCreatedAt().format(DATETIME_FMT) : null);
        row.setUpdatedBy(audit.getActor());
        return row;
    }

    private String mapScheduleStatusToUi(String scheduleStatus) {
        if (scheduleStatus == null) return CommercialEnums.IndividualPlanStatus.AVAILABLE.name();
        if (CommercialEnums.ScheduleStatus.REQUESTED.name().equals(scheduleStatus)) {
            return CommercialEnums.IndividualPlanStatus.PENDING.name();
        }
        if (CommercialEnums.ScheduleStatus.ACTIVE.name().equals(scheduleStatus)) {
            return CommercialEnums.IndividualPlanStatus.APPLYING.name();
        }
        if (CommercialEnums.ScheduleStatus.INACTIVE.name().equals(scheduleStatus)) {
            return CommercialEnums.IndividualPlanStatus.UNAVAILABLE.name();
        }
        return scheduleStatus;
    }

    private boolean matchesFilter(IndividualPlanConfigListItemResponse item,
                                   String status, String applyFrom, String applyUntil, String updatedAt) {
        if (status != null && !status.isBlank() && !status.equals(item.getStatus())) return false;
        if (applyFrom != null && !applyFrom.isBlank()) {
            if (item.getApplyFrom() == null) return false;
            LocalDate filterDate = LocalDate.parse(applyFrom);
            LocalDate itemDate = LocalDate.parse(item.getApplyFrom(), DATE_FMT);
            if (!itemDate.equals(filterDate)) return false;
        }
        if (applyUntil != null && !applyUntil.isBlank()) {
            if (item.getApplyUntil() == null) return false;
            LocalDate filterDate = LocalDate.parse(applyUntil);
            LocalDate itemDate = LocalDate.parse(item.getApplyUntil(), DATE_FMT);
            if (!itemDate.equals(filterDate)) return false;
        }
        if (updatedAt != null && !updatedAt.isBlank()) {
            if (item.getUpdatedAt() == null) return false;
            LocalDate filterDate = LocalDate.parse(updatedAt);
            LocalDate itemDate = LocalDateTime.parse(item.getUpdatedAt(), DATETIME_FMT).toLocalDate();
            if (!itemDate.equals(filterDate)) return false;
        }
        return true;
    }

    private IndividualPlanConfigDetailResponse.SubjectConfigRow toSubjectConfigRow(PlanSubjectConfig config) {
        IndividualPlanConfigDetailResponse.SubjectConfigRow row = new IndividualPlanConfigDetailResponse.SubjectConfigRow();
        row.setSubjectType(config.getSubjectType());
        row.setIconUrl(minioStorageService.toPublicUrl(config.getIconUrl()));
        row.setFeaturesText(config.getFeaturesText());
        return row;
    }

    private PlanPricingRule toPricingRuleEntity(CreateIndividualPlanConfigRequest.PricingRuleRequest rr, int sortOrder) {
        return PlanPricingRule.builder()
                .subjectType(rr.getSubject() != null ? rr.getSubject() : CommercialEnums.SubjectType.INDIVIDUAL.name())
                .certificateValidityValue(rr.getDurationMonths() != null ? rr.getDurationMonths() : 12)
                .certificateValidityUnit(CommercialEnums.ValidityUnit.MONTH.name())
                .pricingMetric(rr.getCondition() != null ? rr.getCondition() : CommercialEnums.PricingMetric.CERTIFICATE_COUNT.name())
                .rangeMin(rr.getMinValue() != null ? rr.getMinValue() : 1)
                .rangeMax(rr.getMaxValue())
                .unitPrice(rr.getFee() != null ? BigDecimal.valueOf(rr.getFee()) : BigDecimal.ZERO)
                .currency("VND")
                .sortOrder(rr.getSortOrder() != null ? rr.getSortOrder() : sortOrder)
                .isActive(true)
                .build();
    }
}
