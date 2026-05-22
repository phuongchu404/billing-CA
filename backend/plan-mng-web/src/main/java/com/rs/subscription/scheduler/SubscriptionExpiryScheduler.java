package com.rs.subscription.scheduler;

import com.rs.subscription.entity.Subscription;
import com.rs.subscription.enums.CommercialEnums;
import com.rs.subscription.repository.SubscriptionRepository;
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

    // Chạy lúc 01:00 sáng mỗi ngày, cron configurable qua property
    @Scheduled(cron = "${subscription.expiry.cron:0 0 1 * * *}")
    @Transactional
    public void expireOverdueSubscriptions() {
        LocalDate today = LocalDate.now();
        List<Subscription> overdue = subscriptionRepository.findExpiredSubscriptions(today);

        if (overdue.isEmpty()) {
            log.debug("Subscription expiry check: no overdue subscriptions found for {}", today);
            return;
        }

        log.info("Subscription expiry: found {} overdue subscription(s) as of {}", overdue.size(), today);
        for (Subscription sub : overdue) {
            log.info("Auto-expiring subscription id={} (subscriberType={}, endDate={})",
                sub.getSubscriptionId(), sub.getSubscriberType(), sub.getEndDate());
            sub.setStatus(CommercialEnums.SubscriptionStatus.EXPIRED.name());
        }
        subscriptionRepository.saveAll(overdue);
        log.info("Subscription expiry: expired {} subscription(s)", overdue.size());
    }
}
