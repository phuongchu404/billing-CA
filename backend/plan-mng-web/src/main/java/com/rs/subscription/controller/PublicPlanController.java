package com.rs.subscription.controller;

import com.rs.subscription.entity.PlanPricingRule;
import com.rs.subscription.entity.PlanSubjectConfig;
import com.rs.subscription.entity.PlanTemplate;
import com.rs.subscription.entity.RetailPlanSchedule;
import com.rs.subscription.enums.CommercialEnums;
import com.rs.subscription.repository.RetailPlanScheduleRepository;
import com.rs.subscription.service.MinioStorageService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Public (no auth) API consumed by frontend-public to display the pricing cards.
 * Returns the currently active/approved individual plan's subject configs.
 * Display price is derived automatically from the minimum fee of each subject type's pricing rules.
 */
@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class PublicPlanController {

    private final RetailPlanScheduleRepository scheduleRepo;
    private final MinioStorageService minioStorageService;

    private static final List<String> SUBJECT_ORDER = Arrays.asList(
            "INDIVIDUAL", "ORGANIZATION", "INDIVIDUAL_OF_ORG"
    );

    @GetMapping("/plans")
    public List<PublicPlanCard> getPublicPlans() {
        Optional<RetailPlanSchedule> activeSchedule =
                scheduleRepo.findTopByScheduleStatusOrderByApplyFromAsc(CommercialEnums.ScheduleStatus.ACTIVE.name())
                .or(() -> scheduleRepo.findTopByScheduleStatusOrderByApplyFromAsc(
                        CommercialEnums.ScheduleStatus.APPROVED.name()));

        if (activeSchedule.isEmpty()) return List.of();

        PlanTemplate template = activeSchedule.get().getPlanTemplate();

        // Min fee per subject type — from active pricing rules
        Map<String, BigDecimal> minFeeBySubject = template.getPricingRules().stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsActive()) && r.getUnitPrice() != null)
                .collect(Collectors.groupingBy(
                        PlanPricingRule::getSubjectType,
                        Collectors.mapping(PlanPricingRule::getUnitPrice,
                                Collectors.minBy(BigDecimal::compareTo))))
                .entrySet().stream()
                .filter(e -> e.getValue().isPresent())
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));

        // Subject configs indexed by subjectType
        Map<String, PlanSubjectConfig> configBySubject = template.getSubjectConfigs().stream()
                .collect(Collectors.toMap(PlanSubjectConfig::getSubjectType, c -> c));

        return SUBJECT_ORDER.stream()
                .map(subjectType -> {
                    PlanSubjectConfig config = configBySubject.get(subjectType);
                    BigDecimal minFee = minFeeBySubject.get(subjectType);
                    if (config == null && minFee == null) return null;

                    PublicPlanCard card = new PublicPlanCard();
                    card.setSubjectType(subjectType);
                    card.setPlanName(template.getPlanName());
                    card.setIconUrl(config != null ? minioStorageService.toPublicUrl(config.getIconUrl()) : null);
                    card.setMinFee(minFee);
                    card.setMinFeeFormatted(minFee != null ? formatVnd(minFee) : null);
                    card.setFeatures(config != null ? parseFeatures(config.getFeaturesText()) : List.of());
                    return card;
                })
                .filter(c -> c != null)
                .collect(Collectors.toList());
    }

    private List<String> parseFeatures(String featuresText) {
        if (featuresText == null || featuresText.isBlank()) return List.of();
        return Arrays.stream(featuresText.split("\\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /** Format VND: 30000 → "30.000đ" */
    private String formatVnd(BigDecimal amount) {
        if (amount == null) return null;
        NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        fmt.setMaximumFractionDigits(0);
        return fmt.format(amount) + "đ";
    }

    @Data
    public static class PublicPlanCard {
        private String subjectType;
        private String planName;
        private String iconUrl;
        /** Raw min fee value (BigDecimal) — for any client-side formatting. */
        private BigDecimal minFee;
        /** Pre-formatted: e.g. "30.000đ" */
        private String minFeeFormatted;
        private List<String> features;
    }
}
