package com.rs.subscription.service;

import com.rs.subscription.aop.TrackSubscriptionAudit;
import com.rs.subscription.dto.request.CreateRuntimeSubscriptionRequest;
import com.rs.subscription.dto.response.RuntimeSubscriptionResponse;
import com.rs.subscription.enums.CommercialEnums;
import com.rs.subscription.entity.*;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.repository.GroupRepository;
import com.rs.subscription.repository.PlanPricingRuleRepository;
import com.rs.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface RuntimeSubscriptionService {

    List<RuntimeSubscriptionResponse> listAll();

    List<RuntimeSubscriptionResponse> listByUserId(Long userId);

    RuntimeSubscriptionResponse getById(Long id);

    RuntimeSubscriptionResponse create(CreateRuntimeSubscriptionRequest request);

    RuntimeSubscriptionResponse updateStatus(Long id, String status, String actor);

    Subscription findEntity(Long id);
}
