package com.rs.subscription.controller;

import com.rs.subscription.dto.ApiResponse;
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
import java.time.LocalDate;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

    /**
     * Returns aggregated pricing per subject group for all ACTIVE/APPROVED plans.
     * Rows within the same (subjectType + pricingMetric + durationMonths) group are merged:
     *   - totalPrice = sum of all rows
     *   - rangeMin   = first row's rangeMin
     *   - rangeMax   = last row's rangeMax (null = unlimited)
     */
    @GetMapping("/plans/pricing")
    public ApiResponse<List<PricingRuleItem>> getPublicPlanPricing() {
        try {
            List<String> statuses = List.of(
                    CommercialEnums.ScheduleStatus.ACTIVE.name(),
                    CommercialEnums.ScheduleStatus.APPROVED.name());
            LocalDate today = LocalDate.now();

            List<RetailPlanSchedule> schedules = scheduleRepo.findCurrentlyApplicableWithPricingRules(statuses, today);

            List<PricingRuleItem> result = new ArrayList<>();

            for (RetailPlanSchedule schedule : schedules) {
                PlanTemplate template = schedule.getPlanTemplate();
                String planName = template.getPlanName();

                Map<String, List<PlanPricingRule>> grouped = template.getPricingRules().stream()
                        .filter(r -> Boolean.TRUE.equals(r.getIsActive()) && r.getTotalPrice() != null)
                        .collect(Collectors.groupingBy(
                                r -> r.getSubjectType() + "|" + r.getPricingMetric() + "|" + r.getCertificateValidityValue(),
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

                grouped.entrySet().stream()
                        .sorted(Comparator.comparingInt(e ->
                                SUBJECT_ORDER.indexOf(e.getValue().get(0).getSubjectType())))
                        .forEach(entry -> {
                            List<PlanPricingRule> rows = entry.getValue().stream()
                                    .sorted(Comparator.comparingInt(r -> (r.getRangeMin() != null ? r.getRangeMin() : 1)))
                                    .collect(Collectors.toList());

                            PlanPricingRule first = rows.get(0);
                            PlanPricingRule last  = rows.get(rows.size() - 1);

                            BigDecimal sumTotal = rows.stream()
                                    .map(PlanPricingRule::getTotalPrice)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                            Integer rangeMin = first.getRangeMin() != null ? first.getRangeMin() : 1;
                            Integer rangeMax = last.getRangeMax();

                            PricingRuleItem item = new PricingRuleItem();
                            item.setSubjectType(first.getSubjectType());
                            item.setSubjectLabel(subjectLabel(first.getSubjectType()));
                            item.setPlanName(planName);
                            item.setDurationValue(first.getCertificateValidityValue());
                            item.setDurationUnit(validityUnitLabel(first.getCertificateValidityUnit()));
                            item.setPricingMetric(first.getPricingMetric());
                            item.setPricingMetricLabel(metricLabel(first.getPricingMetric()));
                            item.setRangeMin(rangeMin);
                            item.setRangeMax(rangeMax);
                            item.setRangeLabel(rangeMax == null ? "Không giới hạn" : rangeMin + " – " + rangeMax);
                            item.setTotalPrice(sumTotal);
                            item.setTotalPriceFormatted(formatVnd(sumTotal));
                            result.add(item);
                        });
            }

            return ApiResponse.<List<PricingRuleItem>>builder()
                    .success(true)
                    .code("200")
                    .message("Success")
                    .data(result)
                    .build();
        } catch (Exception e) {
            return ApiResponse.<List<PricingRuleItem>>builder()
                    .success(false)
                    .message(e.getMessage())
                    .data(List.of())
                    .build();
        }
    }

    private String subjectLabel(String subjectType) {
        return switch (subjectType) {
            case "INDIVIDUAL"         -> "Cá nhân";
            case "ORGANIZATION"       -> "Tổ chức";
            case "INDIVIDUAL_OF_ORG"  -> "Cá nhân thuộc tổ chức";
            default                   -> subjectType;
        };
    }

    private String validityUnitLabel(String unit) {
        return switch (unit) {
            case "MONTH" -> "tháng";
            case "YEAR"  -> "năm";
            case "DAY"   -> "ngày";
            default      -> unit;
        };
    }

    private String metricLabel(String metric) {
        return switch (metric) {
            case "SIGNING_COUNT"     -> "Lượt ký số";
            case "CERTIFICATE_COUNT" -> "Số chứng thư";
            default                  -> metric;
        };
    }

    @GetMapping("/plans")
    public List<PublicPlanCard> getPublicPlans() {
        List<String> statuses = List.of(
                CommercialEnums.ScheduleStatus.ACTIVE.name(),
                CommercialEnums.ScheduleStatus.APPROVED.name());
        LocalDate today = LocalDate.now();

        Map<Long, RetailPlanSchedule> byId = scheduleRepo
                .findCurrentlyApplicableWithPricingRules(statuses, today)
                .stream()
                .collect(Collectors.toMap(
                        RetailPlanSchedule::getRetailPlanScheduleId, s -> s,
                        (a, b) -> a, LinkedHashMap::new));

        scheduleRepo.findCurrentlyApplicableWithSubjectConfigs(statuses, today)
                .forEach(s -> byId.merge(s.getRetailPlanScheduleId(), s, (existing, incoming) -> existing));

        List<RetailPlanSchedule> schedules = new ArrayList<>(byId.values());

        if (schedules.isEmpty()) return List.of();

        List<PublicPlanCard> cards = new ArrayList<>();
        for (RetailPlanSchedule schedule : schedules) {
            PlanTemplate template = schedule.getPlanTemplate();
            Long scheduleId = schedule.getRetailPlanScheduleId();

            // Min total price per subject type — from active pricing rules
            Map<String, BigDecimal> minFeeBySubject = template.getPricingRules().stream()
                    .filter(r -> Boolean.TRUE.equals(r.getIsActive()) && r.getTotalPrice() != null)
                    .collect(Collectors.groupingBy(
                            PlanPricingRule::getSubjectType,
                            Collectors.mapping(PlanPricingRule::getTotalPrice,
                                    Collectors.minBy(BigDecimal::compareTo))))
                    .entrySet().stream()
                    .filter(e -> e.getValue().isPresent())
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));

            // Subject configs indexed by subjectType
            Map<String, PlanSubjectConfig> configBySubject = template.getSubjectConfigs().stream()
                    .collect(Collectors.toMap(PlanSubjectConfig::getSubjectType, c -> c));

            SUBJECT_ORDER.stream()
                    .map(subjectType -> {
                        PlanSubjectConfig config = configBySubject.get(subjectType);
                        BigDecimal minFee = minFeeBySubject.get(subjectType);
                        if (config == null && minFee == null) return null;

                        PublicPlanCard card = new PublicPlanCard();
                        card.setScheduleId(scheduleId);
                        card.setSubjectType(subjectType);
                        card.setPlanName(template.getPlanName());
                        card.setIconUrl(config != null ? minioStorageService.toPublicUrl(config.getIconUrl()) : null);
                        card.setMinFee(minFee);
                        card.setMinFeeFormatted(minFee != null ? formatVnd(minFee) : null);
                        card.setFeatures(config != null ? parseFeatures(config.getFeaturesText()) : List.of());
                        return card;
                    })
                    .filter(c -> c != null)
                    .forEach(cards::add);
        }
        return cards;
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
    public static class PricingRuleItem {
        private String subjectType;
        private String subjectLabel;
        private String planName;
        private Integer durationValue;
        private String durationUnit;
        private String pricingMetric;
        private String pricingMetricLabel;
        private Integer rangeMin;
        private Integer rangeMax;
        private String rangeLabel;
        private BigDecimal totalPrice;
        private String totalPriceFormatted;
    }

    @Data
    public static class PublicPlanCard {
        private Long scheduleId;
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
