-- V25: Daily rollup table for fast usage reporting without scanning billions of raw events.
-- uk_cert_date enables idempotent upserts (INSERT ... ON DUPLICATE KEY UPDATE) in the scheduler.

CREATE TABLE certificate_usage_daily (
    id             BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    certificate_id VARCHAR(200) NOT NULL,
    usage_date     DATE         NOT NULL,
    usage_count    INT          NOT NULL DEFAULT 0,
    distinct_users INT          NOT NULL DEFAULT 0,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_cert_date (certificate_id, usage_date),
    INDEX idx_daily_date (usage_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
