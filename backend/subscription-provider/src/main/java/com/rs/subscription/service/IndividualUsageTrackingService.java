package com.rs.subscription.service;

import com.rs.subscription.dto.response.IndividualUsageRowResponse;
import com.rs.subscription.dto.response.IndividualUsageTrackingResponse;
import com.rs.subscription.entity.Subscription;
import com.rs.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IndividualUsageTrackingService {

    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final SubscriptionRepository subscriptionRepository;

    public IndividualUsageTrackingResponse getUsageTracking() {
        List<Subscription> subs = subscriptionRepository.findBySubscriberTypeOrderByCreatedAtDesc("INDIVIDUAL");

        IndividualUsageTrackingResponse.IndividualUsageStatsResponse stats =
                new IndividualUsageTrackingResponse.IndividualUsageStatsResponse();
        stats.setAccounts(subs.stream().map(Subscription::getUserId).filter(u -> u != null).distinct().count());
        stats.setPlansBought(subs.size());
        stats.setSignings(subs.stream().mapToLong(s -> s.getSigningQuotaUsed() != null ? s.getSigningQuotaUsed() : 0).sum());
        stats.setCtsIndividual(subs.stream()
                .filter(s -> s.getPricingRule() != null && "INDIVIDUAL".equals(s.getPricingRule().getSubjectType()))
                .count());
        stats.setCtsOrg(subs.stream()
                .filter(s -> s.getPricingRule() != null && "ORGANIZATION".equals(s.getPricingRule().getSubjectType()))
                .count());
        stats.setCtsIndividualOfOrg(subs.stream()
                .filter(s -> s.getPricingRule() != null && "INDIVIDUAL_OF_ORG".equals(s.getPricingRule().getSubjectType()))
                .count());

        List<IndividualUsageRowResponse> rows = subs.stream()
                .map(this::toRow)
                .collect(Collectors.toList());

        String lastUpdated = subs.stream()
                .map(Subscription::getUpdatedAt)
                .filter(d -> d != null)
                .max(java.util.Comparator.naturalOrder())
                .map(d -> d.format(DATETIME_FMT))
                .orElse(null);

        IndividualUsageTrackingResponse response = new IndividualUsageTrackingResponse();
        response.setStats(stats);
        response.setList(rows);
        response.setLastUpdated(lastUpdated);
        return response;
    }

    private IndividualUsageRowResponse toRow(Subscription sub) {
        IndividualUsageRowResponse row = new IndividualUsageRowResponse();
        row.setId(sub.getSubscriptionId());
        row.setAccount(sub.getUserId());
        row.setPurchasedAt(sub.getCreatedAt() != null ? sub.getCreatedAt().format(DATETIME_FMT) : null);

        if (sub.getPricingRule() != null) {
            row.setCtsType(sub.getPricingRule().getSubjectType());
            row.setCtsDuration(sub.getPricingRule().getCertificateValidityValue());
            row.setFee(sub.getPricingRule().getUnitPrice() != null
                    ? sub.getPricingRule().getUnitPrice().longValue() : 0L);
        } else {
            row.setCtsType("INDIVIDUAL");
            row.setCtsDuration(12);
            row.setFee(0L);
        }

        row.setCtsStatus(mapSubscriptionStatusToCtsStatus(sub.getStatus()));
        row.setSignings(sub.getSigningQuotaUsed() != null ? sub.getSigningQuotaUsed() : 0);
        row.setPlan(sub.getPlanTemplate() != null ? sub.getPlanTemplate().getPlanName() : null);

        return row;
    }

    private String mapSubscriptionStatusToCtsStatus(String subStatus) {
        if (subStatus == null) return "PENDING_ACTIVATE";
        return switch (subStatus) {
            case "ACTIVE" -> "ACTIVE";
            case "EXPIRED" -> "EXPIRED";
            case "CANCELLED", "SUSPENDED" -> "REVOKED";
            default -> "PENDING_ACTIVATE";
        };
    }
}
