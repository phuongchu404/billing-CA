package com.rs.subscription.scheduler;

import com.rs.subscription.repository.CertificateUsageRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class UsageRollupScheduler {

    private final CertificateUsageRecordRepository usageRepo;

    @Value("${rs.usage.rollup.enabled:true}")
    private boolean rollupEnabled;

    /** Runs at 01:30 daily; aggregates yesterday's raw events into certificate_usage_daily. */
    @Scheduled(cron = "0 30 1 * * *")
    @Transactional
    public void rollupYesterday() {
        if (!rollupEnabled) return;
        rollupForDate(LocalDate.now().minusDays(1));
    }

    @Transactional
    public void rollupForDate(LocalDate date) {
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to   = date.plusDays(1).atStartOfDay();
        log.info("Running usage rollup for date={}", date);
        usageRepo.upsertDailyRollup(from, to);
        log.info("Usage rollup complete for date={}", date);
    }
}
