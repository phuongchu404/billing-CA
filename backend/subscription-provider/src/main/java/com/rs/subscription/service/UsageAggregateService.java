package com.rs.subscription.service;

import com.rs.subscription.dto.request.UpsertUsageAggregateRequest;
import com.rs.subscription.dto.response.UsageAggregateResponse;
import com.rs.subscription.enums.CommercialEnums;
import com.rs.subscription.entity.UsageAggregate;
import com.rs.subscription.repository.UsageAggregateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface UsageAggregateService {

    List<UsageAggregateResponse> listAll();

    List<UsageAggregateResponse> listByScope(String aggregateScope, Long scopeId);

    UsageAggregateResponse upsert(UpsertUsageAggregateRequest request);
}
