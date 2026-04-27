package com.rs.subscription.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rs.subscription.dto.request.ApproveStepRequest;
import com.rs.subscription.dto.request.RejectApprovalRequest;
import com.rs.subscription.dto.request.RevisionApprovalRequest;
import com.rs.subscription.dto.request.SubmitApprovalRequest;
import com.rs.subscription.dto.response.ApprovalLevelConfigResponse;
import com.rs.subscription.dto.response.ApprovalStepResponse;
import com.rs.subscription.dto.response.MultiLevelApprovalResponse;
import com.rs.subscription.entity.ApprovalLevelConfig;
import com.rs.subscription.entity.ApprovalRequest;
import com.rs.subscription.entity.ApprovalRequestStep;
import com.rs.subscription.entity.GroupPlanAssignment;
import com.rs.subscription.entity.RetailPlanSchedule;
import com.rs.subscription.enums.CommercialEnums;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.repository.ApprovalLevelConfigRepository;
import com.rs.subscription.repository.ApprovalRequestRepository;
import com.rs.subscription.repository.ApprovalRequestStepRepository;
import com.rs.subscription.repository.GroupPlanAssignmentRepository;
import com.rs.subscription.repository.RetailPlanScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface MultiLevelApprovalService {

    List<MultiLevelApprovalResponse> listAll();

    MultiLevelApprovalResponse getById(Long id);

    List<ApprovalLevelConfigResponse> listLevelConfigs();

    MultiLevelApprovalResponse submit(Long id, SubmitApprovalRequest request);

    Long createAndSubmit(ApprovalRequest draft);

    MultiLevelApprovalResponse approveStep(Long id, ApproveStepRequest request);

    MultiLevelApprovalResponse reject(Long id, RejectApprovalRequest request);

    MultiLevelApprovalResponse requestRevision(Long id, RevisionApprovalRequest request);

    MultiLevelApprovalResponse resubmit(Long id, SubmitApprovalRequest request);
}
