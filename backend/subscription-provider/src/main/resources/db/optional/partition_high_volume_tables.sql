-- ============================================================
-- Optional: Partition high-volume tables
--
-- Báº£ng Ä‘Æ°á»£c partition:
--   1. certificate_usage_records  â€” 1 row má»—i láº§n kÃ½, volume cao nháº¥t
--   2. admin_audit_logs           â€” 1 row má»—i hÃ nh Ä‘á»™ng admin
--   3. subscription_audit_logs    â€” 1 row má»—i thay Ä‘á»•i subscription
--
-- Táº¡i sao PHáº¢I drop FK trÆ°á»›c:
--   MySQL InnoDB khÃ´ng cho phÃ©p PARTITION BY trÃªn báº£ng cÃ³ foreign key
--   (cáº£ FK Ä‘i ra láº«n FK Ä‘Æ°á»£c tham chiáº¿u). Drop FK á»Ÿ Ä‘Ã¢y lÃ  an toÃ n vÃ¬:
--   - ToÃ n váº¹n dá»¯ liá»‡u váº«n Ä‘áº£m báº£o bá»Ÿi application layer (service + repo)
--   - JOIN váº«n hoáº¡t Ä‘á»™ng bÃ¬nh thÆ°á»ng qua JPA relationship / native query
--
-- MySQL yÃªu cáº§u cá»™t partition pháº£i náº±m trong PRIMARY KEY.
-- Hiá»‡n táº¡i PK = (id), cáº§n Ä‘á»•i thÃ nh (id, <date_col>) composite PK.
-- AUTO_INCREMENT váº«n hoáº¡t Ä‘á»™ng vá»›i composite PK khi id lÃ  cá»™t Ä‘áº§u.
--
-- Chiáº¿n lÆ°á»£c partition:
--   RANGE by YEAR â€” má»—i partition = 1 nÄƒm
--   Partition p_future chá»©a táº¥t cáº£ dá»¯ liá»‡u chÆ°a Ä‘Æ°á»£c chia rÃµ
--   Scheduler tá»± Ä‘á»™ng reorganize p_future vÃ o Ä‘áº§u má»—i nÄƒm
--
-- Lá»£i Ã­ch khi cÃ³ partition:
--   - Query vá»›i filter theo thá»i gian: MySQL chá»‰ scan Ä‘Ãºng partition (partition pruning)
--   - XoÃ¡ dá»¯ liá»‡u cÅ©: DROP PARTITION tá»©c thÃ¬ O(1) thay vÃ¬ DELETE O(n)
--   - Backup tá»«ng nÄƒm riÃªng biá»‡t dá»… dÃ ng hÆ¡n
-- ============================================================

-- ============================================================
-- 1. certificate_usage_records
--    Volume: má»—i láº§n kÃ½ = 1 row â†’ hÃ ng triá»‡u rows/nÄƒm
-- ============================================================

-- BÆ°á»›c 1a: Drop FKs (FK lÃ  lÃ½ do duy nháº¥t blocking partition)
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

-- BÆ°á»›c 1b: Äá»•i PK tá»« (id) â†’ (id, used_at)
--           MySQL báº¯t buá»™c cá»™t partition pháº£i cÃ³ trong má»i unique index + PK
ALTER TABLE certificate_usage_records
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (id, used_at);

-- BÆ°á»›c 1c: Partition by RANGE(YEAR(used_at))
ALTER TABLE certificate_usage_records
    PARTITION BY RANGE (YEAR(used_at)) (
        PARTITION p2024 VALUES LESS THAN (2025),
        PARTITION p2025 VALUES LESS THAN (2026),
        PARTITION p2026 VALUES LESS THAN (2027),
        PARTITION p_future VALUES LESS THAN MAXVALUE
    );

-- ============================================================
-- 2. admin_audit_logs
--    Volume: má»—i action admin = 1 row
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
--    Volume: má»—i thay Ä‘á»•i tráº¡ng thÃ¡i subscription = 1 row
-- ============================================================

-- Drop FK trÆ°á»›c
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
