-- V26: Align certificate_usage_records collation with the rest of the schema.
-- All original tables use utf8mb4_unicode_ci; V24 omitted COLLATE so MySQL 8
-- defaulted to utf8mb4_0900_ai_ci, causing "Illegal mix of collations" when the
-- certificate_id column is compared in a subquery against certificate_provisioning_records.

ALTER TABLE certificate_usage_records
    MODIFY certificate_id VARCHAR(200) NOT NULL COLLATE utf8mb4_unicode_ci,
    MODIFY user_id        VARCHAR(100) NOT NULL COLLATE utf8mb4_unicode_ci;
