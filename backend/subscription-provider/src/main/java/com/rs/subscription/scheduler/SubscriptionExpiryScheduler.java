package com.rs.subscription.scheduler;

import com.rs.subscription.entity.Subscription;
import com.rs.subscription.entity.SubscriptionAuditLog;
import com.rs.subscription.repository.SubscriptionRepository;
import com.rs.subscription.repository.SubscriptionAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionExpiryScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionAuditLogRepository auditLogRepository;

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void expireSubscriptions() {
        List<Subscription> expired = subscriptionRepository.findExpiredSubscriptions(LocalDate.now());
        log.info("Found {} subscriptions to expire", expired.size());
        for (Subscription sub : expired) {
            String oldStatus = sub.getStatus().name();
            sub.setStatus(Subscription.SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(sub);
            SubscriptionAuditLog log2 = SubscriptionAuditLog.builder()
                .subscription(sub)
                .actor("SCHEDULER")
                .oldStatus(oldStatus)
                .newStatus("EXPIRED")
                .reason("Subscription past end date")
                .build();
            auditLogRepository.save(log2);
        }
        log.info("Expired {} subscriptions", expired.size());
    }
}
