-- ============================================================
-- V6: Partition các bảng có volume cao
--
-- Bảng được partition:
--   1. certificate_usage_records  — 1 row mỗi lần ký, volume cao nhất
--   2. admin_audit_logs           — 1 row mỗi hành động admin
--   3. subscription_audit_logs    — 1 row mỗi thay đổi subscription
--
-- Tại sao PHẢI drop FK trước:
--   MySQL InnoDB không cho phép PARTITION BY trên bảng có foreign key
--   (cả FK đi ra lẫn FK được tham chiếu). Drop FK ở đây là an toàn vì:
--   - Toàn vẹn dữ liệu vẫn đảm bảo bởi application layer (service + repo)
--   - JOIN vẫn hoạt động bình thường qua JPA relationship / native query
--
-- MySQL yêu cầu cột partition phải nằm trong PRIMARY KEY.
-- Hiện tại PK = (id), cần đổi thành (id, <date_col>) composite PK.
-- AUTO_INCREMENT vẫn hoạt động với composite PK khi id là cột đầu.
--
-- Chiến lược partition:
--   RANGE by YEAR — mỗi partition = 1 năm
--   Partition p_future chứa tất cả dữ liệu chưa được chia rõ
--   Scheduler tự động reorganize p_future vào đầu mỗi năm
--
-- Lợi ích khi có partition:
--   - Query với filter theo thời gian: MySQL chỉ scan đúng partition (partition pruning)
--   - Xoá dữ liệu cũ: DROP PARTITION tức thì O(1) thay vì DELETE O(n)
--   - Backup từng năm riêng biệt dễ dàng hơn
-- ============================================================

-- ============================================================
-- 1. certificate_usage_records
--    Volume: mỗi lần ký = 1 row → hàng triệu rows/năm
-- ============================================================

-- Bước 1a: Drop FKs (FK là lý do duy nhất blocking partition)
SET @drop_fk := (
    SELECT IF(COUNT(*) > 0,
        'ALTER TABLE certificate_usage_records DROP FOREIGN KEY fk_certificate_usage_subscription',
        'DO 0')
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'certificate_usage_records'
      AND CONSTRAINT_NAME = 'fk_certificate_usage_subscription'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
PREPARE stmt FROM @drop_fk;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @drop_fk := (
    SELECT IF(COUNT(*) > 0,
        'ALTER TABLE certificate_usage_records DROP FOREIGN KEY fk_certificate_usage_group_assignment',
        'DO 0')
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'certificate_usage_records'
      AND CONSTRAINT_NAME = 'fk_certificate_usage_group_assignment'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
PREPARE stmt FROM @drop_fk;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Bước 1b: Đổi PK từ (id) → (id, used_at)
--           MySQL bắt buộc cột partition phải có trong mọi unique index + PK
ALTER TABLE certificate_usage_records
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (id, used_at);

-- Bước 1c: Partition by RANGE(YEAR(used_at))
ALTER TABLE certificate_usage_records
    PARTITION BY RANGE (YEAR(used_at)) (
        PARTITION p2024 VALUES LESS THAN (2025),
        PARTITION p2025 VALUES LESS THAN (2026),
        PARTITION p2026 VALUES LESS THAN (2027),
        PARTITION p_future VALUES LESS THAN MAXVALUE
    );

-- ============================================================
-- 2. admin_audit_logs
--    Volume: mỗi action admin = 1 row
-- ============================================================

ALTER TABLE admin_audit_logs
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (id, created_at);

ALTER TABLE admin_audit_logs
    PARTITION BY RANGE (YEAR(created_at)) (
        PARTITION p2024 VALUES LESS THAN (2025),
        PARTITION p2025 VALUES LESS THAN (2026),
        PARTITION p2026 VALUES LESS THAN (2027),
        PARTITION p_future VALUES LESS THAN MAXVALUE
    );

-- ============================================================
-- 3. subscription_audit_logs
--    Volume: mỗi thay đổi trạng thái subscription = 1 row
-- ============================================================

-- Drop FK trước
SET @drop_fk := (
    SELECT IF(COUNT(*) > 0,
        'ALTER TABLE subscription_audit_logs DROP FOREIGN KEY fk_subscription_audit_logs_subscription',
        'DO 0')
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'subscription_audit_logs'
      AND CONSTRAINT_NAME = 'fk_subscription_audit_logs_subscription'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
PREPARE stmt FROM @drop_fk;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE subscription_audit_logs
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (id, created_at);

ALTER TABLE subscription_audit_logs
    PARTITION BY RANGE (YEAR(created_at)) (
        PARTITION p2024 VALUES LESS THAN (2025),
        PARTITION p2025 VALUES LESS THAN (2026),
        PARTITION p2026 VALUES LESS THAN (2027),
        PARTITION p_future VALUES LESS THAN MAXVALUE
    );
