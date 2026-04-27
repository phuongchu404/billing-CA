package com.rs.subscription.service;

import com.rs.subscription.dto.request.UpsertGroupRequest;
import com.rs.subscription.dto.response.GroupDetailResponse;
import com.rs.subscription.dto.response.GroupListItemResponse;
import com.rs.subscription.dto.response.PlanHistoryResponse;
import com.rs.subscription.entity.Group;
import com.rs.subscription.entity.GroupContact;
import com.rs.subscription.entity.GroupPlanAssignment;
import com.rs.subscription.entity.PlanPricingRule;
import com.rs.subscription.entity.UserAccount;
import com.rs.subscription.enums.CommercialEnums;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.repository.GroupContactRepository;
import com.rs.subscription.repository.GroupPlanAssignmentRepository;
import com.rs.subscription.repository.GroupRepository;
import com.rs.subscription.repository.UserAccountRepository;
import com.rs.subscription.repository.UsageAggregateRepository;
import com.rs.subscription.aop.Auditable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface GroupService {

    List<GroupListItemResponse> listAll();

    GroupDetailResponse getById(Long id);

    GroupDetailResponse create(UpsertGroupRequest request);

    GroupDetailResponse update(Long id, UpsertGroupRequest request);

    GroupDetailResponse assignOwner(Long groupId, String ownerUserId);

    void suspend(Long id);

    void activate(Long id);

    List<PlanHistoryResponse> getPlanHistory(Long groupId);

    Group findEntity(Long id);
}
