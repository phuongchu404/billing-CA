-- ============================================================
-- V9: Partition additional high-volume event tables
--
-- Run after V6__partition_high_volume_tables.sql.
--
-- Additional partitioned tables:
--   1. certificate_provisioning_records  - certificate issuance/bind raw events
--   2. document_upload_records           - document upload raw events
--   3. certificate_auth_failure_records  - signing/auth failure raw events
--
-- MySQL InnoDB partition limitations:
--   - A partitioned InnoDB table cannot have foreign keys.
--   - Every UNIQUE KEY must include the partition expression column.
--
-- For certificate_provisioning_records, existing global unique constraints
-- cannot be kept together with time-based partitioning. They are replaced by
-- non-unique lookup indexes, and uniqueness must be enforced by application
-- validation before insert/update.
-- ============================================================

-- ============================================================
-- 1. certificate_provisioning_records
--    Use created_at because it is NOT NULL for every row and follows ingestion time.
-- ============================================================

SET @drop_fk := (
    SELECT IF(COUNT(*) > 0,
        'ALTER TABLE certificate_provisioning_records DROP FOREIGN KEY fk_cpr_subscription',
        'DO 0')
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'certificate_provisioning_records'
      AND CONSTRAINT_NAME = 'fk_cpr_subscription'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
PREPARE stmt FROM @drop_fk;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @drop_fk := (
    SELECT IF(COUNT(*) > 0,
        'ALTER TABLE certificate_provisioning_records DROP FOREIGN KEY fk_cpr_assignment',
        'DO 0')
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'certificate_provisioning_records'
      AND CONSTRAINT_NAME = 'fk_cpr_assignment'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
PREPARE stmt FROM @drop_fk;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @drop_fk := (
    SELECT IF(COUNT(*) > 0,
        'ALTER TABLE certificate_provisioning_records DROP FOREIGN KEY fk_cpr_pricing_rule',
        'DO 0')
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'certificate_provisioning_records'
      AND CONSTRAINT_NAME = 'fk_cpr_pricing_rule'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
PREPARE stmt FROM @drop_fk;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE certificate_provisioning_records
    DROP INDEX uk_cpr_request_id,
    DROP INDEX uk_cpr_cert_user,
    DROP INDEX uk_cpr_certificate,
    ADD KEY idx_cpr_request_id_lookup (request_id),
    ADD KEY idx_cpr_cert_user_lookup (subscription_id, user_id),
    ADD KEY idx_cpr_certificate_lookup (certificate_id);

ALTER TABLE certificate_provisioning_records
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (id, created_at);

ALTER TABLE certificate_provisioning_records
    PARTITION BY RANGE (YEAR(created_at)) (
        PARTITION p2024 VALUES LESS THAN (2025),
        PARTITION p2025 VALUES LESS THAN (2026),
        PARTITION p2026 VALUES LESS THAN (2027),
        PARTITION p_future VALUES LESS THAN MAXVALUE
    );

-- ============================================================
-- 2. document_upload_records
-- ============================================================

SET @drop_fk := (
    SELECT IF(COUNT(*) > 0,
        'ALTER TABLE document_upload_records DROP FOREIGN KEY fk_dur_subscription',
        'DO 0')
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'document_upload_records'
      AND CONSTRAINT_NAME = 'fk_dur_subscription'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
PREPARE stmt FROM @drop_fk;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE document_upload_records
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (id, uploaded_at);

ALTER TABLE document_upload_records
    PARTITION BY RANGE (YEAR(uploaded_at)) (
        PARTITION p2024 VALUES LESS THAN (2025),
        PARTITION p2025 VALUES LESS THAN (2026),
        PARTITION p2026 VALUES LESS THAN (2027),
        PARTITION p_future VALUES LESS THAN MAXVALUE
    );

-- ============================================================
-- 3. certificate_auth_failure_records
-- ============================================================

SET @drop_fk := (
    SELECT IF(COUNT(*) > 0,
        'ALTER TABLE certificate_auth_failure_records DROP FOREIGN KEY fk_cafr_subscription',
        'DO 0')
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'certificate_auth_failure_records'
      AND CONSTRAINT_NAME = 'fk_cafr_subscription'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
PREPARE stmt FROM @drop_fk;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE certificate_auth_failure_records
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (id, failed_at);

ALTER TABLE certificate_auth_failure_records
    PARTITION BY RANGE (YEAR(failed_at)) (
        PARTITION p2024 VALUES LESS THAN (2025),
        PARTITION p2025 VALUES LESS THAN (2026),
        PARTITION p2026 VALUES LESS THAN (2027),
        PARTITION p_future VALUES LESS THAN MAXVALUE
    );
