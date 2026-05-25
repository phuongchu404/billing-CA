package com.rs.subscription.service.impl;

import com.rs.subscription.service.*;

import com.rs.subscription.aop.Auditable;
import com.rs.subscription.aop.TrackAssignmentAudit;
import com.rs.subscription.dto.request.CreateGroupPlanAssignmentRequest;
import com.rs.subscription.dto.request.ReviewCommercialRequest;
import com.rs.subscription.dto.response.AssignmentAuditResponse;
import com.rs.subscription.dto.response.GroupPlanAssignmentResponse;
import com.rs.subscription.enums.CommercialEnums;
import com.rs.subscription.entity.ApprovalRequest;
import com.rs.subscription.entity.AssignmentAudit;
import com.rs.subscription.entity.Group;
import com.rs.subscription.entity.GroupPlanAssignment;
import com.rs.subscription.entity.PlanPricingRule;
import com.rs.subscription.entity.PlanTemplate;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.repository.ApprovalRequestRepository;
import com.rs.subscription.repository.AssignmentAuditRepository;
import com.rs.subscription.repository.GroupPlanAssignmentRepository;
import com.rs.subscription.repository.GroupRepository;
import com.rs.subscription.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Auditable(entityType = "GROUP_PLAN_ASSIGNMENT")
@Service
@RequiredArgsConstructor
public class GroupPlanAssignmentServiceImpl implements GroupPlanAssignmentService {

    private final GroupPlanAssignmentRepository assignmentRepository;
    private final GroupRepository groupRepository;
    private final PlanTemplateService planTemplateService;
    private final AssignmentAuditRepository assignmentAuditRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final MultiLevelApprovalService multiLevelApprovalService;

    public List<GroupPlanAssignmentResponse> listAll() {
        return assignmentRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<GroupPlanAssignmentResponse> listByGroup(Long groupId) {
        return assignmentRepository.findByGroupGroupIdOrderByCreatedAtDesc(groupId).stream().map(this::toResponse).toList();
    }

    public GroupPlanAssignmentResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    @Transactional
    @TrackAssignmentAudit(
        assignmentType = CommercialEnums.ASSIGNMENT_TYPE_GROUP_PLAN,
        entityId = "#result.groupPlanAssignmentId",
        action = CommercialEnums.AUDIT_ACTION_REQUEST,
        actor = "#p0.requestedBy",
        note = "#p0.stopReason"
    )
    public GroupPlanAssignmentResponse create(CreateGroupPlanAssignmentRequest request) {
        if (request.getGroupId() == null) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED, "Group id is required", 400);
        }
        Group group = groupRepository.findById(request.getGroupId())
            .orElseThrow(() -> new SmsException(ErrorCodes.GROUP_NOT_FOUND, "Group not found: " + request.getGroupId(), 404));
        PlanTemplate template = planTemplateService.findEntity(request.getPlanTemplateId());
        String requestedStatus = CommercialEnums.normalize(request.getAssignmentStatus(), CommercialEnums.AssignmentStatus.class, "assignmentStatus");

        if (CommercialEnums.AssignmentStatus.REQUESTED.name().equals(requestedStatus)) {
            var availableAssignment = assignmentRepository
                .findFirstByGroupGroupIdAndPlanTemplatePlanTemplateIdAndAssignmentStatus(
                    request.getGroupId(),
                    request.getPlanTemplateId(),
                    CommercialEnums.AssignmentStatus.AVAILABLE.name());

            if (availableAssignment.isPresent()) {
                GroupPlanAssignment entity = availableAssignment.get();
                entity.setAssignmentStatus(CommercialEnums.AssignmentStatus.REQUESTED.name());
                entity.setRequestedBy(request.getRequestedBy());
                entity.setRequestedAt(LocalDateTime.now());
                entity.setApplyFrom(request.getApplyFrom());
                entity.setApplyTo(request.getApplyTo());
                entity.setStopReason(request.getStopReason());
                GroupPlanAssignment saved = assignmentRepository.save(entity);
                Long approvalId = createMultiLevelApproval(saved, request.getRequestedBy(), request.getApprovalLevel());
                GroupPlanAssignmentResponse response = toResponse(saved);
                response.setApprovalRequestId(approvalId);
                return response;
            }
        }

        GroupPlanAssignment entity = GroupPlanAssignment.builder()
            .group(group)
            .planTemplate(template)
            .assignmentStatus(requestedStatus)
            .requestedBy(request.getRequestedBy())
            .requestedAt(CommercialEnums.AssignmentStatus.REQUESTED.name().equals(requestedStatus) ? LocalDateTime.now() : null)
            .applyFrom(request.getApplyFrom())
            .applyTo(request.getApplyTo())
            .stopReason(request.getStopReason())
            .build();
        GroupPlanAssignment saved = assignmentRepository.save(entity);
        GroupPlanAssignmentResponse response = toResponse(saved);
        if (CommercialEnums.AssignmentStatus.REQUESTED.name().equals(requestedStatus)) {
            Long approvalId = createMultiLevelApproval(saved, request.getRequestedBy(), request.getApprovalLevel());
            response.setApprovalRequestId(approvalId);
        }
        return response;
    }

    @Transactional
    @TrackAssignmentAudit(
        assignmentType = CommercialEnums.ASSIGNMENT_TYPE_GROUP_PLAN,
        entityId = "#p0",
        action = "#p1.decision.toUpperCase()",
        actor = "#p1.actor",
        note = "#p1.note"
    )
    public GroupPlanAssignmentResponse review(Long id, ReviewCommercialRequest request) {
        GroupPlanAssignment entity = findEntity(id);
        String decision = CommercialEnums.normalize(request.getDecision(), CommercialEnums.ReviewDecision.class, "decision");
        validateReviewPermission(decision);
        if (CommercialEnums.ReviewDecision.APPROVE.name().equals(decision)) {
            if (request.getApplyFrom() != null) entity.setApplyFrom(request.getApplyFrom());
            if (request.getApplyTo() != null) entity.setApplyTo(request.getApplyTo());
            entity.setAssignmentStatus(CommercialEnums.AssignmentStatus.APPROVED.name());
            entity.setApprovedBy(request.getActor());
            entity.setApprovedAt(LocalDateTime.now());
        } else if (CommercialEnums.ReviewDecision.REJECT.name().equals(decision)) {
            entity.setAssignmentStatus(CommercialEnums.AssignmentStatus.AVAILABLE.name());
            entity.setRejectedBy(request.getActor());
            entity.setRejectedAt(LocalDateTime.now());
        } else if (CommercialEnums.ReviewDecision.ACTIVATE.name().equals(decision)) {
            entity.setAssignmentStatus(CommercialEnums.AssignmentStatus.ACTIVE.name());
            entity.setActivatedAt(LocalDateTime.now());
        } else if (CommercialEnums.ReviewDecision.STOP.name().equals(decision)) {
            String current = entity.getAssignmentStatus();
            if (CommercialEnums.AssignmentStatus.STOPPED.name().equals(current)
                || CommercialEnums.AssignmentStatus.REJECTED.name().equals(current)
                || CommercialEnums.AssignmentStatus.EXPIRED.name().equals(current)) {
                throw new SmsException(ErrorCodes.VALIDATION_FAILED,
                    "Cannot stop assignment in status: " + current, 400);
            }
            entity.setAssignmentStatus(CommercialEnums.AssignmentStatus.STOPPED.name());
            entity.setStoppedAt(LocalDateTime.now());
            entity.setStopReason(request.getNote());
        } else {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED, "Unsupported decision: " + request.getDecision(), 400);
        }
        updateApproval(entity, decision, request.getActor(), request.getNote());
        return toResponse(assignmentRepository.save(entity));
    }

    private void validateReviewPermission(String decision) {
        boolean allowed;
        if (CommercialEnums.ReviewDecision.APPROVE.name().equals(decision)
            || CommercialEnums.ReviewDecision.REJECT.name().equals(decision)) {
            allowed = SecurityUtil.hasAnyAuthority(
                "*",
                "approval:level1",
                "approval:level2",
                "approval:level3");
        } else if (CommercialEnums.ReviewDecision.ACTIVATE.name().equals(decision)
            || CommercialEnums.ReviewDecision.STOP.name().equals(decision)) {
            allowed = SecurityUtil.hasAnyAuthority("*", "group:update");
        } else {
            allowed = true;
        }
        if (!allowed) {
            throw new SmsException(ErrorCodes.FORBIDDEN, "You do not have permission to perform decision: " + decision, 403);
        }
    }

    public GroupPlanAssignment findEntity(Long id) {
        return assignmentRepository.findById(id)
            .orElseThrow(() -> new SmsException(ErrorCodes.ASSIGNMENT_NOT_FOUND, "Group plan assignment not found: " + id, 404));
    }

    /**
     * Tạo ApprovalRequest multi-level và tự động submit → gửi email cho Level 1 approver.
     * Giá trị hợp đồng = totalPrice tối đa trong các pricing rules của plan template.
     */
    private Long createMultiLevelApproval(GroupPlanAssignment entity, String actor, Integer approvalLevel) {
        BigDecimal contractValue = entity.getPlanTemplate().getPricingRules().stream()
            .filter(r -> Boolean.TRUE.equals(r.getIsActive()) && r.getTotalPrice() != null)
            .map(PlanPricingRule::getTotalPrice)
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);

        ApprovalRequest draft = ApprovalRequest.builder()
            .requestType(CommercialEnums.RequestType.REQUEST_GROUP_PLAN_ASSIGNMENT.name())
            .status(CommercialEnums.MultiApprovalRequestStatus.DRAFT.name())
            .customerSegment(CommercialEnums.CustomerSegment.GROUP.name())
            .requestedBy(actor)
            .entityType("GROUP_PLAN_ASSIGNMENT")
            .entityId(String.valueOf(entity.getGroupPlanAssignmentId()))
            .requestPayload("{\"groupId\":" + entity.getGroup().getGroupId()
                + ",\"planTemplateId\":" + entity.getPlanTemplate().getPlanTemplateId() + "}")
            .description("Yêu cầu áp dụng gói cước đại lý: " + entity.getPlanTemplate().getPlanName()
                + " cho " + entity.getGroup().getGroupName())
            .contractValue(contractValue)
            .totalLevels(normalizeApprovalLevel(approvalLevel))
            .currentLevel(0)
            .build();

        return multiLevelApprovalService.createAndSubmit(draft);
    }

    private int normalizeApprovalLevel(Integer approvalLevel) {
        if (approvalLevel == null) return 1;
        if (approvalLevel < 1 || approvalLevel > 3) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED, "approvalLevel must be between 1 and 3", 400);
        }
        return approvalLevel;
    }

    private void updateApproval(GroupPlanAssignment entity, String decision, String actor, String note) {
        approvalRequestRepository.findAllByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                "GROUP_PLAN_ASSIGNMENT",
                String.valueOf(entity.getGroupPlanAssignmentId()))
            .stream()
            .findFirst()
            .ifPresent(approval -> {
                approval.setStatus(CommercialEnums.ReviewDecision.APPROVE.name().equals(decision)
                    ? CommercialEnums.MultiApprovalRequestStatus.APPROVED.name()
                    : CommercialEnums.ReviewDecision.REJECT.name().equals(decision)
                        ? CommercialEnums.MultiApprovalRequestStatus.REJECTED.name()
                        : approval.getStatus());
                approval.setReviewedBy(actor);
                approval.setReviewNote(note);
                approval.setReviewedAt(LocalDateTime.now());
                approvalRequestRepository.save(approval);
            });
    }

    private GroupPlanAssignmentResponse toResponse(GroupPlanAssignment entity) {
        GroupPlanAssignmentResponse response = new GroupPlanAssignmentResponse();
        response.setGroupPlanAssignmentId(entity.getGroupPlanAssignmentId());
        response.setGroupId(entity.getGroup().getGroupId());
        response.setGroupCode(entity.getGroup().getGroupCode());
        response.setGroupName(entity.getGroup().getGroupName());
        response.setPlanTemplateId(entity.getPlanTemplate().getPlanTemplateId());
        response.setPlanCode(entity.getPlanTemplate().getPlanCode());
        response.setPlanName(entity.getPlanTemplate().getPlanName());
        response.setAssignmentStatus(entity.getAssignmentStatus());
        response.setRequestedBy(entity.getRequestedBy());
        response.setRequestedAt(entity.getRequestedAt());
        response.setApprovedBy(entity.getApprovedBy());
        response.setApprovedAt(entity.getApprovedAt());
        response.setRejectedBy(entity.getRejectedBy());
        response.setRejectedAt(entity.getRejectedAt());
        response.setApplyFrom(entity.getApplyFrom());
        response.setApplyTo(entity.getApplyTo());
        response.setActivatedAt(entity.getActivatedAt());
        response.setStoppedAt(entity.getStoppedAt());
        response.setStopReason(entity.getStopReason());
        approvalRequestRepository
            .findAllByEntityTypeAndEntityIdOrderByCreatedAtDesc("GROUP_PLAN_ASSIGNMENT",
                String.valueOf(entity.getGroupPlanAssignmentId()))
            .stream()
            .findFirst()
            .ifPresent(approval -> response.setApprovalRequestId(approval.getId()));
        response.setAudits(assignmentAuditRepository.findByGroupPlanAssignmentGroupPlanAssignmentIdOrderByCreatedAtDesc(entity.getGroupPlanAssignmentId())
            .stream().map(this::toAuditResponse).toList());
        return response;
    }

    private AssignmentAuditResponse toAuditResponse(AssignmentAudit audit) {
        AssignmentAuditResponse response = new AssignmentAuditResponse();
        response.setAssignmentAuditId(audit.getAssignmentAuditId());
        response.setAssignmentType(audit.getAssignmentType());
        response.setAction(audit.getAction());
        response.setOldStatus(audit.getOldStatus());
        response.setNewStatus(audit.getNewStatus());
        response.setActor(audit.getActor());
        response.setNote(audit.getNote());
        response.setCreatedAt(audit.getCreatedAt());
        return response;
    }
}
