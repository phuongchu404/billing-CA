package com.rs.subscription.service;

import com.rs.subscription.dto.request.AddGroupPlanRequest;
import com.rs.subscription.dto.request.CreateGroupPlanAssignmentRequest;
import com.rs.subscription.dto.request.CreatePlanTemplateRequest;
import com.rs.subscription.dto.request.ProvisionGroupRequest;
import com.rs.subscription.dto.request.UpsertGroupRequest;
import com.rs.subscription.dto.response.GroupDetailResponse;
import com.rs.subscription.dto.response.GroupPlanAssignmentResponse;
import com.rs.subscription.dto.response.PlanTemplateResponse;
import com.rs.subscription.dto.response.ProvisionGroupResponse;
import com.rs.subscription.entity.Group;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.repository.GroupRepository;
import com.rs.subscription.enums.CommercialEnums;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

public interface GroupProvisioningService {

    ProvisionGroupResponse provision(ProvisionGroupRequest req);

    GroupPlanAssignmentResponse addPlanToGroup(Long groupId, AddGroupPlanRequest req);
}
