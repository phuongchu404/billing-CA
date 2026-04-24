package com.rs.subscription.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rs.subscription.dto.PagedResponse;
import com.rs.subscription.dto.request.AssignGroupPlanRequest;
import com.rs.subscription.dto.request.CreatePlanRequest;
import com.rs.subscription.dto.request.ReviewApprovalRequest;
import com.rs.subscription.dto.request.SubmitApprovalRequest;
import com.rs.subscription.dto.response.ApprovalRequestResponse;
import com.rs.subscription.entity.ApprovalRequest;
import com.rs.subscription.entity.ApprovalRequest.ApprovalStatus;
import com.rs.subscription.entity.ApprovalRequest.RequestType;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.repository.ApprovalRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalService {

    private final ApprovalRequestRepository approvalRepository;
    private final PlanService planService;
    private final SubscriptionService subscriptionService;
    private final SystemSettingService settingService;
    private final ObjectMapper objectMapper;

    public PagedResponse<ApprovalRequestResponse> listApprovals(String status, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ApprovalRequest> result;
        if (status != null && !status.isBlank()) {
            ApprovalStatus st = ApprovalStatus.valueOf(status.toUpperCase());
            result = approvalRepository.findByStatus(st, pageable);
        } else {
            result = approvalRepository.findAll(pageable);
        }
        return PagedResponse.<ApprovalRequestResponse>builder()
            .content(result.getContent().stream().map(this::toResponse).toList())
            .totalElements(result.getTotalElements())
            .totalPages(result.getTotalPages())
            .page(page)
            .size(size)
            .build();
    }

    public ApprovalRequestResponse getApproval(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public ApprovalRequestResponse submit(SubmitApprovalRequest req, String requestedBy) {
        RequestType type;
        try {
            type = RequestType.valueOf(req.getRequestType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED, "Unknown request type: " + req.getRequestType(), 400);
        }

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(req.getPayload());
        } catch (JsonProcessingException e) {
            throw new SmsException(ErrorCodes.VALIDATION_FAILED, "Invalid payload", 400);
        }

        ApprovalRequest ar = ApprovalRequest.builder()
            .requestType(type)
            .status(ApprovalStatus.PENDING)
            .requestedBy(requestedBy)
            .requestPayload(payloadJson)
            .description(req.getDescription())
            .build();
        ar = approvalRepository.save(ar);

        if (settingService.getBoolean("auto_approval.enabled")) {
            log.info("Auto-approval enabled — executing request {} immediately", ar.getId());
            executeRequest(ar, "SYSTEM");
            ar.setStatus(ApprovalStatus.APPROVED);
            ar.setReviewedBy("SYSTEM");
            ar.setReviewNote("Auto-approved");
            ar.setReviewedAt(LocalDateTime.now());
            ar = approvalRepository.save(ar);
        }

        return toResponse(ar);
    }

    @Transactional
    public ApprovalRequestResponse approve(Long id, ReviewApprovalRequest req, String reviewedBy) {
        ApprovalRequest ar = findById(id);
        ensurePending(ar);
        executeRequest(ar, reviewedBy);
        ar.setStatus(ApprovalStatus.APPROVED);
        ar.setReviewedBy(reviewedBy);
        ar.setReviewNote(req.getNote());
        ar.setReviewedAt(LocalDateTime.now());
        return toResponse(approvalRepository.save(ar));
    }

    @Transactional
    public ApprovalRequestResponse deny(Long id, ReviewApprovalRequest req, String reviewedBy) {
        ApprovalRequest ar = findById(id);
        ensurePending(ar);
        ar.setStatus(ApprovalStatus.DENIED);
        ar.setReviewedBy(reviewedBy);
        ar.setReviewNote(req.getNote());
        ar.setReviewedAt(LocalDateTime.now());
        return toResponse(approvalRepository.save(ar));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void ensurePending(ApprovalRequest ar) {
        if (ar.getStatus() != ApprovalStatus.PENDING) {
            throw new SmsException(ErrorCodes.APPROVAL_ALREADY_REVIEWED,
                "Request has already been " + ar.getStatus().name().toLowerCase(), 422);
        }
    }

    private void executeRequest(ApprovalRequest ar, String actor) {
        try {
            Map<String, Object> payload = objectMapper.readValue(
                ar.getRequestPayload(), new TypeReference<>() {});
            switch (ar.getRequestType()) {
                case CREATE_PLAN -> {
                    CreatePlanRequest planReq = objectMapper.convertValue(payload, CreatePlanRequest.class);
                    planService.createPlan(planReq);
                }
                case ASSIGN_GROUP_PLAN -> {
                    Long groupId = ((Number) payload.get("groupId")).longValue();
                    AssignGroupPlanRequest aReq = new AssignGroupPlanRequest();
                    aReq.setPlanCode((String) payload.get("planCode"));
                    aReq.setActivatedBy(actor);
                    subscriptionService.assignPlanToGroup(groupId, aReq, actor);
                }
                case CANCEL_SUBSCRIPTION -> {
                    Long subId = ((Number) payload.get("subscriptionId")).longValue();
                    subscriptionService.cancelSubscription(subId, actor);
                }
                case SUSPEND_SUBSCRIPTION -> {
                    Long subId = ((Number) payload.get("subscriptionId")).longValue();
                    subscriptionService.suspendSubscription(subId, actor);
                }
            }
        } catch (SmsException e) {
            throw e; // propagate business errors as-is
        } catch (Exception e) {
            log.error("Failed to execute approval request {}: {}", ar.getId(), e.getMessage(), e);
            throw new SmsException(ErrorCodes.APPROVAL_EXECUTION_FAILED,
                "Execution failed: " + e.getMessage(), 500);
        }
    }

    private ApprovalRequest findById(Long id) {
        return approvalRepository.findById(id)
            .orElseThrow(() -> new SmsException(ErrorCodes.APPROVAL_NOT_FOUND,
                "Approval request not found: " + id, 404));
    }

    @SuppressWarnings("unchecked")
    private ApprovalRequestResponse toResponse(ApprovalRequest ar) {
        ApprovalRequestResponse r = new ApprovalRequestResponse();
        r.setId(ar.getId());
        r.setRequestType(ar.getRequestType().name());
        r.setStatus(ar.getStatus().name());
        r.setRequestedBy(ar.getRequestedBy());
        r.setDescription(ar.getDescription());
        r.setReviewedBy(ar.getReviewedBy());
        r.setReviewNote(ar.getReviewNote());
        r.setReviewedAt(ar.getReviewedAt());
        r.setCreatedAt(ar.getCreatedAt());
        r.setUpdatedAt(ar.getUpdatedAt());
        try {
            r.setPayload(objectMapper.readValue(ar.getRequestPayload(), new TypeReference<>() {}));
        } catch (Exception e) {
            log.warn("Could not deserialize payload for approval {}", ar.getId());
        }
        return r;
    }
}
