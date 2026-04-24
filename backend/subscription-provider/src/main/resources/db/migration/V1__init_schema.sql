-- Clean initial schema for a brand-new database.
-- This file reflects the current entity model only.

CREATE TABLE user_accounts (
    user_id                VARCHAR(36)  NOT NULL,
    username               VARCHAR(100) NOT NULL,
    email                  VARCHAR(200) NOT NULL,
    full_name              VARCHAR(200) NOT NULL,
    password_hash          VARCHAR(255) NOT NULL,
    auth_provider          VARCHAR(30)  NOT NULL DEFAULT 'LOCAL',
    status                 VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    failed_login_attempts  INT          NOT NULL DEFAULT 0,
    locked_until           DATETIME     NULL,
    last_login_at          DATETIME     NULL,
    created_by             VARCHAR(36)  NULL,
    created_at             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_user_accounts_username (username),
    UNIQUE KEY uk_user_accounts_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE roles (
    role_id          BIGINT       NOT NULL AUTO_INCREMENT,
    role_name        VARCHAR(100) NOT NULL,
    display_name     VARCHAR(200) NOT NULL,
    description      TEXT         NULL,
    is_system_role   TINYINT(1)   NOT NULL DEFAULT 0,
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id),
    UNIQUE KEY uk_roles_role_name (role_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE permissions (
    permission_id    BIGINT       NOT NULL AUTO_INCREMENT,
    permission_key   VARCHAR(100) NOT NULL,
    display_name     VARCHAR(200) NOT NULL,
    module_group     VARCHAR(100) NOT NULL,
    group_name       VARCHAR(100) NOT NULL,
    sort_order       INT          NOT NULL,
    PRIMARY KEY (permission_id),
    UNIQUE KEY uk_permissions_permission_key (permission_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_roles (
    user_id          VARCHAR(36) NOT NULL,
    role_id          BIGINT      NOT NULL,
    created_at       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES user_accounts(user_id),
    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id) REFERENCES roles(role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE role_permissions (
    id               BIGINT NOT NULL AUTO_INCREMENT,
    role_id          BIGINT NOT NULL,
    permission_id    BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_permissions_role_permission (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role
        FOREIGN KEY (role_id) REFERENCES roles(role_id),
    CONSTRAINT fk_role_permissions_permission
        FOREIGN KEY (permission_id) REFERENCES permissions(permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE refresh_tokens (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    token            VARCHAR(36)  NOT NULL,
    user_id          VARCHAR(36)  NOT NULL,
    status           VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    expires_at       DATETIME     NOT NULL,
    issued_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked_at       DATETIME     NULL,
    user_agent       VARCHAR(500) NULL,
    ip_address       VARCHAR(45)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_refresh_tokens_token (token),
    KEY idx_refresh_tokens_user_status (user_id, status),
    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id) REFERENCES user_accounts(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE password_history (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    user_id          VARCHAR(36)  NOT NULL,
    password_hash    VARCHAR(255) NOT NULL,
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_password_history_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE admin_audit_logs (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    actor            VARCHAR(150) NOT NULL,
    action           VARCHAR(100) NOT NULL,
    entity_type      VARCHAR(50)  NOT NULL,
    entity_id        VARCHAR(150) NOT NULL,
    details          TEXT         NULL,
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_admin_audit_logs_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE system_settings (
    setting_key      VARCHAR(100) NOT NULL,
    setting_value    TEXT         NULL,
    setting_type     VARCHAR(20)  NOT NULL,
    category         VARCHAR(50)  NOT NULL,
    description      VARCHAR(300) NULL,
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `groups` (
    group_id         BIGINT       NOT NULL AUTO_INCREMENT,
    group_code       VARCHAR(100) NOT NULL,
    group_name       VARCHAR(200) NOT NULL,
    username         VARCHAR(100) NOT NULL,
    password         VARCHAR(255) NOT NULL,
    ref_contract_no  VARCHAR(200) NULL,
    status           VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    created_by       VARCHAR(100) NOT NULL,
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (group_id),
    UNIQUE KEY uk_groups_group_code (group_code),
    UNIQUE KEY uk_groups_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE plan_templates (
    plan_template_id         BIGINT       NOT NULL AUTO_INCREMENT,
    plan_code                VARCHAR(50)  NOT NULL,
    plan_name                VARCHAR(150) NOT NULL,
    description              TEXT         NULL,
    customer_segment         VARCHAR(30)  NOT NULL DEFAULT 'INDIVIDUAL',
    template_scope           VARCHAR(30)  NOT NULL DEFAULT 'PUBLIC',
    status                   VARCHAR(30)  NOT NULL DEFAULT 'DRAFT',
    effective_from           DATE         NULL,
    effective_to             DATE         NULL,
    is_visible               TINYINT(1)   NOT NULL DEFAULT 1,
    allow_bulk_signing       TINYINT(1)   NOT NULL DEFAULT 0,
    allow_api_access         TINYINT(1)   NOT NULL DEFAULT 0,
    created_by               VARCHAR(100) NULL,
    cloned_from_template_id  BIGINT       NULL,
    version_no               INT          NOT NULL DEFAULT 1,
    created_at               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (plan_template_id),
    UNIQUE KEY uk_plan_templates_plan_code (plan_code),
    CONSTRAINT fk_plan_templates_cloned_from
        FOREIGN KEY (cloned_from_template_id) REFERENCES plan_templates(plan_template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE plan_pricing_rules (
    plan_pricing_rule_id        BIGINT        NOT NULL AUTO_INCREMENT,
    plan_template_id            BIGINT        NOT NULL,
    subject_type                VARCHAR(30)   NOT NULL,
    certificate_validity_value  INT           NOT NULL,
    certificate_validity_unit   VARCHAR(20)   NOT NULL DEFAULT 'MONTH',
    pricing_metric              VARCHAR(30)   NOT NULL DEFAULT 'CERTIFICATE_COUNT',
    range_min                   INT           NOT NULL,
    range_max                   INT           NULL,
    unit_price                  DECIMAL(15,2) NOT NULL,
    currency                    CHAR(3)       NOT NULL DEFAULT 'VND',
    quota_total                 INT           NULL,
    sort_order                  INT           NOT NULL DEFAULT 0,
    is_active                   TINYINT(1)    NOT NULL DEFAULT 1,
    created_at                  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (plan_pricing_rule_id),
    KEY idx_ppr_template (plan_template_id),
    KEY idx_ppr_active_sort (plan_template_id, is_active, sort_order),
    CONSTRAINT fk_plan_pricing_rules_template
        FOREIGN KEY (plan_template_id) REFERENCES plan_templates(plan_template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE group_contacts (
    group_contact_id     BIGINT       NOT NULL AUTO_INCREMENT,
    group_id             BIGINT       NOT NULL,
    contact_type         VARCHAR(30)  NOT NULL,
    email                VARCHAR(200) NOT NULL,
    full_name            VARCHAR(200) NULL,
    phone                VARCHAR(50)  NULL,
    is_primary           TINYINT(1)   NOT NULL DEFAULT 0,
    receive_usage_alert  TINYINT(1)   NOT NULL DEFAULT 0,
    is_active            TINYINT(1)   NOT NULL DEFAULT 1,
    created_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (group_contact_id),
    UNIQUE KEY uk_group_contact_type_email (group_id, contact_type, email),
    CONSTRAINT fk_group_contacts_group
        FOREIGN KEY (group_id) REFERENCES `groups`(group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE group_plan_assignments (
    group_plan_assignment_id BIGINT       NOT NULL AUTO_INCREMENT,
    group_id                 BIGINT       NOT NULL,
    plan_template_id         BIGINT       NOT NULL,
    assignment_status        VARCHAR(30)  NOT NULL DEFAULT 'REQUESTED',
    requested_by             VARCHAR(100) NULL,
    requested_at             DATETIME     NULL,
    approved_by              VARCHAR(100) NULL,
    approved_at              DATETIME     NULL,
    rejected_by              VARCHAR(100) NULL,
    rejected_at              DATETIME     NULL,
    apply_from               DATE         NULL,
    apply_to                 DATE         NULL,
    activated_at             DATETIME     NULL,
    stopped_at               DATETIME     NULL,
    stop_reason              TEXT         NULL,
    created_at               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (group_plan_assignment_id),
    KEY idx_gpa_group_status (group_id, assignment_status),
    KEY idx_gpa_apply_range (apply_from, apply_to),
    CONSTRAINT fk_group_plan_assignments_group
        FOREIGN KEY (group_id) REFERENCES `groups`(group_id),
    CONSTRAINT fk_group_plan_assignments_template
        FOREIGN KEY (plan_template_id) REFERENCES plan_templates(plan_template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE retail_plan_schedules (
    retail_plan_schedule_id BIGINT       NOT NULL AUTO_INCREMENT,
    plan_template_id        BIGINT       NOT NULL,
    schedule_status         VARCHAR(30)  NOT NULL DEFAULT 'AVAILABLE',
    apply_from              DATE         NULL,
    apply_to                DATE         NULL,
    requested_by            VARCHAR(100) NULL,
    requested_at            DATETIME     NULL,
    approved_by             VARCHAR(100) NULL,
    approved_at             DATETIME     NULL,
    created_at              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (retail_plan_schedule_id),
    KEY idx_rps_status (schedule_status),
    KEY idx_rps_apply_range (apply_from, apply_to),
    CONSTRAINT fk_retail_plan_schedules_template
        FOREIGN KEY (plan_template_id) REFERENCES plan_templates(plan_template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE group_members (
    id                   BIGINT       NOT NULL AUTO_INCREMENT,
    group_id             BIGINT       NOT NULL,
    user_id              VARCHAR(100) NOT NULL,
    role                 VARCHAR(30)  NOT NULL DEFAULT 'MEMBER',
    joined_at            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    added_by             VARCHAR(100) NOT NULL,
    member_start_date    DATE         NULL,
    member_end_date      DATE         NULL,
    source_assignment_id BIGINT       NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_group_member (group_id, user_id),
    KEY idx_group_members_source_assignment (source_assignment_id),
    CONSTRAINT fk_group_members_group
        FOREIGN KEY (group_id) REFERENCES `groups`(group_id),
    CONSTRAINT fk_group_members_source_assignment
        FOREIGN KEY (source_assignment_id) REFERENCES group_plan_assignments(group_plan_assignment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE approval_requests (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    request_type     VARCHAR(50)  NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    requested_by     VARCHAR(100) NOT NULL,
    entity_type      VARCHAR(50)  NOT NULL,
    entity_id        VARCHAR(150) NOT NULL,
    request_payload  TEXT         NOT NULL,
    description      VARCHAR(500) NOT NULL,
    reviewed_by      VARCHAR(100) NULL,
    review_note      TEXT         NULL,
    reviewed_at      DATETIME     NULL,
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_approval_requests_status (status),
    KEY idx_approval_requests_requested_by (requested_by),
    KEY idx_approval_requests_entity (entity_type, entity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE subscriptions (
    subscription_id         BIGINT       NOT NULL AUTO_INCREMENT,
    subscriber_type         VARCHAR(20)  NOT NULL,
    user_id                 VARCHAR(100) NULL,
    group_id                BIGINT       NULL,
    plan_template_id        BIGINT       NOT NULL,
    pricing_rule_id         BIGINT       NULL,
    group_plan_assignment_id BIGINT      NULL,
    retail_plan_schedule_id BIGINT       NULL,
    source_type             VARCHAR(30)  NOT NULL DEFAULT 'MANUAL',
    source_id               BIGINT       NULL,
    status                  VARCHAR(30)  NOT NULL,
    start_date              DATE         NULL,
    end_date                DATE         NULL,
    signing_quota_total     INT          NOT NULL,
    signing_quota_used      INT          NOT NULL DEFAULT 0,
    activated_by            VARCHAR(100) NULL,
    payment_reference       VARCHAR(200) NULL,
    created_at              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (subscription_id),
    KEY idx_subscriptions_user_status (user_id, status),
    KEY idx_subscriptions_group_status (group_id, status),
    KEY idx_subscriptions_end_status (end_date, status),
    KEY idx_subscriptions_group_assignment (group_plan_assignment_id),
    KEY idx_subscriptions_retail_schedule (retail_plan_schedule_id),
    CONSTRAINT fk_subscriptions_group
        FOREIGN KEY (group_id) REFERENCES `groups`(group_id),
    CONSTRAINT fk_subscriptions_plan_template
        FOREIGN KEY (plan_template_id) REFERENCES plan_templates(plan_template_id),
    CONSTRAINT fk_subscriptions_pricing_rule
        FOREIGN KEY (pricing_rule_id) REFERENCES plan_pricing_rules(plan_pricing_rule_id),
    CONSTRAINT fk_subscriptions_group_plan_assignment
        FOREIGN KEY (group_plan_assignment_id) REFERENCES group_plan_assignments(group_plan_assignment_id),
    CONSTRAINT fk_subscriptions_retail_plan_schedule
        FOREIGN KEY (retail_plan_schedule_id) REFERENCES retail_plan_schedules(retail_plan_schedule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE certificate_provisioning_records (
    id                       BIGINT       NOT NULL AUTO_INCREMENT,
    subscription_id          BIGINT       NOT NULL,
    group_plan_assignment_id BIGINT       NULL,
    pricing_rule_id          BIGINT       NULL,
    user_id                  VARCHAR(100) NOT NULL,
    request_id               VARCHAR(36)  NOT NULL,
    status                   VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    cert_type                TINYINT      NOT NULL DEFAULT 1,
    certificate_id           VARCHAR(200) NULL,
    key_id                   VARCHAR(200) NULL,
    issued_at                DATETIME     NULL,
    expires_at               DATETIME     NULL,
    retry_count              INT          NOT NULL DEFAULT 0,
    usage_count              INT          NOT NULL DEFAULT 0,
    last_attempted_at        DATETIME     NULL,
    failure_reason           TEXT         NULL,
    created_at               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_certificate_provisioning_request_id (request_id),
    UNIQUE KEY uq_cert_sub_user (subscription_id, user_id),
    UNIQUE KEY uq_certificate_provisioning_certificate_id (certificate_id),
    KEY idx_cert_status_retry (status, retry_count),
    KEY idx_cert_subscription_id (subscription_id),
    CONSTRAINT fk_certificate_provisioning_subscription
        FOREIGN KEY (subscription_id) REFERENCES subscriptions(subscription_id),
    CONSTRAINT fk_certificate_provisioning_group_assignment
        FOREIGN KEY (group_plan_assignment_id) REFERENCES group_plan_assignments(group_plan_assignment_id),
    CONSTRAINT fk_certificate_provisioning_pricing_rule
        FOREIGN KEY (pricing_rule_id) REFERENCES plan_pricing_rules(plan_pricing_rule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE certificate_usage_records (
    id                       BIGINT       NOT NULL AUTO_INCREMENT,
    certificate_id           VARCHAR(200) NOT NULL,
    user_id                  VARCHAR(100) NOT NULL,
    subscription_id          BIGINT       NULL,
    group_plan_assignment_id BIGINT       NULL,
    usage_type               VARCHAR(30)  NOT NULL DEFAULT 'SIGNING',
    quantity                 INT          NOT NULL DEFAULT 1,
    used_at                  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_usage_cert_used (certificate_id, used_at),
    KEY idx_usage_user_used (user_id, used_at),
    KEY idx_usage_subscription_used (subscription_id, used_at),
    KEY idx_usage_assignment_used (group_plan_assignment_id, used_at),
    CONSTRAINT fk_certificate_usage_subscription
        FOREIGN KEY (subscription_id) REFERENCES subscriptions(subscription_id),
    CONSTRAINT fk_certificate_usage_group_assignment
        FOREIGN KEY (group_plan_assignment_id) REFERENCES group_plan_assignments(group_plan_assignment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE subscription_audit_logs (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    subscription_id  BIGINT       NOT NULL,
    actor            VARCHAR(100) NOT NULL,
    old_status       VARCHAR(50)  NULL,
    new_status       VARCHAR(50)  NOT NULL,
    reason           TEXT         NULL,
    source_type      VARCHAR(50)  NULL,
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_subscription_audit_logs_subscription (subscription_id, created_at),
    CONSTRAINT fk_subscription_audit_logs_subscription
        FOREIGN KEY (subscription_id) REFERENCES subscriptions(subscription_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE assignment_audits (
    assignment_audit_id      BIGINT       NOT NULL AUTO_INCREMENT,
    group_plan_assignment_id BIGINT       NULL,
    retail_plan_schedule_id  BIGINT       NULL,
    assignment_type          VARCHAR(30)  NOT NULL,
    action                   VARCHAR(30)  NOT NULL,
    old_status               VARCHAR(50)  NULL,
    new_status               VARCHAR(50)  NOT NULL,
    actor                    VARCHAR(100) NOT NULL,
    note                     TEXT         NULL,
    created_at               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (assignment_audit_id),
    KEY idx_assignment_audit_group_assignment (group_plan_assignment_id),
    KEY idx_assignment_audit_retail_schedule (retail_plan_schedule_id),
    CONSTRAINT fk_assignment_audits_group_assignment
        FOREIGN KEY (group_plan_assignment_id) REFERENCES group_plan_assignments(group_plan_assignment_id),
    CONSTRAINT fk_assignment_audits_retail_schedule
        FOREIGN KEY (retail_plan_schedule_id) REFERENCES retail_plan_schedules(retail_plan_schedule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE usage_aggregates (
    usage_aggregate_id    BIGINT        NOT NULL AUTO_INCREMENT,
    aggregate_scope       VARCHAR(30)   NOT NULL,
    scope_id              BIGINT        NOT NULL,
    period_type           VARCHAR(20)   NOT NULL,
    period_key            VARCHAR(20)   NOT NULL,
    certificates_created  INT           NOT NULL DEFAULT 0,
    signing_used          INT           NOT NULL DEFAULT 0,
    active_certificates   INT           NOT NULL DEFAULT 0,
    expired_certificates  INT           NOT NULL DEFAULT 0,
    revoked_certificates  INT           NOT NULL DEFAULT 0,
    amount_due            DECIMAL(15,2) NOT NULL DEFAULT 0,
    currency              CHAR(3)       NOT NULL DEFAULT 'VND',
    created_at            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (usage_aggregate_id),
    UNIQUE KEY uk_usage_aggregate_scope_period (aggregate_scope, scope_id, period_type, period_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE settlement_statements (
    settlement_statement_id BIGINT        NOT NULL AUTO_INCREMENT,
    group_id                BIGINT        NOT NULL,
    from_date               DATE          NULL,
    to_date                 DATE          NULL,
    status                  VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
    total_certificates      INT           NOT NULL DEFAULT 0,
    total_signings          INT           NOT NULL DEFAULT 0,
    total_amount            DECIMAL(15,2) NOT NULL DEFAULT 0,
    currency                CHAR(3)       NOT NULL DEFAULT 'VND',
    generated_at            DATETIME      NULL,
    generated_by            VARCHAR(100)  NULL,
    created_at              DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (settlement_statement_id),
    KEY idx_settlement_group_period (group_id, from_date, to_date),
    KEY idx_settlement_status (status),
    CONSTRAINT fk_settlement_statements_group
        FOREIGN KEY (group_id) REFERENCES `groups`(group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE payment_records (
    payment_id               BIGINT        NOT NULL AUTO_INCREMENT,
    subscription_id          BIGINT        NULL,
    group_plan_assignment_id BIGINT        NULL,
    settlement_statement_id  BIGINT        NULL,
    external_reference       VARCHAR(200)  NOT NULL,
    amount                   DECIMAL(12,2) NOT NULL,
    currency                 CHAR(3)       NOT NULL,
    payment_status           VARCHAR(30)   NOT NULL,
    payment_scope            VARCHAR(30)   NOT NULL,
    payment_method           VARCHAR(100)  NOT NULL,
    paid_at                  DATETIME      NOT NULL,
    raw_payload              JSON          NULL,
    PRIMARY KEY (payment_id),
    UNIQUE KEY uk_payment_records_external_reference (external_reference),
    KEY idx_payment_records_subscription_paid_at (subscription_id, paid_at),
    KEY idx_payment_records_group_assignment_paid_at (group_plan_assignment_id, paid_at),
    KEY idx_payment_records_settlement_paid_at (settlement_statement_id, paid_at),
    CONSTRAINT fk_payment_records_subscription
        FOREIGN KEY (subscription_id) REFERENCES subscriptions(subscription_id),
    CONSTRAINT fk_payment_records_group_assignment
        FOREIGN KEY (group_plan_assignment_id) REFERENCES group_plan_assignments(group_plan_assignment_id),
    CONSTRAINT fk_payment_records_settlement_statement
        FOREIGN KEY (settlement_statement_id) REFERENCES settlement_statements(settlement_statement_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO roles (role_id, role_name, display_name, description, is_system_role)
VALUES
    (1, 'ROLE_ADMIN', 'Administrator', 'System administrator', 1),
    (2, 'ROLE_USER', 'User', 'Default user role', 1);

INSERT INTO user_accounts (
    user_id,
    username,
    email,
    full_name,
    password_hash,
    auth_provider,
    status,
    failed_login_attempts,
    created_by
)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'admin',
    'admin@rs.local',
    'System Administrator',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5qXvS4p6UwHuu',
    'LOCAL',
    'ACTIVE',
    0,
    'SYSTEM'
);

INSERT INTO user_roles (user_id, role_id)
VALUES ('00000000-0000-0000-0000-000000000001', 1);
