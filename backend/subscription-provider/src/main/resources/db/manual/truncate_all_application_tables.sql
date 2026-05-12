-- ============================================================
-- Truncate all application tables but keep the schema and Flyway history.
--
-- Use this when you only want clean data and do NOT want Flyway to rerun V1.
-- If you want Flyway to run from the beginning again, use:
--   manual/reset_schema_for_flyway_rerun.sql
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE minio_orphan_tracking;
TRUNCATE TABLE plan_subject_config;

TRUNCATE TABLE payment_records;
TRUNCATE TABLE settlement_statements;
TRUNCATE TABLE usage_aggregate_rollup_log;
TRUNCATE TABLE usage_aggregates;
TRUNCATE TABLE assignment_audits;
TRUNCATE TABLE subscription_audit_logs;

TRUNCATE TABLE certificate_auth_failure_records;
TRUNCATE TABLE document_upload_records;
TRUNCATE TABLE certificate_usage_daily;
TRUNCATE TABLE certificate_usage_records;
TRUNCATE TABLE certificate_provisioning_records;

TRUNCATE TABLE subscriptions;
TRUNCATE TABLE approval_request_steps;
TRUNCATE TABLE approval_requests;
TRUNCATE TABLE approval_level_configs;

TRUNCATE TABLE group_members;
TRUNCATE TABLE retail_plan_schedules;
TRUNCATE TABLE group_plan_assignments;
TRUNCATE TABLE group_contacts;
TRUNCATE TABLE plan_pricing_rules;
TRUNCATE TABLE plan_templates;
TRUNCATE TABLE partner_group_access;
TRUNCATE TABLE `groups`;

TRUNCATE TABLE password_reset_tokens;
TRUNCATE TABLE password_history;
TRUNCATE TABLE refresh_tokens;
TRUNCATE TABLE role_permissions;
TRUNCATE TABLE user_roles;
TRUNCATE TABLE permissions;
TRUNCATE TABLE roles;
TRUNCATE TABLE admin_audit_logs;
TRUNCATE TABLE system_settings;
TRUNCATE TABLE user_accounts;

SET FOREIGN_KEY_CHECKS = 1;
