package com.rs.subscription.service;

import com.rs.subscription.dto.request.CreateIndividualPlanConfigRequest;
import com.rs.subscription.dto.request.IndividualApproveRequest;
import com.rs.subscription.dto.request.IndividualRequestApplyRequest;
import com.rs.subscription.dto.response.IndividualPlanConfigDetailResponse;
import com.rs.subscription.dto.response.IndividualPlanConfigListItemResponse;
import com.rs.subscription.dto.response.IndividualPlanConfigSummaryResponse;
import com.rs.subscription.entity.ApprovalRequest;
import com.rs.subscription.entity.AssignmentAudit;
import com.rs.subscription.entity.PlanPricingRule;
import com.rs.subscription.entity.PlanTemplate;
import com.rs.subscription.entity.RetailPlanSchedule;
import com.rs.subscription.enums.CommercialEnums;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.repository.AssignmentAuditRepository;
import com.rs.subscription.repository.PlanTemplateRepository;
import com.rs.subscription.repository.RetailPlanScheduleRepository;
import com.rs.subscription.aop.Auditable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface IndividualPlanConfigService {

    IndividualPlanConfigSummaryResponse getSummary(String status, String applyFrom, String applyUntil, String updatedAt, int page, int size, String sortBy, String sortDir);

    IndividualPlanConfigDetailResponse getDetail(Long id);

    IndividualPlanConfigDetailResponse create(CreateIndividualPlanConfigRequest req);

    IndividualPlanConfigDetailResponse requestApply(Long id, IndividualRequestApplyRequest req);

    IndividualPlanConfigDetailResponse approve(Long id, IndividualApproveRequest req);

    IndividualPlanConfigDetailResponse reject(Long id, String actor);

    IndividualPlanConfigDetailResponse stop(Long id, String actor);

    IndividualPlanConfigDetailResponse deactivate(Long id, String actor);
}
