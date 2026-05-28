package com.rs.subscription.service.impl;

import com.rs.subscription.service.IndividualUsageTrackingService;
import com.rs.subscription.dto.response.IndividualUsageRowResponse;
import com.rs.subscription.dto.response.IndividualUsageTrackingResponse;
import com.rs.subscription.enums.CommercialEnums;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IndividualUsageTrackingServiceImpl implements IndividualUsageTrackingService {

    @PersistenceContext
    private EntityManager em;

    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private static final String BASE_FROM = """
            FROM subscriptions s
            LEFT JOIN plan_pricing_rules ppr ON ppr.plan_pricing_rule_id = s.pricing_rule_id
            LEFT JOIN plan_templates pt ON pt.plan_template_id = s.plan_template_id
            WHERE s.subscriber_type = 'INDIVIDUAL'
            """;

    @Override
    @Transactional(readOnly = true)
    public IndividualUsageTrackingResponse getUsageTracking(
            String purchasedAt, String ctsType, String ctsDuration,
            String ctsStatus, String plan,
            int page, int size, String sortBy, String sortDir) {

        StringBuilder cond = new StringBuilder();
        Map<String, Object> params = new LinkedHashMap<>();

        if (purchasedAt != null && !purchasedAt.isBlank()) {
            // Range query so MySQL can use index on created_at
            LocalDate d = LocalDate.parse(purchasedAt);
            cond.append("AND s.created_at >= :purchasedAtStart AND s.created_at < :purchasedAtEnd\n");
            params.put("purchasedAtStart", d.atStartOfDay());
            params.put("purchasedAtEnd",   d.plusDays(1).atStartOfDay());
        }
        if (ctsType != null && !ctsType.isBlank()) {
            cond.append("AND COALESCE(ppr.subject_type, 'INDIVIDUAL') = :ctsType\n");
            params.put("ctsType", ctsType);
        }
        if (ctsDuration != null && !ctsDuration.isBlank()) {
            cond.append("AND ppr.certificate_validity_value = :ctsDuration\n");
            params.put("ctsDuration", Integer.parseInt(ctsDuration));
        }
        if (ctsStatus != null && !ctsStatus.isBlank()) {
            switch (ctsStatus) {
                case "ACTIVE"           -> cond.append("AND s.status = 'ACTIVE'\n");
                case "EXPIRED"          -> cond.append("AND s.status = 'EXPIRED'\n");
                case "REVOKED"          -> cond.append("AND s.status IN ('CANCELLED','SUSPENDED')\n");
                case "PENDING_ACTIVATE" -> cond.append("AND s.status NOT IN ('ACTIVE','EXPIRED','CANCELLED','SUSPENDED')\n");
            }
        }
        if (plan != null && !plan.isBlank()) {
            cond.append("AND pt.plan_name = :plan\n");
            params.put("plan", plan);
        }

        String orderBy  = buildOrderBy(sortBy, sortDir);
        String dataSql  = "SELECT s.subscription_id, s.user_id, s.created_at, s.status, s.signing_quota_used,"
                        + " COALESCE(ppr.subject_type,'INDIVIDUAL'), COALESCE(ppr.certificate_validity_value,12),"
                        + " COALESCE(ppr.unit_price,0), pt.plan_name "
                        + BASE_FROM + cond + orderBy;
        String countSql = "SELECT COUNT(*) " + BASE_FROM + cond;

        // Query 1: paginated data
        Query dataQuery  = em.createNativeQuery(dataSql);
        // Query 2: total count for pagination
        Query countQuery = em.createNativeQuery(countSql);
        params.forEach((k, v) -> { dataQuery.setParameter(k, v); countQuery.setParameter(k, v); });
        dataQuery.setFirstResult(page * size);
        dataQuery.setMaxResults(size);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQuery.getResultList();
        long totalElements = ((Number) countQuery.getSingleResult()).longValue();
        int  totalPages    = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;

        // Query 3: aggregate stats + lastUpdated — always on full dataset, independent of filters
        Object[] statsRow = loadStatsRow();
        IndividualUsageTrackingResponse.IndividualUsageStatsResponse stats =
                new IndividualUsageTrackingResponse.IndividualUsageStatsResponse();
        stats.setAccounts(toLong(statsRow[0]));
        stats.setPlansBought(toLong(statsRow[1]));
        stats.setSignings(toLong(statsRow[2]));
        stats.setCtsIndividual(toLong(statsRow[3]));
        stats.setCtsOrg(toLong(statsRow[4]));
        stats.setCtsIndividualOfOrg(toLong(statsRow[5]));

        String lastUpdated = toDatetimeString(statsRow[6]);

        IndividualUsageTrackingResponse response = new IndividualUsageTrackingResponse();
        response.setStats(stats);
        response.setList(rows.stream().map(this::fromRow).collect(Collectors.toList()));
        response.setLastUpdated(lastUpdated);
        response.setTotalElements(totalElements);
        response.setTotalPages(totalPages);
        response.setPage(page);
        response.setSize(size);
        return response;
    }

    /** Stats + lastUpdated trong 1 query, không bị ảnh hưởng bởi filter trang */
    private Object[] loadStatsRow() {
        Query q = em.createNativeQuery("""
                SELECT
                  COUNT(DISTINCT s.user_id),
                  COUNT(*),
                  COALESCE(SUM(s.signing_quota_used), 0),
                  SUM(CASE WHEN COALESCE(ppr.subject_type,'INDIVIDUAL') = 'INDIVIDUAL' THEN 1 ELSE 0 END),
                  SUM(CASE WHEN ppr.subject_type = 'ORGANIZATION' THEN 1 ELSE 0 END),
                  SUM(CASE WHEN ppr.subject_type = 'INDIVIDUAL_OF_ORG' THEN 1 ELSE 0 END),
                  MAX(s.updated_at)
                FROM subscriptions s
                LEFT JOIN plan_pricing_rules ppr ON ppr.plan_pricing_rule_id = s.pricing_rule_id
                WHERE s.subscriber_type = 'INDIVIDUAL'
                """);
        return (Object[]) q.getSingleResult();
    }

    private IndividualUsageRowResponse fromRow(Object[] r) {
        IndividualUsageRowResponse row = new IndividualUsageRowResponse();
        row.setId(toLong(r[0]));
        row.setAccount(r[1] != null ? String.valueOf(r[1]) : null);
        row.setPurchasedAt(toDatetimeString(r[2]));
        row.setCtsStatus(mapSubscriptionStatusToCtsStatus((String) r[3]));
        row.setSignings(toLong(r[4]));
        row.setCtsType((String) r[5]);
        row.setCtsDuration(r[6] != null ? ((Number) r[6]).intValue() : 12);
        row.setFee(toLong(r[7]));
        row.setPlan(r[8] != null ? (String) r[8] : null);
        return row;
    }

    private String buildOrderBy(String sortBy, String sortDir) {
        String dir = "asc".equalsIgnoreCase(sortDir) ? "ASC" : "DESC";
        return switch (sortBy != null ? sortBy : "") {
            case "ctsType"     -> " ORDER BY COALESCE(ppr.subject_type,'INDIVIDUAL') " + dir;
            case "ctsDuration" -> " ORDER BY COALESCE(ppr.certificate_validity_value,12) " + dir;
            case "account"     -> " ORDER BY s.user_id " + dir;
            case "ctsStatus"   -> " ORDER BY s.status " + dir;
            case "signings"    -> " ORDER BY s.signing_quota_used " + dir;
            case "plan"        -> " ORDER BY pt.plan_name " + dir;
            case "fee"         -> " ORDER BY COALESCE(ppr.unit_price,0) " + dir;
            default            -> " ORDER BY s.created_at " + dir;
        };
    }

    private String mapSubscriptionStatusToCtsStatus(String subStatus) {
        if (subStatus == null) return CommercialEnums.IndividualCtsStatus.PENDING_ACTIVATE.name();
        return switch (subStatus) {
            case "ACTIVE"                -> CommercialEnums.IndividualCtsStatus.ACTIVE.name();
            case "EXPIRED"               -> CommercialEnums.IndividualCtsStatus.EXPIRED.name();
            case "CANCELLED","SUSPENDED" -> CommercialEnums.IndividualCtsStatus.REVOKED.name();
            default                      -> CommercialEnums.IndividualCtsStatus.PENDING_ACTIVATE.name();
        };
    }

    /** Hibernate 5 trả Timestamp, Hibernate 6 trả LocalDateTime — xử lý cả hai */
    private String toDatetimeString(Object val) {
        if (val == null) return null;
        if (val instanceof java.sql.Timestamp ts) return ts.toLocalDateTime().format(DATETIME_FMT);
        if (val instanceof LocalDateTime ldt)    return ldt.format(DATETIME_FMT);
        return null;
    }

    private long toLong(Object val) {
        if (val == null) return 0L;
        if (val instanceof Number n) return n.longValue();
        return 0L;
    }
}
