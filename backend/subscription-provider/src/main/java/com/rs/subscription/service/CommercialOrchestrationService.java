package com.rs.subscription.service;

import com.rs.subscription.dto.request.CreateRuntimeSubscriptionRequest;
import com.rs.subscription.dto.request.ExecuteGroupAssignmentFlowRequest;
import com.rs.subscription.dto.request.ExecuteRetailPlanFlowRequest;
import com.rs.subscription.dto.request.GenerateSettlementFlowRequest;
import com.rs.subscription.dto.request.ReviewCommercialRequest;
import com.rs.subscription.dto.response.CommercialFlowResponse;
import com.rs.subscription.dto.response.RuntimeSubscriptionResponse;
import com.rs.subscription.dto.response.SettlementStatementResponse;
import com.rs.subscription.entity.CertificateUsageRecord;
import com.rs.subscription.entity.Group;
import com.rs.subscription.entity.GroupPlanAssignment;
import com.rs.subscription.entity.PlanPricingRule;
import com.rs.subscription.entity.RetailPlanSchedule;
import com.rs.subscription.entity.SettlementStatement;
import com.rs.subscription.entity.Subscription;
import com.rs.subscription.entity.UsageAggregate;
import com.rs.subscription.enums.CommercialEnums;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.repository.CertificateUsageRecordRepository;
import com.rs.subscription.repository.GroupRepository;
import com.rs.subscription.repository.PlanPricingRuleRepository;
import com.rs.subscription.repository.SettlementStatementRepository;
import com.rs.subscription.repository.SubscriptionRepository;
import com.rs.subscription.repository.UsageAggregateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public interface CommercialOrchestrationService {

    CommercialFlowResponse executeGroupAssignmentFlow(Long assignmentId, ExecuteGroupAssignmentFlowRequest request);

    CommercialFlowResponse executeRetailPlanFlow(Long scheduleId, ExecuteRetailPlanFlowRequest request);

    CommercialFlowResponse generateSettlement(Long groupId, GenerateSettlementFlowRequest request);
}
