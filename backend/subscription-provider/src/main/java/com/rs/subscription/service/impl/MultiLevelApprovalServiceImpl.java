package com.rs.subscription.service.impl;

import com.rs.subscription.service.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rs.subscription.dto.request.ApproveStepRequest;
import com.rs.subscription.dto.request.RejectApprovalRequest;
import com.rs.subscription.dto.request.RevisionApprovalRequest;
import com.rs.subscription.dto.request.SubmitApprovalRequest;
import com.rs.subscription.dto.response.ApprovalLevelConfigResponse;
import com.rs.subscription.dto.response.ApprovalStepResponse;
import com.rs.subscription.dto.response.MultiLevelApprovalResponse;
import com.rs.subscription.entity.ApprovalLevelConfig;
import com.rs.subscription.entity.ApprovalRequest;
import com.rs.subscription.entity.ApprovalRequestStep;
import com.rs.subscription.entity.GroupPlanAssignment;
import com.rs.subscription.entity.RetailPlanSchedule;
import com.rs.subscription.entity.UserAccount;
import com.rs.subscription.enums.CommercialEnums;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.repository.ApprovalLevelConfigRepository;
import com.rs.subscription.repository.ApprovalRequestRepository;
import com.rs.subscription.repository.ApprovalRequestStepRepository;
import com.rs.subscription.repository.GroupPlanAssignmentRepository;
import com.rs.subscription.repository.PlanTemplateRepository;
import com.rs.subscription.repository.RetailPlanScheduleRepository;
import com.rs.subscription.repository.UserAccountRepository;
import com.rs.subscription.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rs.subscription.dto.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Xử lý toàn bộ luồng phê duyệt nhiều cấp.
 * Propagation (cập nhật entity gốc khi approved/rejected) dùng repository trực tiếp
 * để tránh circular dependency với GroupPlanAssignmentService và RetailPlanScheduleService.
 */
@Service
@RequiredArgsConstructor
public class MultiLevelApprovalServiceImpl implements MultiLevelApprovalService {

    private static final String[] LEVEL_ROLES = {"LEVEL_1", "LEVEL_2", "LEVEL_3"};
    private static final String[] LEVEL_PERMISSION_KEYS = {"approval:level1", "approval:level2", "approval:level3"};

    private final ApprovalRequestRepository approvalRequestRepository;
    private final ApprovalRequestStepRepository stepRepository;
    private final ApprovalLevelConfigRepository levelConfigRepository;
    private final GroupPlanAssignmentRepository groupPlanAssignmentRepository;
    private final PlanTemplateRepository planTemplateRepository;
    private final RetailPlanScheduleRepository retailPlanScheduleRepository;
    private final UserAccountRepository userAccountRepository;
    private final ApprovalNotificationService notificationService;
    private final ObjectMapper objectMapper;

    // ─── Query ───────────────────────────────────────────────────────────────

    public List<MultiLevelApprovalResponse> listAll() {
        return approvalRequestRepository.findAllByOrderByCreatedAtDesc()
            .stream().map(this::toResponse).toList();
    }

    public PagedResponse<MultiLevelApprovalResponse> listPaged(String status, String customerSegment, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        boolean hasStatus = status != null && !status.isBlank();
        boolean hasSegment = customerSegment != null && !customerSegment.isBlank();

        Page<ApprovalRequest> result;
        if (hasStatus && hasSegment) {
            result = approvalRequestRepository.findByStatusAndCustomerSegmentOrderByCreatedAtDesc(status, customerSegment, pageable);
        } else if (hasStatus) {
            result = approvalRequestRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else if (hasSegment) {
            result = approvalRequestRepository.findByCustomerSegmentOrderByCreatedAtDesc(customerSegment, pageable);
        } else {
            result = approvalRequestRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        return PagedResponse.<MultiLevelApprovalResponse>builder()
            .content(result.getContent().stream().map(this::toResponse).toList())
            .totalElements(result.getTotalElements())
            .totalPages(result.getTotalPages())
            .page(page)
            .size(size)
            .build();
    }

    public MultiLevelApprovalResponse getById(Long id) {
        return toResponse(findRequest(id));
    }

    public List<ApprovalLevelConfigResponse> listLevelConfigs() {
        return levelConfigRepository.findAll().stream().map(this::toConfigResponse).toList();
    }

    // ─── Submit ──────────────────────────────────────────────────────────────

    /**
     * Chuyển ApprovalRequest từ DRAFT → IN_APPROVAL.
     * Tính số cấp dựa trên customerSegment + contractValue, tạo steps, gửi email Level 1.
     */
    @Transactional
    public MultiLevelApprovalResponse submit(Long id, SubmitApprovalRequest request) {
        ApprovalRequest approval = findRequest(id);

        if (!CommercialEnums.MultiApprovalRequestStatus.DRAFT.name().equals(approval.getStatus())) {
            throw new SmsException(ErrorCodes.APPROVAL_NOT_IN_PROGRESS,
                "Chỉ có thể submit khi request ở trạng thái DRAFT", 409);
        }

        BigDecimal contractValue = request.getContractValue() != null
            ? request.getContractValue()
            : (approval.getContractValue() != null ? approval.getContractValue() : BigDecimal.ZERO);

        approval.setContractValue(contractValue);
        int levels = resolveRequestedLevels(request.getApprovalLevel(), approval.getTotalLevels());
        approval.setTotalLevels(levels);
        approval.setCurrentLevel(1);
        approval.setStatus(CommercialEnums.MultiApprovalRequestStatus.IN_APPROVAL.name());

        stepRepository.deleteAll(stepRepository.findByApprovalRequestIdOrderByStepLevelAsc(id));
        createSteps(approval, levels);
        approvalRequestRepository.save(approval);

        notificationService.notifySubmitted(approval);
        return toResponse(approval);
    }

    /**
     * Tạo ApprovalRequest ở trạng thái DRAFT và tự động submit ngay (dùng nội bộ khi tạo entity).
     * Trả về approvalRequestId để caller có thể trả về cho frontend.
     */
    @Transactional
    public Long createAndSubmit(ApprovalRequest draft) {
        ApprovalRequest saved = approvalRequestRepository.save(draft);

        int levels = resolveRequestedLevels(saved.getTotalLevels(), 1);
        saved.setTotalLevels(levels);
        saved.setCurrentLevel(1);
        saved.setStatus(CommercialEnums.MultiApprovalRequestStatus.IN_APPROVAL.name());

        createSteps(saved, levels);
        approvalRequestRepository.save(saved);
        notificationService.notifySubmitted(saved);

        return saved.getId();
    }

    // ─── Approve step ────────────────────────────────────────────────────────

    @Transactional
    public MultiLevelApprovalResponse approveStep(Long id, ApproveStepRequest request) {
        ApprovalRequest approval = findRequest(id);
        validateInApproval(approval);

        String actor = SecurityUtil.getCurrentUsername().orElse(request.getApprovedBy());
        if (approval.getRequestedBy().equals(actor)) {
            throw new SmsException(ErrorCodes.APPROVAL_SELF_APPROVE,
                "Không được tự duyệt request của mình", 403);
        }

        ApprovalRequestStep currentStep = findCurrentStep(approval);

        boolean isAdmin = SecurityUtil.hasAnyAuthority("ROLE_ADMIN");
        if (!isAdmin) {
            validateCurrentApprover(approval, currentStep, actor);
        }

        currentStep.setStatus(CommercialEnums.ApprovalStepStatus.APPROVED.name());
        currentStep.setDecidedBy(actor);
        currentStep.setComment(request.getComment());
        currentStep.setDecidedAt(LocalDateTime.now());
        stepRepository.save(currentStep);

        if (isAdmin) {
            // Admin bypass: skip toàn bộ các level còn lại và approve ngay
            stepRepository.findByApprovalRequestIdOrderByStepLevelAsc(id).stream()
                .filter(s -> s.getStepLevel() > approval.getCurrentLevel()
                          && CommercialEnums.ApprovalStepStatus.PENDING.name().equals(s.getStatus()))
                .forEach(s -> {
                    s.setStatus(CommercialEnums.ApprovalStepStatus.SKIPPED.name());
                    stepRepository.save(s);
                });
            finalizeApproval(approval, actor, request.getComment());
            notificationService.notifyFullyApproved(approval);
        } else if (approval.getCurrentLevel() < approval.getTotalLevels()) {
            int nextLevel = approval.getCurrentLevel() + 1;
            approval.setCurrentLevel(nextLevel);
            approvalRequestRepository.save(approval);
            notificationService.notifyStepApproved(approval, nextLevel);
        } else {
            finalizeApproval(approval, actor, request.getComment());
            notificationService.notifyFullyApproved(approval);
        }

        return toResponse(approval);
    }

    // ─── Reject ──────────────────────────────────────────────────────────────

    @Transactional
    public MultiLevelApprovalResponse reject(Long id, RejectApprovalRequest request) {
        ApprovalRequest approval = findRequest(id);
        validateInApproval(approval);
        String actor = SecurityUtil.getCurrentUsername().orElse(request.getRejectedBy());

        ApprovalRequestStep currentStep = findCurrentStep(approval);
        validateCurrentApprover(approval, currentStep, actor);
        currentStep.setStatus(CommercialEnums.ApprovalStepStatus.REJECTED.name());
        currentStep.setDecidedBy(actor);
        currentStep.setComment(request.getReason());
        currentStep.setDecidedAt(LocalDateTime.now());
        stepRepository.save(currentStep);

        stepRepository.findByApprovalRequestIdAndStatus(id, CommercialEnums.ApprovalStepStatus.PENDING.name())
            .stream()
            .filter(s -> s.getStepLevel() > approval.getCurrentLevel())
            .forEach(s -> {
                s.setStatus(CommercialEnums.ApprovalStepStatus.SKIPPED.name());
                stepRepository.save(s);
            });

        approval.setStatus(CommercialEnums.MultiApprovalRequestStatus.REJECTED.name());
        approval.setReviewedBy(actor);
        approval.setReviewNote(request.getReason());
        approval.setReviewedAt(LocalDateTime.now());
        approvalRequestRepository.save(approval);

        notificationService.notifyRejected(approval, currentStep);
        propagateRejection(approval, actor, request.getReason());

        return toResponse(approval);
    }

    // ─── Request revision ────────────────────────────────────────────────────

    @Transactional
    public MultiLevelApprovalResponse requestRevision(Long id, RevisionApprovalRequest request) {
        ApprovalRequest approval = findRequest(id);
        validateInApproval(approval);
        String actor = SecurityUtil.getCurrentUsername().orElse(request.getRequestedBy());
        ApprovalRequestStep currentStep = findCurrentStep(approval);
        validateCurrentApprover(approval, currentStep, actor);

        stepRepository.findByApprovalRequestIdOrderByStepLevelAsc(id).forEach(s -> {
            s.setStatus(CommercialEnums.ApprovalStepStatus.PENDING.name());
            s.setDecidedBy(null);
            s.setComment(null);
            s.setDecidedAt(null);
            stepRepository.save(s);
        });

        approval.setStatus(CommercialEnums.MultiApprovalRequestStatus.NEED_REVISION.name());
        approval.setCurrentLevel(1);
        approval.setReviewNote(request.getReason());
        approvalRequestRepository.save(approval);

        notificationService.notifyRevisionRequested(approval, request.getReason());
        return toResponse(approval);
    }

    @Transactional
    public MultiLevelApprovalResponse resubmit(Long id, SubmitApprovalRequest request) {
        ApprovalRequest approval = findRequest(id);

        if (!CommercialEnums.MultiApprovalRequestStatus.NEED_REVISION.name().equals(approval.getStatus())) {
            throw new SmsException(ErrorCodes.APPROVAL_NOT_REVISABLE,
                "Chỉ có thể resubmit khi request ở trạng thái NEED_REVISION", 409);
        }

        if (request.getContractValue() != null) approval.setContractValue(request.getContractValue());

        int levels = resolveRequestedLevels(request.getApprovalLevel(), approval.getTotalLevels());
        approval.setTotalLevels(levels);
        approval.setCurrentLevel(1);
        approval.setStatus(CommercialEnums.MultiApprovalRequestStatus.IN_APPROVAL.name());
        approval.setReviewNote(null);

        stepRepository.deleteAll(stepRepository.findByApprovalRequestIdOrderByStepLevelAsc(id));
        createSteps(approval, levels);
        approvalRequestRepository.save(approval);

        notificationService.notifySubmitted(approval);
        return toResponse(approval);
    }

    // ─── Internal helpers ────────────────────────────────────────────────────

    private int resolveRequiredLevels(String customerSegment, BigDecimal contractValue) {
        return levelConfigRepository
            .findMatchingConfig(customerSegment, contractValue)
            .map(ApprovalLevelConfig::getRequiredLevels)
            .orElseGet(() -> fallbackLevels(customerSegment, contractValue));
    }

    private int resolveRequestedLevels(Integer requestedLevel, Integer fallbackLevel) {
        int levels = requestedLevel != null ? requestedLevel : (fallbackLevel != null ? fallbackLevel : 1);
        if (levels < 1 || levels > LEVEL_ROLES.length) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED, "approvalLevel must be between 1 and 3", 400);
        }
        return levels;
    }

    private int fallbackLevels(String customerSegment, BigDecimal value) {
        if (CommercialEnums.CustomerSegment.INDIVIDUAL.name().equals(customerSegment)) {
            if (value.compareTo(new BigDecimal("5000000")) < 0) return 1;
            if (value.compareTo(new BigDecimal("50000000")) < 0) return 2;
            return 3;
        } else {
            if (value.compareTo(new BigDecimal("50000000")) < 0) return 1;
            if (value.compareTo(new BigDecimal("500000000")) < 0) return 2;
            return 3;
        }
    }

    private void createSteps(ApprovalRequest approval, int levels) {
        List<ApprovalRequestStep> steps = new java.util.ArrayList<>(levels);
        for (int i = 1; i <= levels; i++) {
            steps.add(ApprovalRequestStep.builder()
                .approvalRequest(approval)
                .stepLevel(i)
                .requiredApprovalLevel(LEVEL_ROLES[i - 1])
                .status(CommercialEnums.ApprovalStepStatus.PENDING.name())
                .build());
        }
        stepRepository.saveAll(steps);
    }

    private ApprovalRequestStep findCurrentStep(ApprovalRequest approval) {
        return stepRepository
            .findByApprovalRequestIdAndStepLevel(approval.getId(), approval.getCurrentLevel())
            .orElseThrow(() -> new SmsException(ErrorCodes.APPROVAL_STEP_NOT_FOUND,
                "Không tìm thấy step cấp " + approval.getCurrentLevel(), 404));
    }

    private void validateInApproval(ApprovalRequest approval) {
        if (!CommercialEnums.MultiApprovalRequestStatus.IN_APPROVAL.name().equals(approval.getStatus())) {
            throw new SmsException(ErrorCodes.APPROVAL_NOT_IN_PROGRESS,
                "Request không ở trạng thái IN_APPROVAL", 409);
        }
    }

    private void validateCurrentApprover(ApprovalRequest approval, ApprovalRequestStep currentStep, String actor) {
        String permissionKey = permissionKeyForStep(currentStep);
        List<UserAccount> managerApprovers = userAccountRepository
            .findActiveManagersByUsernameAndPermissionKey(approval.getRequestedBy(), permissionKey);

        if (!managerApprovers.isEmpty()) {
            boolean inManagerApprovalChain = managerApprovers.stream()
                .anyMatch(user -> actor.equals(user.getUsername()));
            if (!inManagerApprovalChain) {
                throw new SmsException(ErrorCodes.APPROVAL_WRONG_LEVEL,
                    "Only an approver in the requester's management chain can approve this step", 403);
            }
            return;
        }

        boolean hasLevelPermission = userAccountRepository.findActiveUsersByPermissionKey(permissionKey).stream()
            .anyMatch(user -> actor.equals(user.getUsername()));
        if (!hasLevelPermission) {
            throw new SmsException(ErrorCodes.APPROVAL_WRONG_LEVEL,
                "User does not have required permission for approval step: " + permissionKey, 403);
        }
    }

    private String permissionKeyForStep(ApprovalRequestStep step) {
        int level = step.getStepLevel() != null ? step.getStepLevel() : 0;
        if (level < 1 || level > LEVEL_PERMISSION_KEYS.length) {
            throw new SmsException(ErrorCodes.APPROVAL_WRONG_LEVEL,
                "Unsupported approval step level: " + level, 400);
        }
        return LEVEL_PERMISSION_KEYS[level - 1];
    }

    private void finalizeApproval(ApprovalRequest approval, String approvedBy, String note) {
        approval.setStatus(CommercialEnums.MultiApprovalRequestStatus.APPROVED.name());
        approval.setReviewedBy(approvedBy);
        approval.setReviewNote(note);
        approval.setReviewedAt(LocalDateTime.now());
        approvalRequestRepository.save(approval);
        propagateApproval(approval, approvedBy);
    }

    /**
     * Cập nhật trạng thái entity gốc sau khi fully approved.
     * Dùng repository trực tiếp để tránh circular dependency.
     */
    private void propagateApproval(ApprovalRequest approval, String actor) {
        if ("GROUP_PLAN_ASSIGNMENT".equals(approval.getEntityType())) {
            groupPlanAssignmentRepository.findById(Long.valueOf(approval.getEntityId()))
                .ifPresent(gpa -> {
                    gpa.setAssignmentStatus(CommercialEnums.AssignmentStatus.APPROVED.name());
                    gpa.setApprovedBy(actor);
                    gpa.setApprovedAt(LocalDateTime.now());
                    groupPlanAssignmentRepository.save(gpa);
                });
        } else if ("RETAIL_PLAN_SCHEDULE".equals(approval.getEntityType())) {
            retailPlanScheduleRepository.findById(Long.valueOf(approval.getEntityId()))
                .ifPresent(rps -> {
                    rps.setScheduleStatus(CommercialEnums.ScheduleStatus.APPROVED.name());
                    rps.setApprovedBy(actor);
                    rps.setApprovedAt(LocalDateTime.now());
                    retailPlanScheduleRepository.save(rps);
                    touchPlanTemplate(rps.getPlanTemplate().getPlanTemplateId());
                });
        }
    }

    /**
     * Cập nhật trạng thái entity gốc sau khi rejected.
     */
    private void propagateRejection(ApprovalRequest approval, String actor, String reason) {
        if ("GROUP_PLAN_ASSIGNMENT".equals(approval.getEntityType())) {
            groupPlanAssignmentRepository.findById(Long.valueOf(approval.getEntityId()))
                .ifPresent(gpa -> {
                    gpa.setAssignmentStatus(CommercialEnums.AssignmentStatus.AVAILABLE.name());
                    gpa.setRejectedBy(actor);
                    gpa.setRejectedAt(LocalDateTime.now());
                    gpa.setStopReason(reason);
                    groupPlanAssignmentRepository.save(gpa);
                });
        } else if ("RETAIL_PLAN_SCHEDULE".equals(approval.getEntityType())) {
            retailPlanScheduleRepository.findById(Long.valueOf(approval.getEntityId()))
                .ifPresent(rps -> {
                    rps.setScheduleStatus(CommercialEnums.ScheduleStatus.INACTIVE.name());
                    retailPlanScheduleRepository.save(rps);
                    touchPlanTemplate(rps.getPlanTemplate().getPlanTemplateId());
                });
        }
    }

    private void touchPlanTemplate(Long planTemplateId) {
        planTemplateRepository.findById(planTemplateId).ifPresent(pt -> {
            pt.setUpdatedAt(LocalDateTime.now());
            planTemplateRepository.save(pt);
        });
    }

    private ApprovalRequest findRequest(Long id) {
        return approvalRequestRepository.findById(id)
            .orElseThrow(() -> new SmsException(ErrorCodes.APPROVAL_NOT_FOUND,
                "Approval request không tìm thấy: " + id, 404));
    }

    // ─── Mapping ─────────────────────────────────────────────────────────────

    private MultiLevelApprovalResponse toResponse(ApprovalRequest entity) {
        MultiLevelApprovalResponse resp = new MultiLevelApprovalResponse();
        resp.setId(entity.getId());
        resp.setRequestType(entity.getRequestType());
        resp.setCustomerSegment(entity.getCustomerSegment());
        resp.setStatus(entity.getStatus());
        resp.setRequestedBy(entity.getRequestedBy());
        resp.setEntityType(entity.getEntityType());
        resp.setEntityId(entity.getEntityId());
        resp.setDescription(entity.getDescription());
        resp.setContractValue(entity.getContractValue());
        resp.setTotalLevels(entity.getTotalLevels());
        resp.setCurrentLevel(entity.getCurrentLevel());
        resp.setCreatedAt(entity.getCreatedAt());
        resp.setUpdatedAt(entity.getUpdatedAt());
        resp.setPayload(parsePayload(entity.getRequestPayload()));
        resp.setSteps(stepRepository.findByApprovalRequestIdOrderByStepLevelAsc(entity.getId())
            .stream().map(this::toStepResponse).toList());
        return resp;
    }

    private ApprovalStepResponse toStepResponse(ApprovalRequestStep step) {
        ApprovalStepResponse r = new ApprovalStepResponse();
        r.setId(step.getId());
        r.setStepLevel(step.getStepLevel());
        r.setRequiredApprovalLevel(step.getRequiredApprovalLevel());
        r.setStatus(step.getStatus());
        r.setDecidedBy(step.getDecidedBy());
        r.setComment(step.getComment());
        r.setDecidedAt(step.getDecidedAt());
        r.setCreatedAt(step.getCreatedAt());
        return r;
    }

    private ApprovalLevelConfigResponse toConfigResponse(ApprovalLevelConfig config) {
        ApprovalLevelConfigResponse r = new ApprovalLevelConfigResponse();
        r.setId(config.getId());
        r.setCustomerSegment(config.getCustomerSegment());
        r.setMinValue(config.getMinValue());
        r.setMaxValue(config.getMaxValue());
        r.setRequiredLevels(config.getRequiredLevels());
        r.setDescription(config.getDescription());
        r.setIsActive(config.getIsActive());
        return r;
    }

    private Map<String, Object> parsePayload(String payload) {
        if (payload == null || payload.isBlank()) return null;
        try {
            return objectMapper.readValue(payload, new TypeReference<>() {});
        } catch (Exception ex) {
            return Map.of("raw", payload);
        }
    }
}
