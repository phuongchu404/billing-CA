package com.rs.subscription.service.impl;

import com.rs.subscription.service.*;

import com.rs.subscription.aop.TrackAssignmentAudit;
import com.rs.subscription.dto.request.CreateRetailPlanScheduleRequest;
import com.rs.subscription.dto.request.ReviewCommercialRequest;
import com.rs.subscription.dto.response.AssignmentAuditResponse;
import com.rs.subscription.dto.response.RetailPlanScheduleResponse;
import com.rs.subscription.enums.CommercialEnums;
import com.rs.subscription.entity.ApprovalRequest;
import com.rs.subscription.entity.AssignmentAudit;
import com.rs.subscription.entity.PlanTemplate;
import com.rs.subscription.entity.RetailPlanSchedule;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.repository.ApprovalRequestRepository;
import com.rs.subscription.repository.AssignmentAuditRepository;
import com.rs.subscription.repository.RetailPlanScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RetailPlanScheduleServiceImpl implements RetailPlanScheduleService {

    private final RetailPlanScheduleRepository scheduleRepository;
    private final PlanTemplateService planTemplateService;
    private final AssignmentAuditRepository assignmentAuditRepository;
    private final ApprovalRequestRepository approvalRequestRepository;

    public List<RetailPlanScheduleResponse> listAll() {
        return scheduleRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    public RetailPlanScheduleResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    @Transactional
    @TrackAssignmentAudit(
        assignmentType = CommercialEnums.ASSIGNMENT_TYPE_RETAIL_PLAN,
        entityId = "#result.retailPlanScheduleId",
        action = CommercialEnums.AUDIT_ACTION_REQUEST,
        actor = "#p0.requestedBy"
    )
    public RetailPlanScheduleResponse create(CreateRetailPlanScheduleRequest request) {
        PlanTemplate template = planTemplateService.findEntity(request.getPlanTemplateId());
        RetailPlanSchedule entity = RetailPlanSchedule.builder()
            .planTemplate(template)
            .scheduleStatus(CommercialEnums.normalize(request.getScheduleStatus(), CommercialEnums.ScheduleStatus.class, "scheduleStatus"))
            .applyFrom(request.getApplyFrom())
            .applyTo(request.getApplyTo())
            .requestedBy(request.getRequestedBy())
            .requestedAt(LocalDateTime.now())
            .build();
        RetailPlanSchedule saved = scheduleRepository.save(entity);
        approvalRequestRepository.save(ApprovalRequest.builder()
            .requestType(CommercialEnums.RequestType.REQUEST_RETAIL_PLAN_SCHEDULE.name())
            .status(CommercialEnums.ApprovalStatus.PENDING.name())
            .requestedBy(request.getRequestedBy())
            .entityType("RETAIL_PLAN_SCHEDULE")
            .entityId(String.valueOf(saved.getRetailPlanScheduleId()))
            .requestPayload("{\"planTemplateId\":" + template.getPlanTemplateId() + "}")
            .description("Request retail plan schedule")
            .build());
        return toResponse(saved);
    }

    @Transactional
    @TrackAssignmentAudit(
        assignmentType = CommercialEnums.ASSIGNMENT_TYPE_RETAIL_PLAN,
        entityId = "#p0",
        action = "#p1.decision.toUpperCase()",
        actor = "#p1.actor",
        note = "#p1.note"
    )
    public RetailPlanScheduleResponse review(Long id, ReviewCommercialRequest request) {
        RetailPlanSchedule entity = findEntity(id);
        String decision = CommercialEnums.normalize(request.getDecision(), CommercialEnums.ReviewDecision.class, "decision");
        if (CommercialEnums.ReviewDecision.APPROVE.name().equals(decision)) {
            entity.setScheduleStatus(CommercialEnums.ScheduleStatus.APPROVED.name());
            entity.setApprovedBy(request.getActor());
            entity.setApprovedAt(LocalDateTime.now());
        } else if (CommercialEnums.ReviewDecision.ACTIVATE.name().equals(decision)) {
            entity.setScheduleStatus(CommercialEnums.ScheduleStatus.ACTIVE.name());
        } else if (CommercialEnums.ReviewDecision.STOP.name().equals(decision)) {
            entity.setScheduleStatus(CommercialEnums.ScheduleStatus.INACTIVE.name());
        } else if (CommercialEnums.ReviewDecision.REJECT.name().equals(decision)) {
            entity.setScheduleStatus(CommercialEnums.ScheduleStatus.INACTIVE.name());
        } else {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED, "Unsupported decision: " + request.getDecision(), 400);
        }
        updateApproval(entity, decision, request.getActor(), request.getNote());
        return toResponse(scheduleRepository.save(entity));
    }

    public RetailPlanSchedule findEntity(Long id) {
        return scheduleRepository.findById(id)
            .orElseThrow(() -> new SmsException(ErrorCodes.SCHEDULE_NOT_FOUND, "Retail plan schedule not found: " + id, 404));
    }

    private void updateApproval(RetailPlanSchedule entity, String decision, String actor, String note) {
        approvalRequestRepository.findByEntityTypeAndEntityId("RETAIL_PLAN_SCHEDULE", String.valueOf(entity.getRetailPlanScheduleId()))
            .ifPresent(approval -> {
                approval.setStatus(CommercialEnums.ReviewDecision.APPROVE.name().equals(decision)
                    ? CommercialEnums.ApprovalStatus.APPROVED.name()
                    : CommercialEnums.ReviewDecision.REJECT.name().equals(decision)
                        ? CommercialEnums.ApprovalStatus.DENIED.name()
                        : approval.getStatus());
                approval.setReviewedBy(actor);
                approval.setReviewNote(note);
                approval.setReviewedAt(LocalDateTime.now());
                approvalRequestRepository.save(approval);
            });
    }

    private RetailPlanScheduleResponse toResponse(RetailPlanSchedule entity) {
        RetailPlanScheduleResponse response = new RetailPlanScheduleResponse();
        response.setRetailPlanScheduleId(entity.getRetailPlanScheduleId());
        response.setPlanTemplateId(entity.getPlanTemplate().getPlanTemplateId());
        response.setPlanCode(entity.getPlanTemplate().getPlanCode());
        response.setPlanName(entity.getPlanTemplate().getPlanName());
        response.setScheduleStatus(entity.getScheduleStatus());
        response.setApplyFrom(entity.getApplyFrom());
        response.setApplyTo(entity.getApplyTo());
        response.setRequestedBy(entity.getRequestedBy());
        response.setRequestedAt(entity.getRequestedAt());
        response.setApprovedBy(entity.getApprovedBy());
        response.setApprovedAt(entity.getApprovedAt());
        response.setAudits(assignmentAuditRepository.findByRetailPlanScheduleRetailPlanScheduleIdOrderByCreatedAtDesc(entity.getRetailPlanScheduleId())
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
