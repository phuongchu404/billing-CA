package com.rs.subscription.service;

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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface GroupPlanAssignmentService {

    List<GroupPlanAssignmentResponse> listAll();

    List<GroupPlanAssignmentResponse> listByGroup(Long groupId);

    GroupPlanAssignmentResponse getById(Long id);

    GroupPlanAssignmentResponse create(CreateGroupPlanAssignmentRequest request);

    GroupPlanAssignmentResponse review(Long id, ReviewCommercialRequest request);

    GroupPlanAssignment findEntity(Long id);
}
