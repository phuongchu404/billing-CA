-- ============================================================
-- Seed default data for report tables 3.7 -> 3.10
-- Tables:
--   3.7  certificate_provisioning_records
--   3.8  certificate_usage_records
--   3.9  usage_aggregates
--   3.10 usage_aggregate_rollup_log
--
-- Notes:
--   - This script expects at least one ACTIVE subscription to exist.
--   - It reuses existing subscriptions and group assignments where possible.
--   - All seed certificate/request keys use prefix SEED-RPT-3710 for safe cleanup.
--   - Run manually after base schema and base business seed data are available.
-- ============================================================

START TRANSACTION;

-- ============================================================
-- 0. Resolve existing FK targets
-- ============================================================

SET @group_sub_id := (
    SELECT subscription_id
    FROM subscriptions
    WHERE subscriber_type = 'GROUP'
    ORDER BY subscription_id
    LIMIT 1
);

SET @group_assignment_id := (
    SELECT group_plan_assignment_id
    FROM subscriptions
    WHERE subscription_id = @group_sub_id
    LIMIT 1
);

SET @group_pricing_rule_id := (
    SELECT pricing_rule_id
    FROM subscriptions
    WHERE subscription_id = @group_sub_id
    LIMIT 1
);

SET @individual_sub_id := (
    SELECT subscription_id
    FROM subscriptions
    WHERE subscriber_type = 'INDIVIDUAL'
    ORDER BY subscription_id
    LIMIT 1
);

SET @individual_pricing_rule_id := (
    SELECT pricing_rule_id
    FROM subscriptions
    WHERE subscription_id = @individual_sub_id
    LIMIT 1
);

SET @fallback_sub_id := (
    SELECT subscription_id
    FROM subscriptions
    ORDER BY subscription_id
    LIMIT 1
);

SET @group_sub_id := COALESCE(@group_sub_id, @fallback_sub_id);
SET @individual_sub_id := COALESCE(@individual_sub_id, @fallback_sub_id);

-- If @fallback_sub_id is NULL, the inserts below will fail because
-- certificate_provisioning_records.subscription_id is NOT NULL.

-- ============================================================
-- 1. Cleanup previous run for this seed namespace
-- ============================================================

DELETE FROM certificate_usage_records
WHERE certificate_id LIKE 'SEED-RPT-3710-CERT-%';

DELETE FROM certificate_provisioning_records
WHERE request_id LIKE 'SEED-RPT-3710-REQ-%'
   OR certificate_id LIKE 'SEED-RPT-3710-CERT-%';

DELETE FROM usage_aggregates
WHERE period_key IN ('2026-01', '2026-02', '2026-03', '2026-04')
  AND scope_id BETWEEN 9101 AND 9110
  AND aggregate_scope IN ('GROUP', 'GROUP_ASSIGNMENT');

DELETE FROM usage_aggregate_rollup_log
WHERE period_key LIKE 'SEED3710-%';

-- ============================================================
-- 2. Table 3.7: certificate_provisioning_records
--    At least 10 records. Mix of GROUP and INDIVIDUAL-style data.
-- ============================================================

INSERT INTO certificate_provisioning_records (
    subscription_id,
    group_plan_assignment_id,
    pricing_rule_id,
    user_id,
    request_id,
    status,
    cert_type,
    certificate_id,
    key_id,
    issued_at,
    expires_at,
    retry_count,
    usage_count,
    last_attempted_at,
    failure_reason
) VALUES
(@group_sub_id,      @group_assignment_id, @group_pricing_rule_id,      910001, 'SEED-RPT-3710-REQ-001', 'COMPLETED', 1, 'SEED-RPT-3710-CERT-001', 'SEED-RPT-3710-KEY-001', '2026-03-01 09:00:00', '2027-03-01 09:00:00', 0, 3,  '2026-03-01 09:00:00', NULL),
(@group_sub_id,      @group_assignment_id, @group_pricing_rule_id,      910002, 'SEED-RPT-3710-REQ-002', 'COMPLETED', 1, 'SEED-RPT-3710-CERT-002', 'SEED-RPT-3710-KEY-002', '2026-03-03 10:00:00', '2027-03-03 10:00:00', 0, 5,  '2026-03-03 10:00:00', NULL),
(@group_sub_id,      @group_assignment_id, @group_pricing_rule_id,      910003, 'SEED-RPT-3710-REQ-003', 'COMPLETED', 2, 'SEED-RPT-3710-CERT-003', 'SEED-RPT-3710-KEY-003', '2026-03-05 11:00:00', '2027-03-05 11:00:00', 0, 2,  '2026-03-05 11:00:00', NULL),
(@group_sub_id,      @group_assignment_id, @group_pricing_rule_id,      910004, 'SEED-RPT-3710-REQ-004', 'COMPLETED', 3, 'SEED-RPT-3710-CERT-004', 'SEED-RPT-3710-KEY-004', '2026-03-08 08:30:00', '2027-03-08 08:30:00', 0, 8,  '2026-03-08 08:30:00', NULL),
(@group_sub_id,      @group_assignment_id, @group_pricing_rule_id,      910005, 'SEED-RPT-3710-REQ-005', 'COMPLETED', 1, 'SEED-RPT-3710-CERT-005', 'SEED-RPT-3710-KEY-005', '2026-03-12 14:00:00', '2027-03-12 14:00:00', 0, 4,  '2026-03-12 14:00:00', NULL),
(@individual_sub_id, NULL,                 @individual_pricing_rule_id, 910006, 'SEED-RPT-3710-REQ-006', 'COMPLETED', 1, 'SEED-RPT-3710-CERT-006', 'SEED-RPT-3710-KEY-006', '2026-03-15 09:15:00', '2027-03-15 09:15:00', 0, 7,  '2026-03-15 09:15:00', NULL),
(@individual_sub_id, NULL,                 @individual_pricing_rule_id, 910007, 'SEED-RPT-3710-REQ-007', 'COMPLETED', 2, 'SEED-RPT-3710-CERT-007', 'SEED-RPT-3710-KEY-007', '2026-03-18 10:45:00', '2027-03-18 10:45:00', 0, 1,  '2026-03-18 10:45:00', NULL),
(@individual_sub_id, NULL,                 @individual_pricing_rule_id, 910008, 'SEED-RPT-3710-REQ-008', 'COMPLETED', 3, 'SEED-RPT-3710-CERT-008', 'SEED-RPT-3710-KEY-008', '2026-03-22 13:20:00', '2027-03-22 13:20:00', 0, 6,  '2026-03-22 13:20:00', NULL),
(@individual_sub_id, NULL,                 @individual_pricing_rule_id, 910009, 'SEED-RPT-3710-REQ-009', 'COMPLETED', 1, 'SEED-RPT-3710-CERT-009', 'SEED-RPT-3710-KEY-009', '2026-03-25 16:10:00', '2027-03-25 16:10:00', 0, 9,  '2026-03-25 16:10:00', NULL),
(@individual_sub_id, NULL,                 @individual_pricing_rule_id, 910010, 'SEED-RPT-3710-REQ-010', 'COMPLETED', 1, 'SEED-RPT-3710-CERT-010', 'SEED-RPT-3710-KEY-010', '2026-03-29 08:00:00', '2027-03-29 08:00:00', 0, 11, '2026-03-29 08:00:00', NULL);

-- ============================================================
-- 3. Table 3.8: certificate_usage_records
--    At least 10 records. SIGNING rows point to seed certificates above.
-- ============================================================

INSERT INTO certificate_usage_records (
    certificate_id,
    user_id,
    subscription_id,
    group_plan_assignment_id,
    usage_type,
    quantity,
    used_at
) VALUES
('SEED-RPT-3710-CERT-001', 910001, @group_sub_id,      @group_assignment_id, 'SIGNING', 1, '2026-03-01 10:00:00'),
('SEED-RPT-3710-CERT-002', 910002, @group_sub_id,      @group_assignment_id, 'SIGNING', 1, '2026-03-04 11:00:00'),
('SEED-RPT-3710-CERT-003', 910003, @group_sub_id,      @group_assignment_id, 'SIGNING', 1, '2026-03-06 09:30:00'),
('SEED-RPT-3710-CERT-004', 910004, @group_sub_id,      @group_assignment_id, 'SIGNING', 1, '2026-03-09 15:15:00'),
('SEED-RPT-3710-CERT-005', 910005, @group_sub_id,      @group_assignment_id, 'SIGNING', 1, '2026-03-13 08:45:00'),
('SEED-RPT-3710-CERT-006', 910006, @individual_sub_id, NULL,                 'SIGNING', 1, '2026-03-16 10:20:00'),
('SEED-RPT-3710-CERT-007', 910007, @individual_sub_id, NULL,                 'SIGNING', 1, '2026-03-19 11:40:00'),
('SEED-RPT-3710-CERT-008', 910008, @individual_sub_id, NULL,                 'SIGNING', 1, '2026-03-23 14:10:00'),
('SEED-RPT-3710-CERT-009', 910009, @individual_sub_id, NULL,                 'SIGNING', 1, '2026-03-26 16:30:00'),
('SEED-RPT-3710-CERT-010', 910010, @individual_sub_id, NULL,                 'SIGNING', 1, '2026-03-30 09:05:00');

-- ============================================================
-- 4. Table 3.9: usage_aggregates
--    At least 10 records. These use isolated scope_id values 9101..9110
--    to avoid colliding with real group monthly dashboard rows.
-- ============================================================

INSERT INTO usage_aggregates (
    aggregate_scope,
    scope_id,
    period_type,
    period_key,
    certificates_created,
    signing_used,
    active_certificates,
    expired_certificates,
    revoked_certificates,
    amount_due,
    currency
) VALUES
('GROUP',            9101, 'MONTH', '2026-01', 12, 120, 10, 1, 1, 1200000.00, 'VND'),
('GROUP',            9102, 'MONTH', '2026-01', 18, 190, 17, 1, 0, 1900000.00, 'VND'),
('GROUP',            9103, 'MONTH', '2026-02', 24, 260, 23, 0, 1, 2600000.00, 'VND'),
('GROUP',            9104, 'MONTH', '2026-02',  8,  75,  8, 0, 0,  750000.00, 'VND'),
('GROUP',            9105, 'MONTH', '2026-03', 31, 420, 30, 1, 0, 4200000.00, 'VND'),
('GROUP',            9106, 'MONTH', '2026-03',  6,  54,  5, 1, 0,  540000.00, 'VND'),
('GROUP',            9107, 'MONTH', '2026-04', 15, 180, 14, 0, 1, 1800000.00, 'VND'),
('GROUP',            9108, 'MONTH', '2026-04', 22, 310, 21, 1, 0, 3100000.00, 'VND'),
('GROUP_ASSIGNMENT', 9109, 'MONTH', '2026-03',  5,  44,  5, 0, 0,  440000.00, 'VND'),
('GROUP_ASSIGNMENT', 9110, 'MONTH', '2026-03',  9,  88,  9, 0, 0,  880000.00, 'VND');

-- ============================================================
-- 5. Table 3.10: usage_aggregate_rollup_log
--    At least 10 records.
-- ============================================================

INSERT INTO usage_aggregate_rollup_log (
    period_key,
    run_at,
    groups_updated,
    status,
    error_msg
) VALUES
('SEED3710-260101', '2026-01-02 01:00:00', 3,  'SUCCESS', NULL),
('SEED3710-260102', '2026-01-03 01:00:00', 4,  'SUCCESS', NULL),
('SEED3710-260103', '2026-01-04 01:00:00', 4,  'SUCCESS', NULL),
('SEED3710-260201', '2026-02-02 01:00:00', 5,  'SUCCESS', NULL),
('SEED3710-260202', '2026-02-03 01:00:00', 5,  'SUCCESS', NULL),
('SEED3710-260203', '2026-02-04 01:00:00', 6,  'SUCCESS', NULL),
('SEED3710-260301', '2026-03-02 01:00:00', 8,  'SUCCESS', NULL),
('SEED3710-260302', '2026-03-03 01:00:00', 8,  'SUCCESS', NULL),
('SEED3710-260303', '2026-03-04 01:00:00', 10, 'SUCCESS', NULL),
('SEED3710-260401', '2026-04-02 01:00:00', 7,  'ERROR',   'Sample seed error row for dashboard/testing');

COMMIT;
