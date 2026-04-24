package com.rs.subscription.scheduler;

import com.rs.subscription.service.CertificateProvisioningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CertificateRetryScheduler {

    private final CertificateProvisioningService certService;

    @Scheduled(cron = "${rs.core.cert.retry-cron:0 */5 * * * *}")
    public void retryFailedCertificates() {
        log.debug("Running certificate retry scheduler");
        try {
            certService.retryPendingProvisions();
        } catch (Exception e) {
            log.error("Error in certificate retry scheduler", e);
        }
    }
}
