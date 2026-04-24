package com.rs.subscription.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rs.subscription.dto.response.AuditTimelineEntryResponse;
import com.rs.subscription.dto.response.AuditTimelineResponse;
import com.rs.subscription.entity.ApprovalRequest;
import com.rs.subscription.entity.AssignmentAudit;
import com.rs.subscription.entity.GroupPlanAssignment;
import com.rs.subscription.entity.PaymentRecord;
import com.rs.subscription.entity.RetailPlanSchedule;
import com.rs.subscription.entity.SettlementStatement;
import com.rs.subscription.entity.Subscription;
import com.rs.subscription.entity.SubscriptionAuditLog;
import com.rs.subscription.enums.CommercialEnums;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.repository.ApprovalRequestRepository;
import com.rs.subscription.repository.AssignmentAuditRepository;
import com.rs.subscription.repository.PaymentRecordRepository;
import com.rs.subscription.repository.SettlementStatementRepository;
import com.rs.subscription.repository.SubscriptionAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditTimelineService {

    private final RuntimeSubscriptionService runtimeSubscriptionService;
    private final GroupPlanAssignmentService groupPlanAssignmentService;
    private final RetailPlanScheduleService retailPlanScheduleService;
    private final SubscriptionAuditLogRepository subscriptionAuditLogRepository;
    private final AssignmentAuditRepository assignmentAuditRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final SettlementStatementRepository settlementStatementRepository;
    private final ObjectMapper objectMapper;

    public AuditTimelineResponse getSubscriptionTimeline(Long subscriptionId) {
        Subscription subscription = runtimeSubscriptionService.findEntity(subscriptionId);
        List<AuditTimelineEntryResponse> items = new ArrayList<>();

        subscriptionAuditLogRepository.findBySubscriptionSubscriptionIdOrderByCreatedAtDesc(subscriptionId)
            .stream()
            .map(this::toSubscriptionTimelineEntry)
            .forEach(items::add);

        paymentRecordRepository.findBySubscriptionSubscriptionIdOrderByPaidAtDesc(subscriptionId)
            .stream()
            .map(this::toPaymentTimelineEntry)
            .forEach(items::add);

        items.sort(Comparator.comparing(AuditTimelineEntryResponse::getOccurredAt).reversed());

        AuditTimelineResponse response = new AuditTimelineResponse();
        response.setEntityType("SUBSCRIPTION");
        response.setEntityId(subscriptionId);
        response.setCurrentStatus(subscription.getStatus());
        response.setItems(items);
        return response;
    }

    public AuditTimelineResponse getGroupAssignmentTimeline(Long assignmentId) {
        GroupPlanAssignment assignment = groupPlanAssignmentService.findEntity(assignmentId);
        List<AuditTimelineEntryResponse> items = new ArrayList<>();

        approvalRequestRepository.findAllByEntityTypeAndEntityIdOrderByCreatedAtDesc("GROUP_PLAN_ASSIGNMENT", String.valueOf(assignmentId))
            .stream()
            .map(this::toApprovalTimelineEntry)
            .forEach(items::add);

        assignmentAuditRepository.findByGroupPlanAssignmentGroupPlanAssignmentIdOrderByCreatedAtDesc(assignmentId)
            .stream()
            .map(this::toAssignmentTimelineEntry)
            .forEach(items::add);

        paymentRecordRepository.findByGroupPlanAssignmentGroupPlanAssignmentIdOrderByPaidAtDesc(assignmentId)
            .stream()
            .map(this::toPaymentTimelineEntry)
            .forEach(items::add);

        items.sort(Comparator.comparing(AuditTimelineEntryResponse::getOccurredAt).reversed());

        AuditTimelineResponse response = new AuditTimelineResponse();
        response.setEntityType("GROUP_PLAN_ASSIGNMENT");
        response.setEntityId(assignmentId);
        response.setCurrentStatus(assignment.getAssignmentStatus());
        response.setItems(items);
        return response;
    }

    public AuditTimelineResponse getRetailScheduleTimeline(Long scheduleId) {
        RetailPlanSchedule schedule = retailPlanScheduleService.findEntity(scheduleId);
        List<AuditTimelineEntryResponse> items = new ArrayList<>();

        approvalRequestRepository.findAllByEntityTypeAndEntityIdOrderByCreatedAtDesc("RETAIL_PLAN_SCHEDULE", String.valueOf(scheduleId))
            .stream()
            .map(this::toApprovalTimelineEntry)
            .forEach(items::add);

        assignmentAuditRepository.findByRetailPlanScheduleRetailPlanScheduleIdOrderByCreatedAtDesc(scheduleId)
            .stream()
            .map(this::toAssignmentTimelineEntry)
            .forEach(items::add);

        items.sort(Comparator.comparing(AuditTimelineEntryResponse::getOccurredAt).reversed());

        AuditTimelineResponse response = new AuditTimelineResponse();
        response.setEntityType("RETAIL_PLAN_SCHEDULE");
        response.setEntityId(scheduleId);
        response.setCurrentStatus(schedule.getScheduleStatus());
        response.setItems(items);
        return response;
    }

    public AuditTimelineResponse getSettlementTimeline(Long settlementStatementId) {
        SettlementStatement statement = settlementStatementRepository.findById(settlementStatementId)
            .orElseThrow(() -> new SmsException(
                ErrorCodes.SETTLEMENT_NOT_FOUND,
                "Settlement statement not found: " + settlementStatementId,
                404
            ));

        List<AuditTimelineEntryResponse> items = new ArrayList<>();
        items.add(toSettlementCreatedEntry(statement));

        if (!CommercialEnums.StatementStatus.DRAFT.name().equals(statement.getStatus())) {
            items.add(toSettlementStatusEntry(statement));
        }

        paymentRecordRepository.findBySettlementStatementSettlementStatementIdOrderByPaidAtDesc(settlementStatementId)
            .stream()
            .map(this::toPaymentTimelineEntry)
            .forEach(items::add);

        items.sort(Comparator.comparing(AuditTimelineEntryResponse::getOccurredAt).reversed());

        AuditTimelineResponse response = new AuditTimelineResponse();
        response.setEntityType("SETTLEMENT_STATEMENT");
        response.setEntityId(settlementStatementId);
        response.setCurrentStatus(statement.getStatus());
        response.setItems(items);
        return response;
    }

    private AuditTimelineEntryResponse toSubscriptionTimelineEntry(SubscriptionAuditLog log) {
        AuditTimelineEntryResponse entry = new AuditTimelineEntryResponse();
        entry.setSource("SUBSCRIPTION_AUDIT");
        entry.setEventType("STATUS_CHANGE");
        entry.setTitle(resolveSubscriptionTitle(log));
        entry.setActor(log.getActor());
        entry.setOldStatus(log.getOldStatus());
        entry.setNewStatus(log.getNewStatus());
        entry.setNote(log.getReason());
        entry.setOccurredAt(log.getCreatedAt());
        entry.setMetadata(Map.of(
            "subscriptionId", log.getSubscription().getSubscriptionId(),
            "sourceType", log.getSourceType()
        ));
        return entry;
    }

    private AuditTimelineEntryResponse toAssignmentTimelineEntry(AssignmentAudit audit) {
        AuditTimelineEntryResponse entry = new AuditTimelineEntryResponse();
        entry.setSource("ASSIGNMENT_AUDIT");
        entry.setEventType(audit.getAction());
        entry.setTitle(resolveAssignmentTitle(audit));
        entry.setActor(audit.getActor());
        entry.setOldStatus(audit.getOldStatus());
        entry.setNewStatus(audit.getNewStatus());
        entry.setNote(audit.getNote());
        entry.setOccurredAt(audit.getCreatedAt());

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("assignmentType", audit.getAssignmentType());
        if (audit.getGroupPlanAssignment() != null) {
            metadata.put("groupPlanAssignmentId", audit.getGroupPlanAssignment().getGroupPlanAssignmentId());
        }
        if (audit.getRetailPlanSchedule() != null) {
            metadata.put("retailPlanScheduleId", audit.getRetailPlanSchedule().getRetailPlanScheduleId());
        }
        entry.setMetadata(metadata);
        return entry;
    }

    private AuditTimelineEntryResponse toApprovalTimelineEntry(ApprovalRequest approval) {
        AuditTimelineEntryResponse entry = new AuditTimelineEntryResponse();
        entry.setSource("APPROVAL_REQUEST");
        entry.setEventType(approval.getRequestType());
        entry.setTitle(approval.getDescription());
        entry.setActor(approval.getReviewedBy() != null ? approval.getReviewedBy() : approval.getRequestedBy());
        entry.setOldStatus(null);
        entry.setNewStatus(approval.getStatus());
        entry.setNote(approval.getReviewNote());
        entry.setOccurredAt(resolveApprovalOccurredAt(approval));
        entry.setMetadata(parseJson(approval.getRequestPayload()));
        return entry;
    }

    private AuditTimelineEntryResponse toPaymentTimelineEntry(PaymentRecord payment) {
        AuditTimelineEntryResponse entry = new AuditTimelineEntryResponse();
        entry.setSource("PAYMENT_RECORD");
        entry.setEventType(payment.getPaymentScope());
        entry.setTitle("Payment " + payment.getPaymentStatus().toLowerCase());
        entry.setActor(payment.getPaymentMethod());
        entry.setOldStatus(null);
        entry.setNewStatus(payment.getPaymentStatus());
        entry.setNote(payment.getExternalReference());
        entry.setOccurredAt(payment.getPaidAt());
        entry.setAmount(payment.getAmount());
        entry.setCurrency(payment.getCurrency());

        Map<String, Object> metadata = parseJson(payment.getRawPayload());
        if (metadata == null) {
            metadata = new LinkedHashMap<>();
        }
        metadata.put("externalReference", payment.getExternalReference());
        metadata.put("paymentMethod", payment.getPaymentMethod());
        metadata.put("paymentScope", payment.getPaymentScope());
        if (payment.getSubscription() != null) {
            metadata.put("subscriptionId", payment.getSubscription().getSubscriptionId());
        }
        if (payment.getGroupPlanAssignment() != null) {
            metadata.put("groupPlanAssignmentId", payment.getGroupPlanAssignment().getGroupPlanAssignmentId());
        }
        if (payment.getSettlementStatement() != null) {
            metadata.put("settlementStatementId", payment.getSettlementStatement().getSettlementStatementId());
        }
        entry.setMetadata(metadata);
        return entry;
    }

    private AuditTimelineEntryResponse toSettlementCreatedEntry(SettlementStatement statement) {
        AuditTimelineEntryResponse entry = new AuditTimelineEntryResponse();
        entry.setSource("SETTLEMENT_STATEMENT");
        entry.setEventType("GENERATED");
        entry.setTitle("Settlement statement generated");
        entry.setActor(statement.getGeneratedBy());
        entry.setNewStatus(CommercialEnums.StatementStatus.DRAFT.name());
        entry.setOccurredAt(resolveGeneratedAt(statement));
        entry.setAmount(statement.getTotalAmount());
        entry.setCurrency(statement.getCurrency());
        entry.setMetadata(settlementMetadata(statement));
        return entry;
    }

    private AuditTimelineEntryResponse toSettlementStatusEntry(SettlementStatement statement) {
        AuditTimelineEntryResponse entry = new AuditTimelineEntryResponse();
        entry.setSource("SETTLEMENT_STATEMENT");
        entry.setEventType("STATUS_CHANGE");
        entry.setTitle("Settlement statement " + statement.getStatus().toLowerCase());
        entry.setActor(statement.getGeneratedBy());
        entry.setOldStatus(CommercialEnums.StatementStatus.DRAFT.name());
        entry.setNewStatus(statement.getStatus());
        entry.setOccurredAt(statement.getUpdatedAt());
        entry.setAmount(statement.getTotalAmount());
        entry.setCurrency(statement.getCurrency());
        entry.setMetadata(settlementMetadata(statement));
        return entry;
    }

    private Map<String, Object> settlementMetadata(SettlementStatement statement) {
        return Map.of(
            "groupId", statement.getGroup().getGroupId(),
            "fromDate", statement.getFromDate(),
            "toDate", statement.getToDate(),
            "totalCertificates", statement.getTotalCertificates(),
            "totalSignings", statement.getTotalSignings()
        );
    }

    private LocalDateTime resolveApprovalOccurredAt(ApprovalRequest approval) {
        return approval.getReviewedAt() != null ? approval.getReviewedAt() : approval.getCreatedAt();
    }

    private LocalDateTime resolveGeneratedAt(SettlementStatement statement) {
        return statement.getGeneratedAt() != null ? statement.getGeneratedAt() : statement.getCreatedAt();
    }

    private Map<String, Object> parseJson(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(rawJson, new TypeReference<>() {});
        } catch (Exception ex) {
            return Map.of("raw", rawJson);
        }
    }

    private String resolveSubscriptionTitle(SubscriptionAuditLog log) {
        if (log.getOldStatus() == null) {
            return "Subscription issued";
        }
        return "Subscription status changed to " + log.getNewStatus().toLowerCase();
    }

    private String resolveAssignmentTitle(AssignmentAudit audit) {
        return switch (audit.getAction()) {
            case "REQUEST" -> "Assignment requested";
            case "APPROVE" -> "Assignment approved";
            case "REJECT" -> "Assignment rejected";
            case "ACTIVATE" -> "Assignment activated";
            case "STOP" -> "Assignment stopped";
            case "EXPIRE" -> "Assignment expired";
            default -> "Assignment updated";
        };
    }
}
