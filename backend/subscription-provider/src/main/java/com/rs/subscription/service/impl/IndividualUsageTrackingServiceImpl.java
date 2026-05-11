package com.rs.subscription.service.impl;

import com.rs.subscription.service.*;

import com.rs.subscription.dto.response.IndividualUsageRowResponse;
import com.rs.subscription.dto.response.IndividualUsageTrackingResponse;
import com.rs.subscription.entity.Subscription;
import com.rs.subscription.enums.CommercialEnums;
import com.rs.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IndividualUsageTrackingServiceImpl implements IndividualUsageTrackingService {

    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final SubscriptionRepository subscriptionRepository;

    public IndividualUsageTrackingResponse getUsageTracking(String purchasedAt, String ctsType,
                                                            String ctsDuration, String ctsStatus, String plan) {
        List<Subscription> subs = subscriptionRepository.findBySubscriberTypeOrderByCreatedAtDesc(
            CommercialEnums.SubscriberType.INDIVIDUAL.name());

        // Stats luôn tính từ toàn bộ dữ liệu, không bị ảnh hưởng bởi filter
        IndividualUsageTrackingResponse.IndividualUsageStatsResponse stats =
                new IndividualUsageTrackingResponse.IndividualUsageStatsResponse();
        stats.setAccounts(subs.stream().map(Subscription::getUserId).filter(u -> u != null).distinct().count());
        stats.setPlansBought(subs.size());
        stats.setSignings(subs.stream().mapToLong(s -> s.getSigningQuotaUsed() != null ? s.getSigningQuotaUsed() : 0).sum());
        stats.setCtsIndividual(subs.stream()
                .filter(s -> s.getPricingRule() != null && CommercialEnums.SubjectType.INDIVIDUAL.name().equals(s.getPricingRule().getSubjectType()))
                .count());
        stats.setCtsOrg(subs.stream()
                .filter(s -> s.getPricingRule() != null && CommercialEnums.SubjectType.ORGANIZATION.name().equals(s.getPricingRule().getSubjectType()))
                .count());
        stats.setCtsIndividualOfOrg(subs.stream()
                .filter(s -> s.getPricingRule() != null && CommercialEnums.SubjectType.INDIVIDUAL_OF_ORG.name().equals(s.getPricingRule().getSubjectType()))
                .count());

        // Map tất cả rows trước, sau đó filter
        List<IndividualUsageRowResponse> rows = subs.stream()
                .map(this::toRow)
                .filter(row -> matchesUsageFilter(row, purchasedAt, ctsType, ctsDuration, ctsStatus, plan))
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

    private boolean matchesUsageFilter(IndividualUsageRowResponse row,
                                        String purchasedAt, String ctsType,
                                        String ctsDuration, String ctsStatus, String plan) {
        if (purchasedAt != null && !purchasedAt.isBlank()) {
            if (row.getPurchasedAt() == null) return false;
            LocalDate filterDate = LocalDate.parse(purchasedAt);
            LocalDate rowDate = LocalDateTime.parse(row.getPurchasedAt(), DATETIME_FMT).toLocalDate();
            if (!rowDate.equals(filterDate)) return false;
        }
        if (ctsType != null && !ctsType.isBlank() && !ctsType.equals(row.getCtsType())) return false;
        if (ctsDuration != null && !ctsDuration.isBlank() && !ctsDuration.equals(String.valueOf(row.getCtsDuration()))) return false;
        if (ctsStatus != null && !ctsStatus.isBlank() && !ctsStatus.equals(row.getCtsStatus())) return false;
        if (plan != null && !plan.isBlank() && !plan.equals(row.getPlan())) return false;
        return true;
    }

    private IndividualUsageRowResponse toRow(Subscription sub) {
        IndividualUsageRowResponse row = new IndividualUsageRowResponse();
        row.setId(sub.getSubscriptionId());
        row.setAccount(sub.getUserId() != null ? String.valueOf(sub.getUserId()) : null);
        row.setPurchasedAt(sub.getCreatedAt() != null ? sub.getCreatedAt().format(DATETIME_FMT) : null);

        if (sub.getPricingRule() != null) {
            row.setCtsType(sub.getPricingRule().getSubjectType());
            row.setCtsDuration(sub.getPricingRule().getCertificateValidityValue());
            row.setFee(sub.getPricingRule().getUnitPrice() != null
                    ? sub.getPricingRule().getUnitPrice().longValue() : 0L);
        } else {
            row.setCtsType(CommercialEnums.SubjectType.INDIVIDUAL.name());
            row.setCtsDuration(12);
            row.setFee(0L);
        }

        row.setCtsStatus(mapSubscriptionStatusToCtsStatus(sub.getStatus()));
        row.setSignings(sub.getSigningQuotaUsed() != null ? sub.getSigningQuotaUsed() : 0);
        row.setPlan(sub.getPlanTemplate() != null ? sub.getPlanTemplate().getPlanName() : null);

        return row;
    }

    private String mapSubscriptionStatusToCtsStatus(String subStatus) {
        if (subStatus == null) return CommercialEnums.IndividualCtsStatus.PENDING_ACTIVATE.name();
        if (CommercialEnums.SubscriptionStatus.ACTIVE.name().equals(subStatus)) {
            return CommercialEnums.IndividualCtsStatus.ACTIVE.name();
        }
        if (CommercialEnums.SubscriptionStatus.EXPIRED.name().equals(subStatus)) {
            return CommercialEnums.IndividualCtsStatus.EXPIRED.name();
        }
        if (CommercialEnums.SubscriptionStatus.CANCELLED.name().equals(subStatus)
            || CommercialEnums.SubscriptionStatus.SUSPENDED.name().equals(subStatus)) {
            return CommercialEnums.IndividualCtsStatus.REVOKED.name();
        }
        return CommercialEnums.IndividualCtsStatus.PENDING_ACTIVATE.name();
    }
}


