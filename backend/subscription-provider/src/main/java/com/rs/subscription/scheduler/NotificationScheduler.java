package com.rs.subscription.scheduler;

import com.rs.subscription.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final NotificationService notificationService;

    /** Runs every morning at 09:00 to send upcoming-expiry reminders. */
    @Scheduled(cron = "0 0 9 * * *")
    public void runExpiryNotifications() {
        log.info("Running scheduled expiry notification job");
        try {
            notificationService.sendExpiryNotifications();
        } catch (Exception e) {
            log.error("Expiry notification job failed", e);
        }
    }
}
