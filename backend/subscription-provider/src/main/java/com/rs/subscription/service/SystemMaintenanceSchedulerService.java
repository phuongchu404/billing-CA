package com.rs.subscription.service;

import com.rs.subscription.repository.CertificateUsageRecordRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;

/**
 * Bảo trì hệ thống định kỳ:
 *   1. Xoá refresh tokens đã hết hạn (hàng ngày 02:00)
 *   2. Rollup certificate_usage_daily (hàng ngày 01:30)
 *   3. Thêm partition năm mới cho 3 bảng volume cao (01/01 hàng năm 00:30)
 *   4. Xoá partition cũ hơn 3 năm (01/01 hàng năm 00:45)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemMaintenanceSchedulerService {

    // Giữ tối đa bao nhiêu năm dữ liệu raw trong partition
    private static final int PARTITION_RETENTION_YEARS = 3;

    private static final String[] PARTITIONED_TABLES = {
        "certificate_usage_records",
        "admin_audit_logs",
        "subscription_audit_logs"
    };

    @PersistenceContext
    private EntityManager em;

    private final CertificateUsageRecordRepository certUsageRepository;

    // ── Hàng ngày ────────────────────────────────────────────────────────────

    /**
     * Xoá refresh tokens hết hạn quá 30 ngày.
     * Chạy 02:00 AM mỗi ngày.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupExpiredRefreshTokens() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        int deleted = em.createQuery(
                "DELETE FROM RefreshToken t WHERE t.expiresAt < :threshold")
                .setParameter("threshold", threshold)
                .executeUpdate();

        if (deleted > 0) {
            log.info("[Maintenance] Đã xoá {} refresh token(s) hết hạn trước {}", deleted, threshold.toLocalDate());
        }
    }

    /**
     * Rollup certificate_usage_daily cho ngày hôm qua.
     * Chạy 01:30 AM mỗi ngày.
     */
    @Scheduled(cron = "0 30 1 * * *")
    @Transactional
    public void rollupDailyUsage() {
        LocalDateTime fromDt = LocalDateTime.now().minusDays(1).toLocalDate().atStartOfDay();
        LocalDateTime toDt   = fromDt.plusDays(1);
        try {
            certUsageRepository.upsertDailyRollup(fromDt, toDt);
            log.info("[Maintenance] Daily rollup hoàn tất cho ngày {}", fromDt.toLocalDate());
        } catch (Exception e) {
            log.error("[Maintenance] Daily rollup thất bại: {}", e.getMessage(), e);
        }
    }

    // ── Hàng năm (01/01) ─────────────────────────────────────────────────────

    /**
     * Thêm partition cho năm mới vào 3 bảng volume cao.
     * Chạy 00:30 AM ngày 01/01 hàng năm.
     *
     * Cơ chế: REORGANIZE PARTITION p_future
     *   - Tách p_future (MAXVALUE) thành partition năm hiện tại + p_future mới
     *   - Không mất dữ liệu, không lock lâu (MySQL tối ưu cho REORGANIZE PARTITION)
     *
     * Ví dụ khi chạy vào 01/01/2027:
     *   p_future (< MAXVALUE) → p2027 (< 2028) + p_future (< MAXVALUE)
     */
    @Scheduled(cron = "0 30 0 1 1 *")
    public void addYearlyPartition() {
        int newYear    = Year.now().getValue();
        int nextYear   = newYear + 1;
        String partName = "p" + newYear;

        for (String table : PARTITIONED_TABLES) {
            try {
                // REORGANIZE tách p_future → p<năm_mới> + p_future
                String sql = String.format(
                    "ALTER TABLE %s REORGANIZE PARTITION p_future INTO (" +
                    "  PARTITION %s VALUES LESS THAN (%d)," +
                    "  PARTITION p_future VALUES LESS THAN MAXVALUE" +
                    ")",
                    table, partName, nextYear
                );
                em.createNativeQuery(sql).executeUpdate();
                log.info("[Maintenance] Đã thêm partition {} cho bảng {}", partName, table);
            } catch (Exception e) {
                // Có thể partition đã tồn tại nếu job chạy lại — log và bỏ qua
                log.warn("[Maintenance] Không thể thêm partition {} cho {}: {}", partName, table, e.getMessage());
            }
        }
    }

    /**
     * Xoá partition cũ hơn PARTITION_RETENTION_YEARS (mặc định 3 năm).
     * Chạy 00:45 AM ngày 01/01 hàng năm (sau addYearlyPartition).
     *
     * Lợi ích so với DELETE:
     *   - DROP PARTITION = O(1): xoá nguyên file partition tức thì
     *   - Không tạo undo log, không lock bảng lâu
     *   - Không làm đầy transaction log
     */
    @Scheduled(cron = "0 45 0 1 1 *")
    public void dropOldPartitions() {
        int cutoffYear = Year.now().getValue() - PARTITION_RETENTION_YEARS;

        for (String table : PARTITIONED_TABLES) {
            // Kiểm tra partition có tồn tại không trước khi drop
            String checkSql = """
                SELECT COUNT(*) FROM information_schema.PARTITIONS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = :table
                  AND PARTITION_NAME = :pname
                """;

            String partName = "p" + cutoffYear;
            Object count = em.createNativeQuery(checkSql)
                    .setParameter("table", table)
                    .setParameter("pname", partName)
                    .getSingleResult();

            if (((Number) count).intValue() == 0) {
                log.debug("[Maintenance] Partition {} không tồn tại trên bảng {}, bỏ qua", partName, table);
                continue;
            }

            try {
                em.createNativeQuery(
                    "ALTER TABLE " + table + " DROP PARTITION " + partName
                ).executeUpdate();
                log.info("[Maintenance] Đã xoá partition {} (năm {}) khỏi bảng {}", partName, cutoffYear, table);
            } catch (Exception e) {
                log.error("[Maintenance] Không thể xoá partition {} trên {}: {}", partName, table, e.getMessage());
            }
        }
    }
}
