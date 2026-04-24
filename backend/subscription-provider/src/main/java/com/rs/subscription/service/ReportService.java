package com.rs.subscription.service;

import com.rs.subscription.dto.response.SubscriptionResponse;
import com.rs.subscription.entity.Subscription;
import com.rs.subscription.entity.Subscription.SubscriptionStatus;
import com.rs.subscription.repository.CertificateProvisioningRepository;
import com.rs.subscription.repository.CertificateUsageRecordRepository;
import com.rs.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionService subscriptionService;
    private final CertificateProvisioningRepository certProvisioningRepository;
    private final CertificateUsageRecordRepository usageRecordRepository;

    public Map<String, Object> getSubscriptionSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalActive", subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE));
        summary.put("totalPending", subscriptionRepository.countByStatus(SubscriptionStatus.PENDING));
        summary.put("totalExpired", subscriptionRepository.countByStatus(SubscriptionStatus.EXPIRED));
        summary.put("totalCancelled", subscriptionRepository.countByStatus(SubscriptionStatus.CANCELLED));
        summary.put("totalSuspended", subscriptionRepository.countByStatus(SubscriptionStatus.SUSPENDED));
        summary.put("individualActive", subscriptionRepository.countIndividualByStatus(SubscriptionStatus.ACTIVE));
        summary.put("groupActive", subscriptionRepository.countGroupByStatus(SubscriptionStatus.ACTIVE));
        return summary;
    }

    public Map<String, Object> getSubscriptionSummaryByType(Subscription.SubscriberType type) {
        Map<String, Object> summary = new LinkedHashMap<>();
        if (type == Subscription.SubscriberType.INDIVIDUAL) {
            summary.put("active",    subscriptionRepository.countIndividualByStatus(SubscriptionStatus.ACTIVE));
            summary.put("pending",   subscriptionRepository.countIndividualByStatus(SubscriptionStatus.PENDING));
            summary.put("expired",   subscriptionRepository.countIndividualByStatus(SubscriptionStatus.EXPIRED));
            summary.put("cancelled", subscriptionRepository.countIndividualByStatus(SubscriptionStatus.CANCELLED));
            summary.put("suspended", subscriptionRepository.countIndividualByStatus(SubscriptionStatus.SUSPENDED));
        } else {
            summary.put("active",    subscriptionRepository.countGroupByStatus(SubscriptionStatus.ACTIVE));
            summary.put("pending",   subscriptionRepository.countGroupByStatus(SubscriptionStatus.PENDING));
            summary.put("expired",   subscriptionRepository.countGroupByStatus(SubscriptionStatus.EXPIRED));
            summary.put("cancelled", subscriptionRepository.countGroupByStatus(SubscriptionStatus.CANCELLED));
            summary.put("suspended", subscriptionRepository.countGroupByStatus(SubscriptionStatus.SUSPENDED));
        }
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        summary.put("newCertsThisMonth", certProvisioningRepository.countBySubscriberTypeAndCreatedAtAfter(type, monthStart));
        summary.put("usageThisMonth",    usageRecordRepository.countBySubscriberTypeAndUsedAtAfter(type, monthStart));
        int expiryDays = (type == Subscription.SubscriberType.GROUP) ? 90 : 30;
        LocalDate today = LocalDate.now();
        summary.put("expiringSoon", subscriptionRepository.countExpiringSoonBySubscriberType(today, today.plusDays(expiryDays), type));
        return summary;
    }

    public List<SubscriptionResponse> getExpiringSoon(int days) {
        LocalDate from = LocalDate.now();
        LocalDate to = from.plusDays(days);
        return subscriptionRepository.findExpiringSoon(from, to).stream()
            .map(s -> subscriptionService.getSubscriptionById(s.getSubscriptionId()))
            .collect(Collectors.toList());
    }

    public List<SubscriptionResponse> getExpiringSoonByType(int days, Subscription.SubscriberType type) {
        LocalDate from = LocalDate.now();
        LocalDate to = from.plusDays(days);
        return subscriptionRepository.findExpiringSoonBySubscriberType(from, to, type).stream()
            .map(s -> subscriptionService.getSubscriptionById(s.getSubscriptionId()))
            .collect(Collectors.toList());
    }

    public List<SubscriptionResponse> getAllSubscriptions() {
        return subscriptionRepository.findAll().stream()
            .map(s -> subscriptionService.getSubscriptionById(s.getSubscriptionId()))
            .collect(Collectors.toList());
    }
}
