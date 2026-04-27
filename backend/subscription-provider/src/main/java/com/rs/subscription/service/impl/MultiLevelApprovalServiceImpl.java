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
import com.rs.subscription.enums.CommercialEnums;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.repository.ApprovalLevelConfigRepository;
import com.rs.subscription.repository.ApprovalRequestRepository;
import com.rs.subscription.repository.ApprovalRequestStepRepository;
import com.rs.subscription.repository.GroupPlanAssignmentRepository;
import com.rs.subscription.repository.RetailPlanScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final ApprovalRequestRepository approvalRequestRepository;
    private final ApprovalRequestStepRepository stepRepository;
    private final ApprovalLevelConfigRepository levelConfigRepository;
    private final GroupPlanAssignmentRepository groupPlanAssignmentRepository;
    private final RetailPlanScheduleRepository retailPlanScheduleRepository;
    private final ApprovalNotificationService notificationService;
    private final ObjectMapper objectMapper;

    // ─── Query ───────────────────────────────────────────────────────────────

    public List<MultiLevelApprovalResponse> listAll() {
        return approvalRequestRepository.findAllByOrderByCreatedAtDesc()
            .stream().map(this::toResponse).toList();
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
        int levels = resolveRequiredLevels(approval.getCustomerSegment(), contractValue);
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

        BigDecimal contractValue = saved.getContractValue() != null ? saved.getContractValue() : BigDecimal.ZERO;
        int levels = resolveRequiredLevels(saved.getCustomerSegment(), contractValue);
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

        if (approval.getRequestedBy().equals(request.getApprovedBy())) {
            throw new SmsException(ErrorCodes.APPROVAL_SELF_APPROVE,
                "Không được tự duyệt request của mình", 403);
        }

        ApprovalRequestStep currentStep = findCurrentStep(approval);
        currentStep.setStatus(CommercialEnums.ApprovalStepStatus.APPROVED.name());
        currentStep.setDecidedBy(request.getApprovedBy());
        currentStep.setComment(request.getComment());
        currentStep.setDecidedAt(LocalDateTime.now());
        stepRepository.save(currentStep);

        if (approval.getCurrentLevel() < approval.getTotalLevels()) {
            int nextLevel = approval.getCurrentLevel() + 1;
            approval.setCurrentLevel(nextLevel);
            approvalRequestRepository.save(approval);
            notificationService.notifyStepApproved(approval, nextLevel);
        } else {
            finalizeApproval(approval, request.getApprovedBy(), request.getComment());
            notificationService.notifyFullyApproved(approval);
        }

        return toResponse(approval);
    }

    // ─── Reject ──────────────────────────────────────────────────────────────

    @Transactional
    public MultiLevelApprovalResponse reject(Long id, RejectApprovalRequest request) {
        ApprovalRequest approval = findRequest(id);
        validateInApproval(approval);

        ApprovalRequestStep currentStep = findCurrentStep(approval);
        currentStep.setStatus(CommercialEnums.ApprovalStepStatus.REJECTED.name());
        currentStep.setDecidedBy(request.getRejectedBy());
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
        approval.setReviewedBy(request.getRejectedBy());
        approval.setReviewNote(request.getReason());
        approval.setReviewedAt(LocalDateTime.now());
        approvalRequestRepository.save(approval);

        notificationService.notifyRejected(approval, currentStep);
        propagateRejection(approval, request.getRejectedBy(), request.getReason());

        return toResponse(approval);
    }

    // ─── Request revision ────────────────────────────────────────────────────

    @Transactional
    public MultiLevelApprovalResponse requestRevision(Long id, RevisionApprovalRequest request) {
        ApprovalRequest approval = findRequest(id);
        validateInApproval(approval);

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

        int levels = resolveRequiredLevels(
            approval.getCustomerSegment(),
            approval.getContractValue() != null ? approval.getContractValue() : BigDecimal.ZERO
        );
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
        for (int i = 1; i <= levels; i++) {
            stepRepository.save(ApprovalRequestStep.builder()
                .approvalRequest(approval)
                .stepLevel(i)
                .requiredApprovalLevel(LEVEL_ROLES[i - 1])
                .status(CommercialEnums.ApprovalStepStatus.PENDING.name())
                .build());
        }
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
                    gpa.setAssignmentStatus(CommercialEnums.AssignmentStatus.REJECTED.name());
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
                });
        }
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
