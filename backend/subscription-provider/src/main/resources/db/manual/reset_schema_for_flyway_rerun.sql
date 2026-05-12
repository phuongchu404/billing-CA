-- ============================================================
-- Reset schema so Flyway can run from the beginning again.
--
-- Use case:
--   1. Stop backend services using this database.
--   2. Run this script against the target schema.
--   3. Start the backend again so Flyway reruns db/migration from V1.
--
-- Why DROP instead of only TRUNCATE:
--   Flyway V1 creates tables with CREATE TABLE. If tables still exist after
--   TRUNCATE, rerunning Flyway from version 1 will fail with "table exists".
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- Newer extension tables.
DROP TABLE IF EXISTS minio_orphan_tracking;
DROP TABLE IF EXISTS plan_subject_config;

-- Payment, settlement, audit, and aggregate tables.
DROP TABLE IF EXISTS payment_records;
DROP TABLE IF EXISTS settlement_statements;
DROP TABLE IF EXISTS usage_aggregate_rollup_log;
DROP TABLE IF EXISTS usage_aggregates;
DROP TABLE IF EXISTS assignment_audits;
DROP TABLE IF EXISTS subscription_audit_logs;

-- Report/event and certificate usage tables.
DROP TABLE IF EXISTS certificate_auth_failure_records;
DROP TABLE IF EXISTS document_upload_records;
DROP TABLE IF EXISTS certificate_usage_daily;
DROP TABLE IF EXISTS certificate_usage_records;
DROP TABLE IF EXISTS certificate_provisioning_records;

-- Subscription and approval tables.
DROP TABLE IF EXISTS subscriptions;
DROP TABLE IF EXISTS approval_request_steps;
DROP TABLE IF EXISTS approval_requests;
DROP TABLE IF EXISTS approval_level_configs;

-- Group/customer and plan tables.
DROP TABLE IF EXISTS group_members;
DROP TABLE IF EXISTS retail_plan_schedules;
DROP TABLE IF EXISTS group_plan_assignments;
DROP TABLE IF EXISTS group_contacts;
DROP TABLE IF EXISTS plan_pricing_rules;
DROP TABLE IF EXISTS plan_templates;
DROP TABLE IF EXISTS partner_group_access;
DROP TABLE IF EXISTS `groups`;

-- Auth/system tables.
DROP TABLE IF EXISTS password_reset_tokens;
DROP TABLE IF EXISTS password_history;
DROP TABLE IF EXISTS refresh_tokens;
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS permissions;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS admin_audit_logs;
DROP TABLE IF EXISTS system_settings;
DROP TABLE IF EXISTS user_accounts;

-- Flyway history must be removed last so migrations run again from V1.
DROP TABLE IF EXISTS flyway_schema_history;

SET FOREIGN_KEY_CHECKS = 1;
