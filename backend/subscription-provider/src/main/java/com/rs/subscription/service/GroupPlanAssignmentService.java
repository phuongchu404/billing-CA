package com.rs.subscription.service;

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
import com.rs.subscription.entity.PlanTemplate;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.repository.ApprovalRequestRepository;
import com.rs.subscription.repository.AssignmentAuditRepository;
import com.rs.subscription.repository.GroupPlanAssignmentRepository;
import com.rs.subscription.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupPlanAssignmentService {

    private final GroupPlanAssignmentRepository assignmentRepository;
    private final GroupRepository groupRepository;
    private final PlanTemplateService planTemplateService;
    private final AssignmentAuditRepository assignmentAuditRepository;
    private final ApprovalRequestRepository approvalRequestRepository;

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
        assignmentType = "GROUP_PLAN",
        entityId = "#result.groupPlanAssignmentId",
        action = "REQUEST",
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
        GroupPlanAssignment entity = GroupPlanAssignment.builder()
            .group(group)
            .planTemplate(template)
            .assignmentStatus(CommercialEnums.normalize(request.getAssignmentStatus(), CommercialEnums.AssignmentStatus.class, "assignmentStatus"))
            .requestedBy(request.getRequestedBy())
            .requestedAt(LocalDateTime.now())
            .applyFrom(request.getApplyFrom())
            .applyTo(request.getApplyTo())
            .stopReason(request.getStopReason())
            .build();
        GroupPlanAssignment saved = assignmentRepository.save(entity);
        createApproval(saved, request.getRequestedBy());
        return toResponse(saved);
    }

    @Transactional
    @TrackAssignmentAudit(
        assignmentType = "GROUP_PLAN",
        entityId = "#p0",
        action = "#p1.decision.toUpperCase()",
        actor = "#p1.actor",
        note = "#p1.note"
    )
    public GroupPlanAssignmentResponse review(Long id, ReviewCommercialRequest request) {
        GroupPlanAssignment entity = findEntity(id);
        String decision = request.getDecision().toUpperCase();
        if ("APPROVE".equals(decision)) {
            entity.setAssignmentStatus("APPROVED");
            entity.setApprovedBy(request.getActor());
            entity.setApprovedAt(LocalDateTime.now());
        } else if ("REJECT".equals(decision)) {
            entity.setAssignmentStatus("REJECTED");
            entity.setRejectedBy(request.getActor());
            entity.setRejectedAt(LocalDateTime.now());
        } else if ("ACTIVATE".equals(decision)) {
            entity.setAssignmentStatus("ACTIVE");
            entity.setActivatedAt(LocalDateTime.now());
        } else if ("STOP".equals(decision)) {
            entity.setAssignmentStatus("STOPPED");
            entity.setStoppedAt(LocalDateTime.now());
            entity.setStopReason(request.getNote());
        } else {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED, "Unsupported decision: " + request.getDecision(), 400);
        }
        updateApproval(entity, decision, request.getActor(), request.getNote());
        return toResponse(assignmentRepository.save(entity));
    }

    public GroupPlanAssignment findEntity(Long id) {
        return assignmentRepository.findById(id)
            .orElseThrow(() -> new SmsException(ErrorCodes.ASSIGNMENT_NOT_FOUND, "Group plan assignment not found: " + id, 404));
    }

    private void createApproval(GroupPlanAssignment entity, String actor) {
        ApprovalRequest approval = ApprovalRequest.builder()
            .requestType(CommercialEnums.RequestType.REQUEST_GROUP_PLAN_ASSIGNMENT.name())
            .status(CommercialEnums.ApprovalStatus.PENDING.name())
            .requestedBy(actor)
            .entityType("GROUP_PLAN_ASSIGNMENT")
            .entityId(String.valueOf(entity.getGroupPlanAssignmentId()))
            .requestPayload("{\"groupId\":" + entity.getGroup().getGroupId() + ",\"planTemplateId\":" + entity.getPlanTemplate().getPlanTemplateId() + "}")
            .description("Request group plan assignment")
            .build();
        approvalRequestRepository.save(approval);
    }

    private void updateApproval(GroupPlanAssignment entity, String decision, String actor, String note) {
        approvalRequestRepository.findByEntityTypeAndEntityId("GROUP_PLAN_ASSIGNMENT", String.valueOf(entity.getGroupPlanAssignmentId()))
            .ifPresent(approval -> {
                approval.setStatus("APPROVE".equals(decision) ? "APPROVED" : "REJECT".equals(decision) ? "DENIED" : approval.getStatus());
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
