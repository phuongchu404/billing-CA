package com.rs.subscription.service;

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

public interface RetailPlanScheduleService {

    List<RetailPlanScheduleResponse> listAll();

    RetailPlanScheduleResponse getById(Long id);

    RetailPlanScheduleResponse create(CreateRetailPlanScheduleRequest request);

    RetailPlanScheduleResponse review(Long id, ReviewCommercialRequest request);

    RetailPlanSchedule findEntity(Long id);
}
