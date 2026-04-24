-- V24: Certificate usage event tracking with monthly RANGE partitioning
-- Composite PK (id, used_at) is required by MySQL for partitioned tables with AUTO_INCREMENT.
-- The p_future catch-all prevents INSERT failures when no matching partition exists yet.

CREATE TABLE certificate_usage_records (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    certificate_id VARCHAR(200) NOT NULL,
    user_id        VARCHAR(100) NOT NULL,
    used_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id, used_at),
    INDEX idx_usage_cert_used  (certificate_id, used_at DESC),
    INDEX idx_usage_user_used  (user_id, used_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
PARTITION BY RANGE COLUMNS (used_at) (
    PARTITION p202604 VALUES LESS THAN ('2026-05-01 00:00:00'),
    PARTITION p202605 VALUES LESS THAN ('2026-06-01 00:00:00'),
    PARTITION p202606 VALUES LESS THAN ('2026-07-01 00:00:00'),
    PARTITION p202607 VALUES LESS THAN ('2026-08-01 00:00:00'),
    PARTITION p202608 VALUES LESS THAN ('2026-09-01 00:00:00'),
    PARTITION p202609 VALUES LESS THAN ('2026-10-01 00:00:00'),
    PARTITION p202610 VALUES LESS THAN ('2026-11-01 00:00:00'),
    PARTITION p202611 VALUES LESS THAN ('2026-12-01 00:00:00'),
    PARTITION p202612 VALUES LESS THAN ('2027-01-01 00:00:00'),
    PARTITION p202701 VALUES LESS THAN ('2027-02-01 00:00:00'),
    PARTITION p202702 VALUES LESS THAN ('2027-03-01 00:00:00'),
    PARTITION p202703 VALUES LESS THAN ('2027-04-01 00:00:00'),
    PARTITION p202704 VALUES LESS THAN ('2027-05-01 00:00:00'),
    PARTITION p_future VALUES LESS THAN (MAXVALUE)
);
