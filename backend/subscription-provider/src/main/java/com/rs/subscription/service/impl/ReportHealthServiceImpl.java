package com.rs.subscription.service.impl;

import com.rs.subscription.dto.response.ReportHealthResponse;
import com.rs.subscription.entity.UsageAggregateRollupLog;
import com.rs.subscription.repository.CertificateProvisioningRepository;
import com.rs.subscription.repository.CertificateUsageRecordRepository;
import com.rs.subscription.repository.UsageAggregateRepository;
import com.rs.subscription.repository.UsageAggregateRollupLogRepository;
import com.rs.subscription.service.ReportHealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReportHealthServiceImpl implements ReportHealthService {

    private final CertificateProvisioningRepository certProvisioningRepository;
    private final CertificateUsageRecordRepository certUsageRepository;
    private final UsageAggregateRepository usageAggregateRepository;
    private final UsageAggregateRollupLogRepository rollupLogRepository;

    @Override
    @Transactional(readOnly = true)
    public ReportHealthResponse getHealth(String periodKey) {
        LocalDateTime fromDt = LocalDate.parse(periodKey + "-01").atStartOfDay();
        LocalDateTime toDt = fromDt.plusMonths(1);

        long rawCertificates = certProvisioningRepository.countCompletedGroupIssuedBetween(fromDt, toDt);
        long rawSignings = certUsageRepository.countGroupSignings(fromDt, toDt);
        Object[] aggregateSums = usageAggregateRepository.sumGroupMonthlyByPeriodKey(periodKey);
        long aggregateCertificates = ((Number) aggregateSums[0]).longValue();
        long aggregateSignings = ((Number) aggregateSums[1]).longValue();

        ReportHealthResponse response = new ReportHealthResponse();
        response.setPeriodKey(periodKey);
        response.setRawCertificates(rawCertificates);
        response.setRawSignings(rawSignings);
        response.setAggregateCertificates(aggregateCertificates);
        response.setAggregateSignings(aggregateSignings);
        response.setCertificateDelta(rawCertificates - aggregateCertificates);
        response.setSigningDelta(rawSignings - aggregateSignings);

        rollupLogRepository.findTopByPeriodKeyOrderByRunAtDesc(periodKey)
            .ifPresent(log -> mapRollup(response, log));
        return response;
    }

    private void mapRollup(ReportHealthResponse response, UsageAggregateRollupLog log) {
        response.setLastRollupAt(log.getRunAt());
        response.setLastRollupStatus(log.getStatus());
        response.setLastRollupGroupsUpdated(log.getGroupsUpdated());
        response.setLastRollupError(log.getErrorMsg());
    }
}
