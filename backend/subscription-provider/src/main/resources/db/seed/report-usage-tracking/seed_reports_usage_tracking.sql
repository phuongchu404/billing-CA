-- Seed data for:
-- - GET /api/v1/reports/group
-- - GET /api/v1/reports/individual
-- - GET /api/v1/individual/usage-tracking
--
-- Target DB: MySQL 8 / MariaDB-compatible schema from backend/subscription-provider Flyway migrations.
-- The script uses the 900000+ ID range and can be re-run safely.

SET NAMES utf8mb4;
SET @seed_base := 900000;
SET @period_key := '2026-03';
SET @prev_period_key := '2026-02';

CREATE TEMPORARY TABLE IF NOT EXISTS _seed_seq_15 (
                                                      n INT NOT NULL PRIMARY KEY
);

DELETE FROM _seed_seq_15 where true;
INSERT INTO _seed_seq_15 (n) VALUES
                                 (1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(11),(12),(13),(14),(15);

-- 1. Users used by data scope, group ownership, partner access, and individual subscriptions.
INSERT INTO user_accounts (
    user_id, username, email, full_name, password_hash, auth_provider, status,
    failed_login_attempts, created_by, manager_user_id, created_at, updated_at
)
SELECT
    @seed_base + n,
    CONCAT('seed_report_user_', LPAD(n, 2, '0')),
    CONCAT('seed_report_user_', LPAD(n, 2, '0'), '@example.test'),
    CONCAT('Seed Report User ', LPAD(n, 2, '0')),
    '$2a$10$seeded.hash.for.report.usage.tracking',
    'LOCAL',
    'ACTIVE',
    0,
    'seed_sql',
    NULL,
    TIMESTAMP('2026-02-01 08:00:00') + INTERVAL n DAY,
    TIMESTAMP('2026-03-28 17:00:00') + INTERVAL n HOUR
FROM _seed_seq_15
ON DUPLICATE KEY UPDATE
                     email = VALUES(email),
                     full_name = VALUES(full_name),
                     status = VALUES(status),
                     updated_at = VALUES(updated_at);

UPDATE user_accounts
SET manager_user_id = @seed_base + 1
WHERE user_id BETWEEN @seed_base + 2 AND @seed_base + 10;

-- 2. Plans shown in report rows and individual usage rows.
INSERT INTO plan_templates (
    plan_template_id, plan_code, plan_name, description, customer_segment, template_scope,
    status, effective_from, effective_to, is_visible, allow_bulk_signing, allow_api_access,
    created_by, version_no, created_at, updated_at
)
SELECT
    @seed_base + n,
    CONCAT('SEED-RPT-', LPAD(n, 2, '0')),
    CONCAT('Seed Report Plan ', LPAD(n, 2, '0')),
    'Seed plan for reports and individual usage tracking',
    CASE WHEN n <= 8 THEN 'GROUP' ELSE 'INDIVIDUAL' END,
    'PUBLIC',
    'AVAILABLE',
    '2026-01-01',
    '2026-12-31',
    1,
    CASE WHEN n % 2 = 0 THEN 1 ELSE 0 END,
    CASE WHEN n % 3 = 0 THEN 1 ELSE 0 END,
    'seed_sql',
    1,
    TIMESTAMP('2026-01-01 09:00:00') + INTERVAL n DAY,
    TIMESTAMP('2026-03-01 09:00:00') + INTERVAL n DAY
FROM _seed_seq_15
ON DUPLICATE KEY UPDATE
                     plan_name = VALUES(plan_name),
                     customer_segment = VALUES(customer_segment),
                     status = VALUES(status),
                     effective_to = VALUES(effective_to),
                     updated_at = VALUES(updated_at);

-- 3. Pricing rules used by Subscription.pricingRule and individual CTS type/duration/fee filters.
INSERT INTO plan_pricing_rules (
    plan_pricing_rule_id, plan_template_id, subject_type, certificate_validity_value,
    certificate_validity_unit, pricing_metric, range_min, range_max, unit_price,
    currency, quota_total, sort_order, is_active, created_at, updated_at
)
SELECT
    @seed_base + n,
    @seed_base + n,
    CASE
        WHEN n % 3 = 1 THEN 'INDIVIDUAL'
        WHEN n % 3 = 2 THEN 'INDIVIDUAL_OF_ORG'
        ELSE 'ORGANIZATION'
        END,
    CASE WHEN n % 2 = 0 THEN 24 ELSE 12 END,
    'MONTH',
    'CERTIFICATE_COUNT',
    1,
    100 + n,
    150000 + (n * 25000),
    'VND',
    100 + (n * 10),
    n,
    1,
    TIMESTAMP('2026-01-02 09:00:00') + INTERVAL n DAY,
    TIMESTAMP('2026-03-02 09:00:00') + INTERVAL n DAY
FROM _seed_seq_15
ON DUPLICATE KEY UPDATE
                     subject_type = VALUES(subject_type),
                     certificate_validity_value = VALUES(certificate_validity_value),
                     unit_price = VALUES(unit_price),
                     quota_total = VALUES(quota_total),
                     is_active = VALUES(is_active),
                     updated_at = VALUES(updated_at);

-- 4. Active groups visible in the group report.
INSERT INTO `groups` (
    group_id, group_code, group_name, username, password, ref_contract_no, status,
    created_by, owner_user_id, created_at, updated_at
)
SELECT
    @seed_base + n,
    CONCAT('SEED-GRP-', LPAD(n, 2, '0')),
    CONCAT('Seed Agency ', LPAD(n, 2, '0')),
    CONCAT('seed_agency_', LPAD(n, 2, '0')),
    '$2a$10$seeded.group.password.hash',
    CONCAT('SEED-CONTRACT-', LPAD(n, 2, '0')),
    'ACTIVE',
    'seed_sql',
    @seed_base + 1 + MOD(n, 10),
    TIMESTAMP('2026-01-05 08:00:00') + INTERVAL n DAY,
    TIMESTAMP('2026-03-05 08:00:00') + INTERVAL n DAY
FROM _seed_seq_15
ON DUPLICATE KEY UPDATE
                     group_name = VALUES(group_name),
                     ref_contract_no = VALUES(ref_contract_no),
                     status = VALUES(status),
                     owner_user_id = VALUES(owner_user_id),
                     updated_at = VALUES(updated_at);

-- 5. Partner access used when report scope is ROLE_PARTNER/report:view:partner.
INSERT INTO partner_group_access (
    id, partner_user_id, group_id, granted_by, granted_at, revoked_at
)
SELECT
    @seed_base + n,
    @seed_base + 15,
    @seed_base + n,
    'seed_sql',
    TIMESTAMP('2026-02-01 10:00:00') + INTERVAL n DAY,
    NULL
FROM _seed_seq_15
ON DUPLICATE KEY UPDATE
                     granted_by = VALUES(granted_by),
                     granted_at = VALUES(granted_at),
                     revoked_at = VALUES(revoked_at);

-- 6. Active group assignments, also used for expiring-soon report rows.
INSERT INTO group_plan_assignments (
    group_plan_assignment_id, group_id, plan_template_id, assignment_status,
    requested_by, requested_at, approved_by, approved_at, apply_from, apply_to,
    activated_at, created_at, updated_at
)
SELECT
    @seed_base + n,
    @seed_base + n,
    @seed_base + n,
    'ACTIVE',
    'seed_sql',
    TIMESTAMP('2026-01-10 09:00:00') + INTERVAL n DAY,
    'seed_approver',
    TIMESTAMP('2026-01-11 09:00:00') + INTERVAL n DAY,
    '2026-01-15',
    DATE('2026-06-15') + INTERVAL n DAY,
    TIMESTAMP('2026-01-15 00:00:00') + INTERVAL n DAY,
    TIMESTAMP('2026-01-10 09:00:00') + INTERVAL n DAY,
    TIMESTAMP('2026-03-10 09:00:00') + INTERVAL n DAY
FROM _seed_seq_15
ON DUPLICATE KEY UPDATE
                     assignment_status = VALUES(assignment_status),
                     apply_from = VALUES(apply_from),
                     apply_to = VALUES(apply_to),
                     activated_at = VALUES(activated_at),
                     updated_at = VALUES(updated_at);

-- 7a. Individual subscriptions for individual report and usage tracking.
INSERT INTO subscriptions (
    subscription_id, subscriber_type, user_id, group_id, plan_template_id, pricing_rule_id,
    group_plan_assignment_id, retail_plan_schedule_id, source_type, source_id, status,
    start_date, end_date, signing_quota_total, signing_quota_used, activated_by,
    payment_reference, created_at, updated_at
)
SELECT
    @seed_base + n,
    'INDIVIDUAL',
    @seed_base + n,
    NULL,
    @seed_base + n,
    @seed_base + n,
    NULL,
    NULL,
    'RETAIL_PURCHASE',
    @seed_base + n,
    CASE
        WHEN n % 5 = 0 THEN 'EXPIRED'
        WHEN n % 7 = 0 THEN 'CANCELLED'
        ELSE 'ACTIVE'
        END,
    DATE('2026-03-01') + INTERVAL (n - 1) DAY,
    DATE('2027-03-01') + INTERVAL (n - 1) DAY,
    500 + (n * 20),
    20 + (n * 4),
    'seed_sql',
    CONCAT('SEED-PAY-IND-', LPAD(n, 2, '0')),
    TIMESTAMP('2026-03-01 08:00:00') + INTERVAL (n - 1) DAY,
    TIMESTAMP('2026-03-20 08:00:00') + INTERVAL n DAY
FROM _seed_seq_15
ON DUPLICATE KEY UPDATE
                     subscriber_type = VALUES(subscriber_type),
                     user_id = VALUES(user_id),
                     plan_template_id = VALUES(plan_template_id),
                     pricing_rule_id = VALUES(pricing_rule_id),
                     status = VALUES(status),
                     signing_quota_total = VALUES(signing_quota_total),
                     signing_quota_used = VALUES(signing_quota_used),
                     updated_at = VALUES(updated_at);

-- 7b. Group subscriptions so group certificate and signing reports have assignment-linked data.
INSERT INTO subscriptions (
    subscription_id, subscriber_type, user_id, group_id, plan_template_id, pricing_rule_id,
    group_plan_assignment_id, retail_plan_schedule_id, source_type, source_id, status,
    start_date, end_date, signing_quota_total, signing_quota_used, activated_by,
    payment_reference, created_at, updated_at
)
SELECT
    @seed_base + 100 + n,
    'GROUP',
    @seed_base + n,
    @seed_base + n,
    @seed_base + n,
    @seed_base + n,
    @seed_base + n,
    NULL,
    'GROUP_ASSIGNMENT',
    @seed_base + n,
    'ACTIVE',
    '2026-01-15',
    DATE('2026-06-15') + INTERVAL n DAY,
    2000 + (n * 50),
    100 + (n * 7),
    'seed_sql',
    CONCAT('SEED-PAY-GRP-', LPAD(n, 2, '0')),
    TIMESTAMP('2026-01-15 08:00:00') + INTERVAL n DAY,
    TIMESTAMP('2026-03-20 09:00:00') + INTERVAL n DAY
FROM _seed_seq_15
ON DUPLICATE KEY UPDATE
                     subscriber_type = VALUES(subscriber_type),
                     user_id = VALUES(user_id),
                     group_id = VALUES(group_id),
                     plan_template_id = VALUES(plan_template_id),
                     pricing_rule_id = VALUES(pricing_rule_id),
                     group_plan_assignment_id = VALUES(group_plan_assignment_id),
                     status = VALUES(status),
                     signing_quota_total = VALUES(signing_quota_total),
                     signing_quota_used = VALUES(signing_quota_used),
                     updated_at = VALUES(updated_at);

-- 8a. Group monthly aggregates for the default report month.
INSERT INTO usage_aggregates (
    usage_aggregate_id, aggregate_scope, scope_id, period_type, period_key,
    certificates_created, signing_used, active_certificates, expired_certificates,
    revoked_certificates, amount_due, currency, created_at, updated_at
)
SELECT
    @seed_base + n,
    'GROUP',
    @seed_base + n,
    'MONTH',
    @period_key,
    10 + n,
    100 + (n * 7),
    8 + n,
    MOD(n, 4),
    MOD(n, 3),
    1000000 + (n * 125000),
    'VND',
    TIMESTAMP('2026-03-31 23:00:00') + INTERVAL n MINUTE,
    TIMESTAMP('2026-03-31 23:30:00') + INTERVAL n MINUTE
FROM _seed_seq_15
ON DUPLICATE KEY UPDATE
                     certificates_created = VALUES(certificates_created),
                     signing_used = VALUES(signing_used),
                     active_certificates = VALUES(active_certificates),
                     expired_certificates = VALUES(expired_certificates),
                     revoked_certificates = VALUES(revoked_certificates),
                     amount_due = VALUES(amount_due),
                     updated_at = VALUES(updated_at);

-- 8b. Previous month aggregates so growth percentages are non-zero.
INSERT INTO usage_aggregates (
    usage_aggregate_id, aggregate_scope, scope_id, period_type, period_key,
    certificates_created, signing_used, active_certificates, expired_certificates,
    revoked_certificates, amount_due, currency, created_at, updated_at
)
SELECT
    @seed_base + 100 + n,
    'GROUP',
    @seed_base + n,
    'MONTH',
    @prev_period_key,
    7 + n,
    80 + (n * 5),
    6 + n,
    MOD(n, 3),
    MOD(n, 2),
    800000 + (n * 100000),
    'VND',
    TIMESTAMP('2026-02-28 23:00:00') + INTERVAL n MINUTE,
    TIMESTAMP('2026-02-28 23:30:00') + INTERVAL n MINUTE
FROM _seed_seq_15
ON DUPLICATE KEY UPDATE
                     certificates_created = VALUES(certificates_created),
                     signing_used = VALUES(signing_used),
                     active_certificates = VALUES(active_certificates),
                     expired_certificates = VALUES(expired_certificates),
                     revoked_certificates = VALUES(revoked_certificates),
                     amount_due = VALUES(amount_due),
                     updated_at = VALUES(updated_at);

-- 9a. Individual certificate provisioning records for weekly individual report.
INSERT INTO certificate_provisioning_records (
    id, subscription_id, group_plan_assignment_id, pricing_rule_id, user_id, request_id,
    status, cert_type, certificate_id, key_id, issued_at, expires_at, retry_count,
    usage_count, last_attempted_at, failure_reason, created_at, updated_at
)
SELECT
    @seed_base + n,
    @seed_base + n,
    NULL,
    @seed_base + n,
    @seed_base + n,
    CONCAT('seed-ind-req-', LPAD(n, 2, '0')),
    'COMPLETED',
    CASE WHEN n % 3 = 1 THEN 1 WHEN n % 3 = 2 THEN 2 ELSE 3 END,
    CONCAT('SEED-IND-CERT-', LPAD(n, 2, '0')),
    CONCAT('SEED-IND-KEY-', LPAD(n, 2, '0')),
    TIMESTAMP('2026-03-01 10:00:00') + INTERVAL (n - 1) DAY,
    TIMESTAMP('2027-03-01 10:00:00') + INTERVAL (n - 1) DAY,
    0,
    5 + n,
    TIMESTAMP('2026-03-01 10:05:00') + INTERVAL (n - 1) DAY,
    NULL,
    TIMESTAMP('2026-03-01 09:55:00') + INTERVAL (n - 1) DAY,
    TIMESTAMP('2026-03-01 10:10:00') + INTERVAL (n - 1) DAY
FROM _seed_seq_15
ON DUPLICATE KEY UPDATE
                     status = VALUES(status),
                     cert_type = VALUES(cert_type),
                     certificate_id = VALUES(certificate_id),
                     issued_at = VALUES(issued_at),
                     expires_at = VALUES(expires_at),
                     usage_count = VALUES(usage_count),
                     updated_at = VALUES(updated_at);

-- 9b. Group certificate provisioning records for group report cert type ratios.
INSERT INTO certificate_provisioning_records (
    id, subscription_id, group_plan_assignment_id, pricing_rule_id, user_id, request_id,
    status, cert_type, certificate_id, key_id, issued_at, expires_at, retry_count,
    usage_count, last_attempted_at, failure_reason, created_at, updated_at
)
SELECT
    @seed_base + 100 + n,
    @seed_base + 100 + n,
    @seed_base + n,
    @seed_base + n,
    @seed_base + n,
    CONCAT('seed-grp-req-', LPAD(n, 2, '0')),
    'COMPLETED',
    CASE WHEN n % 3 = 1 THEN 1 WHEN n % 3 = 2 THEN 2 ELSE 3 END,
    CONCAT('SEED-GRP-CERT-', LPAD(n, 2, '0')),
    CONCAT('SEED-GRP-KEY-', LPAD(n, 2, '0')),
    TIMESTAMP('2026-03-02 11:00:00') + INTERVAL (n - 1) DAY,
    TIMESTAMP('2027-03-02 11:00:00') + INTERVAL (n - 1) DAY,
    0,
    10 + n,
    TIMESTAMP('2026-03-02 11:05:00') + INTERVAL (n - 1) DAY,
    NULL,
    TIMESTAMP('2026-03-02 10:55:00') + INTERVAL (n - 1) DAY,
    TIMESTAMP('2026-03-02 11:10:00') + INTERVAL (n - 1) DAY
FROM _seed_seq_15
ON DUPLICATE KEY UPDATE
                     status = VALUES(status),
                     cert_type = VALUES(cert_type),
                     certificate_id = VALUES(certificate_id),
                     issued_at = VALUES(issued_at),
                     expires_at = VALUES(expires_at),
                     usage_count = VALUES(usage_count),
                     updated_at = VALUES(updated_at);

-- 10a. Individual signing usage records.
INSERT INTO certificate_usage_records (
    id, certificate_id, user_id, subscription_id, group_plan_assignment_id,
    usage_type, quantity, used_at
)
SELECT
    @seed_base + n,
    CONCAT('SEED-IND-CERT-', LPAD(n, 2, '0')),
    @seed_base + n,
    @seed_base + n,
    NULL,
    'SIGNING',
    1 + MOD(n, 3),
    TIMESTAMP('2026-03-03 12:00:00') + INTERVAL (n - 1) DAY
FROM _seed_seq_15
ON DUPLICATE KEY UPDATE
                     certificate_id = VALUES(certificate_id),
                     subscription_id = VALUES(subscription_id),
                     usage_type = VALUES(usage_type),
                     quantity = VALUES(quantity),
                     used_at = VALUES(used_at);

-- 10b. Group signing usage records.
INSERT INTO certificate_usage_records (
    id, certificate_id, user_id, subscription_id, group_plan_assignment_id,
    usage_type, quantity, used_at
)
SELECT
    @seed_base + 100 + n,
    CONCAT('SEED-GRP-CERT-', LPAD(n, 2, '0')),
    @seed_base + n,
    @seed_base + 100 + n,
    @seed_base + n,
    'SIGNING',
    2 + MOD(n, 4),
    TIMESTAMP('2026-03-04 13:00:00') + INTERVAL (n - 1) DAY
FROM _seed_seq_15
ON DUPLICATE KEY UPDATE
                     certificate_id = VALUES(certificate_id),
                     subscription_id = VALUES(subscription_id),
                     group_plan_assignment_id = VALUES(group_plan_assignment_id),
                     usage_type = VALUES(usage_type),
                     quantity = VALUES(quantity),
                     used_at = VALUES(used_at);

-- 11. Document upload events used by upload stats in individual report.
INSERT INTO document_upload_records (
    id, user_id, subscription_id, certificate_id, document_id, upload_status,
    uploaded_at, created_at
)
SELECT
    @seed_base + n,
    @seed_base + n,
    @seed_base + n,
    CONCAT('SEED-IND-CERT-', LPAD(n, 2, '0')),
    CONCAT('SEED-DOC-', LPAD(n, 2, '0')),
    CASE WHEN n % 5 = 0 THEN 'FAILED' ELSE 'SUCCESS' END,
    TIMESTAMP('2026-03-05 14:00:00') + INTERVAL (n - 1) DAY,
    TIMESTAMP('2026-03-05 14:01:00') + INTERVAL (n - 1) DAY
FROM _seed_seq_15
ON DUPLICATE KEY UPDATE
                     subscription_id = VALUES(subscription_id),
                     certificate_id = VALUES(certificate_id),
                     document_id = VALUES(document_id),
                     upload_status = VALUES(upload_status),
                     uploaded_at = VALUES(uploaded_at);

-- 12. Authentication failure events used by PIN/OTP/MOC failure charts.
INSERT INTO certificate_auth_failure_records (
    id, user_id, subscription_id, certificate_id, failure_type, reason_code,
    failed_at, created_at
)
SELECT
    @seed_base + n,
    @seed_base + n,
    @seed_base + n,
    CONCAT('SEED-IND-CERT-', LPAD(n, 2, '0')),
    CASE WHEN n % 3 = 1 THEN 'PIN' WHEN n % 3 = 2 THEN 'OTP' ELSE 'MOC' END,
    CASE WHEN n % 3 = 1 THEN 'PIN_INVALID' WHEN n % 3 = 2 THEN 'OTP_EXPIRED' ELSE 'MOC_REJECTED' END,
    TIMESTAMP('2026-03-06 15:00:00') + INTERVAL (n - 1) DAY,
    TIMESTAMP('2026-03-06 15:01:00') + INTERVAL (n - 1) DAY
FROM _seed_seq_15
ON DUPLICATE KEY UPDATE
                     subscription_id = VALUES(subscription_id),
                     certificate_id = VALUES(certificate_id),
                     failure_type = VALUES(failure_type),
                     reason_code = VALUES(reason_code),
                     failed_at = VALUES(failed_at);

DROP TEMPORARY TABLE IF EXISTS _seed_seq_15;
