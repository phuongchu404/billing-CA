package com.rs.subscription.service.impl;

import com.rs.subscription.service.*;

import com.rs.subscription.entity.UsageAggregate;
import com.rs.subscription.entity.UsageAggregateRollupLog;
import com.rs.subscription.repository.UsageAggregateRepository;
import com.rs.subscription.repository.UsageAggregateRollupLogRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rollup nightly: tổng hợp cert và signing counts từ raw tables → usage_aggregates.
 *
 * Lý do cần scheduler này:
 *   - usage_aggregates chỉ được cập nhật khi tạo settlement (CommercialOrchestrationService).
 *   - Dashboard report đọc từ usage_aggregates → nếu không có rollup, dữ liệu sẽ cũ.
 *   - Scheduler này chạy mỗi đêm, tính lại aggregate cho tháng hiện tại từ raw events.
 *
 * Thiết kế:
 *   - 2 native queries batch (không có N+1): cert counts per group, signing counts per group.
 *   - Sau khi cập nhật DB, evict Caffeine cache để request tiếp theo lấy dữ liệu mới.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UsageAggregateSchedulerServiceImpl implements UsageAggregateSchedulerService {

    private static final DateTimeFormatter PERIOD_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    @PersistenceContext
    private EntityManager em;

    private final UsageAggregateRepository usageAggregateRepository;
    private final UsageAggregateRollupLogRepository rollupLogRepository;
    private final CacheManager cacheManager;

    /**
     * Chạy lúc 01:00 AM mỗi ngày.
     * Tổng hợp usage_aggregates cho tháng hiện tại từ raw provisioning + usage records.
     */
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void rollupCurrentMonth() {
        String periodKey = LocalDate.now().format(PERIOD_FMT);
        log.info("[UsageRollup] Bắt đầu rollup tháng {} lúc {}", periodKey, LocalDateTime.now());

        int groupsUpdated = 0;
        String errorMsg = null;

        try {
            groupsUpdated = doRollup(periodKey);
            evictReportCache(periodKey);
            log.info("[UsageRollup] Hoàn tất: cập nhật {} group(s) cho tháng {}", groupsUpdated, periodKey);
        } catch (Exception e) {
            errorMsg = e.getMessage();
            log.error("[UsageRollup] Lỗi rollup tháng {}: {}", periodKey, e.getMessage(), e);
        }

        saveLog(periodKey, groupsUpdated, errorMsg);
    }

    /**
     * Gọi thủ công khi cần re-rollup một tháng cụ thể (dùng qua controller admin nếu cần).
     */
    @Transactional
    public int rollupPeriod(String periodKey) {
        log.info("[UsageRollup] Re-rollup thủ công tháng {}", periodKey);
        int updated = doRollup(periodKey);
        evictReportCache(periodKey);
        saveLog(periodKey, updated, null);
        return updated;
    }

    // ── core logic ───────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private int doRollup(String periodKey) {
        LocalDate monthStart = LocalDate.parse(periodKey + "-01");
        LocalDateTime fromDt = monthStart.atStartOfDay();
        LocalDateTime toDt   = monthStart.plusMonths(1).atStartOfDay();

        // ── Batch query 1: cert counts per group ──────────────────────────
        // JOIN qua group_plan_assignments để map cpr → group
        List<Object[]> certRows = em.createNativeQuery("""
                SELECT gpa.group_id, COUNT(cpr.id)
                FROM certificate_provisioning_records cpr
                JOIN group_plan_assignments gpa
                    ON gpa.group_plan_assignment_id = cpr.group_plan_assignment_id
                WHERE cpr.status = 'COMPLETED'
                  AND cpr.issued_at >= :from AND cpr.issued_at < :to
                GROUP BY gpa.group_id
                """)
                .setParameter("from", fromDt)
                .setParameter("to", toDt)
                .getResultList();

        Map<Long, Integer> certByGroup = new HashMap<>();
        for (Object[] row : certRows) {
            certByGroup.put(((Number) row[0]).longValue(), ((Number) row[1]).intValue());
        }

        // ── Batch query 2: signing counts per group ───────────────────────
        List<Object[]> signRows = em.createNativeQuery("""
                SELECT gpa.group_id, COUNT(cur.id)
                FROM certificate_usage_records cur
                JOIN group_plan_assignments gpa
                    ON gpa.group_plan_assignment_id = cur.group_plan_assignment_id
                WHERE cur.usage_type = 'SIGNING'
                  AND cur.used_at >= :from AND cur.used_at < :to
                GROUP BY gpa.group_id
                """)
                .setParameter("from", fromDt)
                .setParameter("to", toDt)
                .getResultList();

        Map<Long, Integer> signByGroup = new HashMap<>();
        for (Object[] row : signRows) {
            signByGroup.put(((Number) row[0]).longValue(), ((Number) row[1]).intValue());
        }

        // ── Upsert aggregates (chỉ các group có dữ liệu) ─────────────────
        // Merge cả 2 map để không bỏ sót group có cert nhưng không có signing (hoặc ngược lại)
        Map<Long, boolean[]> allGroupIds = new HashMap<>();
        certByGroup.keySet().forEach(id -> allGroupIds.computeIfAbsent(id, k -> new boolean[1]));
        signByGroup.keySet().forEach(id -> allGroupIds.computeIfAbsent(id, k -> new boolean[1]));

        int updated = 0;
        for (Long groupId : allGroupIds.keySet()) {
            int certs   = certByGroup.getOrDefault(groupId, 0);
            int signings = signByGroup.getOrDefault(groupId, 0);

            UsageAggregate agg = usageAggregateRepository
                    .findByAggregateScopeAndScopeIdAndPeriodTypeAndPeriodKey(
                            "GROUP", groupId, "MONTH", periodKey)
                    .orElseGet(UsageAggregate::new);

            agg.setAggregateScope("GROUP");
            agg.setScopeId(groupId);
            agg.setPeriodType("MONTH");
            agg.setPeriodKey(periodKey);
            agg.setCertificatesCreated(certs);
            agg.setSigningUsed(signings);
            agg.setActiveCertificates(certs);

            usageAggregateRepository.save(agg);
            updated++;
        }

        return updated;
    }

    // ── Evict Caffeine cache sau khi rollup ──────────────────────────────────
    private void evictReportCache(String periodKey) {
        var cache = cacheManager.getCache("groupReport");
        if (cache != null) {
            cache.evict(periodKey);
            log.debug("[UsageRollup] Evicted groupReport cache key={}", periodKey);
        }
    }

    private void saveLog(String periodKey, int groupsUpdated, String errorMsg) {
        UsageAggregateRollupLog log = UsageAggregateRollupLog.builder()
                .periodKey(periodKey)
                .runAt(LocalDateTime.now())
                .groupsUpdated(groupsUpdated)
                .status(errorMsg == null ? "SUCCESS" : "ERROR")
                .errorMsg(errorMsg)
                .build();
        rollupLogRepository.save(log);
    }
}
