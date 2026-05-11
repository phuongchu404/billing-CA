package com.rs.subscription.service;

import com.rs.subscription.dto.request.CreatePlanTemplateRequest;
import com.rs.subscription.dto.request.PlanPricingRuleRequest;
import com.rs.subscription.dto.response.PlanPricingRuleResponse;
import com.rs.subscription.dto.response.PlanTemplateResponse;
import com.rs.subscription.enums.CommercialEnums;
import com.rs.subscription.entity.PlanPricingRule;
import com.rs.subscription.entity.PlanTemplate;
import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;
import com.rs.subscription.repository.PlanTemplateRepository;
import com.rs.subscription.aop.Auditable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface PlanTemplateService {

    List<PlanTemplateResponse> listAll();

    List<PlanTemplateResponse> listBySegment(String customerSegment);

    PlanTemplateResponse getById(Long id);

    PlanTemplateResponse create(CreatePlanTemplateRequest request);

    PlanTemplateResponse update(Long id, CreatePlanTemplateRequest request);

    void delete(Long id);

    PlanTemplate findEntity(Long id);
}
