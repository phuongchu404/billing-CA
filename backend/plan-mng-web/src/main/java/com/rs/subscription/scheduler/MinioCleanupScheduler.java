package com.rs.subscription.scheduler;

import com.rs.subscription.entity.MinioOrphanTracking;
import com.rs.subscription.repository.MinioOrphanTrackingRepository;
import com.rs.subscription.service.MinioStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Periodically removes MinIO images that were uploaded but never confirmed
 * (i.e., the user closed the form without saving the plan).
 * Grace period and schedule are configurable via application properties.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MinioCleanupScheduler {

    private final MinioOrphanTrackingRepository orphanRepo;
    private final MinioStorageService minioStorageService;

    /** Files unconfirmed after this many minutes are considered orphans. Default 60 min. */
    @Value("${minio.cleanup.grace-period-minutes:60}")
    private int gracePeriodMinutes;

    @Scheduled(cron = "${minio.cleanup.cron:0 0 * * * *}")
    @Transactional
    public void deleteOrphanFiles() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(gracePeriodMinutes);
        List<MinioOrphanTracking> orphans = orphanRepo.findByConfirmedFalseAndUploadedAtBefore(cutoff);

        if (orphans.isEmpty()) return;

        log.info("MinIO cleanup: {} orphan file(s) older than {} min", orphans.size(), gracePeriodMinutes);
        int deleted = 0;
        for (MinioOrphanTracking orphan : orphans) {
            minioStorageService.deleteByStoragePath(orphan.getBucket() + "/" + orphan.getObjectName());
            orphanRepo.delete(orphan);
            deleted++;
        }
        log.info("MinIO cleanup: deleted {} orphan file(s)", deleted);
    }
}
