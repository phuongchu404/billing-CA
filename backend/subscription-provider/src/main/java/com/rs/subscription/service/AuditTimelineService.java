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

public interface AuditTimelineService {

    AuditTimelineResponse getSubscriptionTimeline(Long subscriptionId);

    AuditTimelineResponse getGroupAssignmentTimeline(Long assignmentId);

    AuditTimelineResponse getRetailScheduleTimeline(Long scheduleId);

    AuditTimelineResponse getSettlementTimeline(Long settlementStatementId);
}
