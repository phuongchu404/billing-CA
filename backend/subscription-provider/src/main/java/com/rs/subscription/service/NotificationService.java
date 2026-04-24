package com.rs.subscription.service;

import com.rs.subscription.entity.Subscription;
import com.rs.subscription.repository.SubscriptionRepository;
import com.rs.subscription.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SystemSettingService settings;
    private final MailService mailService;
    private final SubscriptionRepository subscriptionRepository;
    private final UserAccountRepository userAccountRepository;

    public void sendExpiryNotifications() {
        if (!settings.getBoolean("notification.expiry.enabled")) {
            log.debug("Expiry notifications disabled — skipping");
            return;
        }

        String daysBefore   = settings.get("notification.expiry.days_before", "7");
        String subjTemplate = settings.get("notification.expiry.subject", "Your subscription expires in {days} days");
        String bodyTemplate = settings.get("notification.expiry.body",    "Your subscription expires in {days} days.");

        for (String token : daysBefore.split(",")) {
            int days;
            try { days = Integer.parseInt(token.trim()); } catch (NumberFormatException e) { continue; }

            LocalDate target = LocalDate.now().plusDays(days);
            List<Subscription> expiring = subscriptionRepository.findExpiringSoon(target, target);
            log.info("Expiry check: {} subscription(s) expire in {} day(s)", expiring.size(), days);

            for (Subscription sub : expiring) {
                try {
                    String email = resolveEmail(sub);
                    if (email == null || email.isBlank()) continue;

                    String userName = resolveUserName(sub);
                    Map<String, String> vars = Map.of(
                        "days",       String.valueOf(days),
                        "planName",   sub.getPlan().getPlanName(),
                        "expiryDate", sub.getEndDate().toString(),
                        "userName",   userName
                    );
                    mailService.send(email, render(subjTemplate, vars), render(bodyTemplate, vars));
                } catch (Exception e) {
                    log.warn("Failed to notify subscription {}: {}", sub.getSubscriptionId(), e.getMessage());
                }
            }
        }
    }

    private String resolveEmail(Subscription sub) {
        if (sub.getSubscriberType() == Subscription.SubscriberType.INDIVIDUAL && sub.getUserId() != null) {
            return userAccountRepository.findById(sub.getUserId())
                .map(u -> u.getEmail())
                .orElse(null);
        }
        return sub.getGroup() != null ? sub.getGroup().getContactEmail() : null;
    }

    private String resolveUserName(Subscription sub) {
        if (sub.getSubscriberType() == Subscription.SubscriberType.INDIVIDUAL && sub.getUserId() != null) {
            return userAccountRepository.findById(sub.getUserId())
                .map(u -> u.getFullName())
                .orElse(sub.getUserId());
        }
        return sub.getGroup() != null ? sub.getGroup().getGroupName() : "Subscriber";
    }

    private String render(String template, Map<String, String> vars) {
        String out = template;
        for (var e : vars.entrySet()) out = out.replace("{" + e.getKey() + "}", e.getValue());
        return out;
    }
}
