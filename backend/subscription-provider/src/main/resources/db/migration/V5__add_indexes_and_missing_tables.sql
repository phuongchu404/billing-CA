-- ============================================================
-- V5: Indexes còn thiếu + bảng certificate_usage_daily
--
-- Mục tiêu:
--   1. Tối ưu các query thường dùng trong dashboard reports
--   2. Tối ưu expiring-soon query trong scheduler
--   3. Tạo bảng certificate_usage_daily (đã được reference trong
--      CertificateUsageRecordRepository nhưng chưa có trong schema)
--   4. Tạo bảng usage_aggregate_rollup_log để theo dõi scheduler
-- ============================================================

-- ── subscriptions ────────────────────────────────────────────
-- Cho countActiveIndividualCustomers() và native weekly queries
-- WHERE subscriber_type = 'INDIVIDUAL' AND status = 'ACTIVE'
ALTER TABLE subscriptions
    ADD INDEX idx_subscriptions_type_status (subscriber_type, status);

-- Cho countWeeklyNewCustomers(): WHERE subscriber_type = 'INDIVIDUAL' AND created_at BETWEEN ...
ALTER TABLE subscriptions
    ADD INDEX idx_subscriptions_type_created (subscriber_type, created_at);

-- ── certificate_provisioning_records ────────────────────────
-- Cho countCompletedByCertTypeGroupedByAssignment() (batch GROUP report):
-- WHERE group_plan_assignment_id IN (...) AND status = 'COMPLETED'
--   AND issued_at >= :from AND issued_at < :to
-- GROUP BY group_plan_assignment_id, cert_type
ALTER TABLE certificate_provisioning_records
    ADD INDEX idx_cpr_assignment_status_issued (group_plan_assignment_id, status, issued_at);

-- Cho countWeeklyCertsByTypeForIndividual() (native, JOIN subscriptions):
-- WHERE c.status = 'COMPLETED' AND c.issued_at >= :from AND c.issued_at < :to
ALTER TABLE certificate_provisioning_records
    ADD INDEX idx_cpr_issued_status (issued_at, status);

-- ── certificate_usage_records ────────────────────────────────
-- Cho countWeeklySigningsByTypeForIndividual() (native):
-- WHERE cur.usage_type = 'SIGNING' AND cur.used_at >= :from AND cur.used_at < :to
ALTER TABLE certificate_usage_records
    ADD INDEX idx_usage_type_used_at (usage_type, used_at);

-- ── group_plan_assignments ───────────────────────────────────
-- Cho findExpiringSoonWithNoSuccessor():
-- WHERE assignment_status = 'ACTIVE' AND apply_to BETWEEN :today AND :threshold
-- Index hiện tại (apply_from, apply_to) không giúp được vì filter chính là status
ALTER TABLE group_plan_assignments
    ADD INDEX idx_gpa_status_apply_to (assignment_status, apply_to);

-- ── certificate_usage_daily (bảng thiếu trong schema gốc) ───
-- Được reference trong CertificateUsageRecordRepository.upsertDailyRollup()
-- Lưu rollup theo ngày để tránh scan toàn bảng usage_records khi cần thống kê
CREATE TABLE certificate_usage_daily (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    certificate_id   VARCHAR(200)  NOT NULL,
    usage_date       DATE          NOT NULL,
    usage_count      INT           NOT NULL DEFAULT 0,
    distinct_users   INT           NOT NULL DEFAULT 0,
    created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cert_daily (certificate_id, usage_date),
    KEY idx_cert_daily_date (usage_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── usage_aggregate_rollup_log ───────────────────────────────
-- Theo dõi lịch sử chạy scheduler rollup để debug và audit
CREATE TABLE usage_aggregate_rollup_log (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    period_key   VARCHAR(20)  NOT NULL,
    run_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    groups_updated INT        NOT NULL DEFAULT 0,
    status       VARCHAR(20)  NOT NULL DEFAULT 'SUCCESS',
    error_msg    TEXT         NULL,
    PRIMARY KEY (id),
    KEY idx_rollup_log_period (period_key),
    KEY idx_rollup_log_run_at (run_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
