package com.rs.subscription.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rs.subscription.dto.request.ReviewCommercialRequest;
import com.rs.subscription.dto.response.ApprovalRequestResponse;
import com.rs.subscription.entity.ApprovalRequest;
import com.rs.subscription.enums.CommercialEnums;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.repository.ApprovalRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalRequestService {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final GroupPlanAssignmentService groupPlanAssignmentService;
    private final RetailPlanScheduleService retailPlanScheduleService;
    private final ObjectMapper objectMapper;

    public List<ApprovalRequestResponse> listAll() {
        return approvalRequestRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    @Transactional
    public ApprovalRequestResponse review(Long id, ReviewCommercialRequest request) {
        ApprovalRequest approval = approvalRequestRepository.findById(id)
            .orElseThrow(() -> new SmsException(ErrorCodes.APPROVAL_NOT_FOUND, "Approval request not found: " + id, 404));
        if (!CommercialEnums.ApprovalStatus.PENDING.name().equals(approval.getStatus())) {
            throw new SmsException(ErrorCodes.APPROVAL_ALREADY_REVIEWED, "Approval request already reviewed", 409);
        }
        approval.setStatus("APPROVE".equalsIgnoreCase(request.getDecision())
            ? CommercialEnums.ApprovalStatus.APPROVED.name()
            : CommercialEnums.ApprovalStatus.DENIED.name());
        approval.setReviewedBy(request.getActor());
        approval.setReviewNote(request.getNote());
        approval.setReviewedAt(LocalDateTime.now());
        approvalRequestRepository.save(approval);

        if ("GROUP_PLAN_ASSIGNMENT".equals(approval.getEntityType())) {
            groupPlanAssignmentService.review(Long.valueOf(approval.getEntityId()), request);
        } else if ("RETAIL_PLAN_SCHEDULE".equals(approval.getEntityType())) {
            retailPlanScheduleService.review(Long.valueOf(approval.getEntityId()), request);
        }
        return toResponse(approval);
    }

    private ApprovalRequestResponse toResponse(ApprovalRequest entity) {
        ApprovalRequestResponse response = new ApprovalRequestResponse();
        response.setId(entity.getId());
        response.setRequestType(entity.getRequestType());
        response.setStatus(entity.getStatus());
        response.setRequestedBy(entity.getRequestedBy());
        response.setDescription(entity.getDescription());
        response.setPayload(parsePayload(entity.getRequestPayload()));
        response.setReviewNote(entity.getReviewNote());
        response.setReviewedBy(entity.getReviewedBy());
        response.setReviewedAt(entity.getReviewedAt());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    private java.util.Map<String, Object> parsePayload(String payload) {
        if (payload == null || payload.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(payload, new TypeReference<>() {});
        } catch (Exception ex) {
            return java.util.Map.of("raw", payload);
        }
    }
}
