package com.rs.subscription.service.impl;

import com.rs.subscription.service.*;

import com.rs.subscription.dto.request.UpsertUsageAggregateRequest;
import com.rs.subscription.dto.response.UsageAggregateResponse;
import com.rs.subscription.enums.CommercialEnums;
import com.rs.subscription.entity.UsageAggregate;
import com.rs.subscription.repository.UsageAggregateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsageAggregateServiceImpl implements UsageAggregateService {

    private final UsageAggregateRepository usageAggregateRepository;

    public List<UsageAggregateResponse> listAll() {
        return usageAggregateRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<UsageAggregateResponse> listByScope(String aggregateScope, Long scopeId) {
        return usageAggregateRepository.findByAggregateScopeAndScopeIdOrderByPeriodKeyDesc(
                CommercialEnums.normalize(aggregateScope, CommercialEnums.AggregateScope.class, "aggregateScope"),
                scopeId
            )
            .stream().map(this::toResponse).toList();
    }

    @Transactional
    public UsageAggregateResponse upsert(UpsertUsageAggregateRequest request) {
        UsageAggregate entity = usageAggregateRepository.findByAggregateScopeAndScopeIdAndPeriodTypeAndPeriodKey(
                CommercialEnums.normalize(request.getAggregateScope(), CommercialEnums.AggregateScope.class, "aggregateScope"),
                request.getScopeId(),
                CommercialEnums.normalize(request.getPeriodType(), CommercialEnums.PeriodType.class, "periodType"),
                request.getPeriodKey()
            )
            .orElseGet(UsageAggregate::new);
        entity.setAggregateScope(CommercialEnums.normalize(request.getAggregateScope(), CommercialEnums.AggregateScope.class, "aggregateScope"));
        entity.setScopeId(request.getScopeId());
        entity.setPeriodType(CommercialEnums.normalize(request.getPeriodType(), CommercialEnums.PeriodType.class, "periodType"));
        entity.setPeriodKey(request.getPeriodKey());
        entity.setCertificatesCreated(request.getCertificatesCreated());
        entity.setSigningUsed(request.getSigningUsed());
        entity.setActiveCertificates(request.getActiveCertificates());
        entity.setExpiredCertificates(request.getExpiredCertificates());
        entity.setRevokedCertificates(request.getRevokedCertificates());
        entity.setAmountDue(request.getAmountDue());
        entity.setCurrency(request.getCurrency());
        return toResponse(usageAggregateRepository.save(entity));
    }

    private UsageAggregateResponse toResponse(UsageAggregate entity) {
        UsageAggregateResponse response = new UsageAggregateResponse();
        response.setUsageAggregateId(entity.getUsageAggregateId());
        response.setAggregateScope(entity.getAggregateScope());
        response.setScopeId(entity.getScopeId());
        response.setPeriodType(entity.getPeriodType());
        response.setPeriodKey(entity.getPeriodKey());
        response.setCertificatesCreated(entity.getCertificatesCreated());
        response.setSigningUsed(entity.getSigningUsed());
        response.setActiveCertificates(entity.getActiveCertificates());
        response.setExpiredCertificates(entity.getExpiredCertificates());
        response.setRevokedCertificates(entity.getRevokedCertificates());
        response.setAmountDue(entity.getAmountDue());
        response.setCurrency(entity.getCurrency());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}
