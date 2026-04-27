package com.rs.subscription.service;

import com.rs.subscription.repository.CertificateUsageRecordRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.Year;

public interface SystemMaintenanceSchedulerService {

    void cleanupExpiredRefreshTokens();

    void rollupDailyUsage();

    void addYearlyPartition();

    void dropOldPartitions();
}
