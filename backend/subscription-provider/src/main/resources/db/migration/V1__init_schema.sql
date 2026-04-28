-- ============================================================
-- V1: Complete initial schema
--
-- Tất cả bảng, index, và seed data cơ bản ở đây.
-- Không cần chạy V5/V7/V8 — đã gộp vào file này.
-- Tùy chọn: chạy V6 (partition) để tối ưu bảng volume cao.
--
-- user_id dung BIGINT AUTO_INCREMENT cho khoa chinh va cac FK lien quan.
-- ============================================================

SET NAMES utf8mb4;

-- ============================================================
-- BẢNG HỆ THỐNG NGƯỜI DÙNG
-- ============================================================

CREATE TABLE user_accounts (
    user_id                BIGINT       NOT NULL AUTO_INCREMENT,
    username               VARCHAR(100) NOT NULL,
    email                  VARCHAR(200) NOT NULL,
    full_name              VARCHAR(200) NOT NULL,
    password_hash          VARCHAR(255) NOT NULL,
    auth_provider          VARCHAR(30)  NOT NULL DEFAULT 'LOCAL',
    status                 VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    failed_login_attempts  INT          NOT NULL DEFAULT 0,
    locked_until           DATETIME     NULL,
    last_login_at          DATETIME     NULL,
    created_by             VARCHAR(36)  NULL     COMMENT 'Username hoặc SYSTEM — không phải FK',
    manager_user_id        BIGINT   NULL     COMMENT 'Manager trực tiếp (null = top-level)',
    created_at             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_user_accounts_username (username),
    UNIQUE KEY uk_user_accounts_email    (email),
    KEY idx_user_accounts_manager        (manager_user_id),
    CONSTRAINT fk_user_accounts_manager
        FOREIGN KEY (manager_user_id) REFERENCES user_accounts(user_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE roles (
    role_id       BIGINT       NOT NULL AUTO_INCREMENT,
    role_name     VARCHAR(100) NOT NULL,
    display_name  VARCHAR(200) NOT NULL,
    description   TEXT         NULL,
    is_system_role TINYINT(1)  NOT NULL DEFAULT 0,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id),
    UNIQUE KEY uk_roles_role_name (role_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE permissions (
    permission_id  BIGINT       NOT NULL AUTO_INCREMENT,
    permission_key VARCHAR(100) NOT NULL,
    display_name   VARCHAR(200) NOT NULL,
    module_group   VARCHAR(100) NOT NULL,
    group_name     VARCHAR(100) NOT NULL,
    sort_order     INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (permission_id),
    UNIQUE KEY uk_permissions_permission_key (permission_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_roles (
    user_id                BIGINT     NOT NULL,
    role_id    BIGINT     NOT NULL,
    created_at DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES user_accounts(user_id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE role_permissions (
    id            BIGINT NOT NULL AUTO_INCREMENT,
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_permissions (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role       FOREIGN KEY (role_id)       REFERENCES roles(role_id),
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions(permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE refresh_tokens (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    token      VARCHAR(36)  NOT NULL,
    user_id                BIGINT       NOT NULL,
    status     VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    expires_at DATETIME     NOT NULL,
    issued_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked_at DATETIME     NULL,
    user_agent VARCHAR(500) NULL,
    ip_address VARCHAR(45)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_refresh_tokens_token           (token),
    KEY        idx_refresh_tokens_user_status    (user_id, status),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES user_accounts(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE password_history (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    user_id                BIGINT       NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_password_history_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE password_reset_tokens (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    token      VARCHAR(36) NOT NULL,
    user_id                BIGINT      NOT NULL,
    used       TINYINT(1)  NOT NULL DEFAULT 0,
    expires_at DATETIME    NOT NULL,
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_password_reset_tokens_token (token),
    KEY        idx_prt_user_expires           (user_id, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- BẢNG CẤU HÌNH VÀ LOG HỆ THỐNG
-- ============================================================

CREATE TABLE system_settings (
    setting_key   VARCHAR(100) NOT NULL,
    setting_value TEXT         NULL,
    setting_type  VARCHAR(20)  NOT NULL,
    category      VARCHAR(50)  NOT NULL,
    description   VARCHAR(300) NULL,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE admin_audit_logs (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    actor       VARCHAR(150) NOT NULL,
    action      VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50)  NOT NULL,
    entity_id   VARCHAR(150) NOT NULL,
    details     TEXT         NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_admin_audit_logs_actor      (actor),
    KEY idx_admin_audit_logs_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- BẢNG KHÁCH HÀNG & GÓI CƯỚC
-- ============================================================

CREATE TABLE `groups` (
    group_id        BIGINT       NOT NULL AUTO_INCREMENT,
    group_code      VARCHAR(100) NOT NULL,
    group_name      VARCHAR(200) NOT NULL,
    username        VARCHAR(100) NOT NULL,
    password        VARCHAR(255) NOT NULL,
    ref_contract_no VARCHAR(200) NULL,
    status          VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    created_by      VARCHAR(100) NOT NULL,
    owner_user_id   BIGINT   NULL     COMMENT 'Nhân viên kinh doanh phụ trách',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (group_id),
    UNIQUE KEY uk_groups_group_code (group_code),
    UNIQUE KEY uk_groups_username   (username),
    KEY        idx_groups_owner     (owner_user_id),
    CONSTRAINT fk_groups_owner
        FOREIGN KEY (owner_user_id) REFERENCES user_accounts(user_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE partner_group_access (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    partner_user_id BIGINT  NOT NULL COMMENT 'User có role ROLE_PARTNER',
    group_id        BIGINT      NOT NULL COMMENT 'Group được phép xem báo cáo',
    granted_by      VARCHAR(36) NOT NULL COMMENT 'Username của admin cấp quyền',
    granted_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked_at      DATETIME    NULL     COMMENT 'NULL = còn hiệu lực',
    PRIMARY KEY (id),
    UNIQUE KEY uk_partner_group (partner_user_id, group_id),
    KEY        idx_pga_partner  (partner_user_id),
    KEY        idx_pga_group    (group_id),
    CONSTRAINT fk_pga_partner FOREIGN KEY (partner_user_id) REFERENCES user_accounts(user_id),
    CONSTRAINT fk_pga_group   FOREIGN KEY (group_id)        REFERENCES `groups`(group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE plan_templates (
    plan_template_id        BIGINT       NOT NULL AUTO_INCREMENT,
    plan_code               VARCHAR(50)  NOT NULL,
    plan_name               VARCHAR(150) NOT NULL,
    description             TEXT         NULL,
    customer_segment        VARCHAR(30)  NOT NULL DEFAULT 'INDIVIDUAL',
    template_scope          VARCHAR(30)  NOT NULL DEFAULT 'PUBLIC',
    status                  VARCHAR(30)  NOT NULL DEFAULT 'DRAFT',
    effective_from          DATE         NULL,
    effective_to            DATE         NULL,
    is_visible              TINYINT(1)   NOT NULL DEFAULT 1,
    allow_bulk_signing      TINYINT(1)   NOT NULL DEFAULT 0,
    allow_api_access        TINYINT(1)   NOT NULL DEFAULT 0,
    created_by              VARCHAR(100) NULL,
    cloned_from_template_id BIGINT       NULL,
    version_no              INT          NOT NULL DEFAULT 1,
    created_at              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (plan_template_id),
    UNIQUE KEY uk_plan_templates_plan_code (plan_code),
    CONSTRAINT fk_plan_templates_cloned_from
        FOREIGN KEY (cloned_from_template_id) REFERENCES plan_templates(plan_template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE plan_pricing_rules (
    plan_pricing_rule_id       BIGINT        NOT NULL AUTO_INCREMENT,
    plan_template_id           BIGINT        NOT NULL,
    subject_type               VARCHAR(30)   NOT NULL,
    certificate_validity_value INT           NOT NULL,
    certificate_validity_unit  VARCHAR(20)   NOT NULL DEFAULT 'MONTH',
    pricing_metric             VARCHAR(30)   NOT NULL DEFAULT 'CERTIFICATE_COUNT',
    range_min                  INT           NOT NULL,
    range_max                  INT           NULL,
    unit_price                 DECIMAL(15,2) NOT NULL,
    currency                   CHAR(3)       NOT NULL DEFAULT 'VND',
    quota_total                INT           NULL,
    sort_order                 INT           NOT NULL DEFAULT 0,
    is_active                  TINYINT(1)    NOT NULL DEFAULT 1,
    created_at                 DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                 DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (plan_pricing_rule_id),
    KEY idx_ppr_template     (plan_template_id),
    KEY idx_ppr_active_sort  (plan_template_id, is_active, sort_order),
    CONSTRAINT fk_plan_pricing_rules_template
        FOREIGN KEY (plan_template_id) REFERENCES plan_templates(plan_template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE group_contacts (
    group_contact_id    BIGINT       NOT NULL AUTO_INCREMENT,
    group_id            BIGINT       NOT NULL,
    contact_type        VARCHAR(30)  NOT NULL,
    email               VARCHAR(200) NOT NULL,
    full_name           VARCHAR(200) NULL,
    phone               VARCHAR(50)  NULL,
    is_primary          TINYINT(1)   NOT NULL DEFAULT 0,
    receive_usage_alert TINYINT(1)   NOT NULL DEFAULT 0,
    is_active           TINYINT(1)   NOT NULL DEFAULT 1,
    created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (group_contact_id),
    UNIQUE KEY uk_group_contact_type_email (group_id, contact_type, email),
    CONSTRAINT fk_group_contacts_group FOREIGN KEY (group_id) REFERENCES `groups`(group_id)
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
    KEY idx_gpa_group_status   (group_id, assignment_status),
    KEY idx_gpa_apply_range    (apply_from, apply_to),
    KEY idx_gpa_status_apply_to (assignment_status, apply_to),
    CONSTRAINT fk_gpa_group    FOREIGN KEY (group_id)         REFERENCES `groups`(group_id),
    CONSTRAINT fk_gpa_template FOREIGN KEY (plan_template_id) REFERENCES plan_templates(plan_template_id)
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
    KEY idx_rps_status      (schedule_status),
    KEY idx_rps_apply_range (apply_from, apply_to),
    CONSTRAINT fk_rps_template FOREIGN KEY (plan_template_id) REFERENCES plan_templates(plan_template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE group_members (
    id                   BIGINT      NOT NULL AUTO_INCREMENT,
    group_id             BIGINT      NOT NULL,
    user_id                BIGINT      NOT NULL,
    role                 VARCHAR(30) NOT NULL DEFAULT 'MEMBER',
    joined_at            DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    added_by             VARCHAR(100) NOT NULL,
    member_start_date    DATE        NULL,
    member_end_date      DATE        NULL,
    source_assignment_id BIGINT      NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_group_member             (group_id, user_id),
    KEY        idx_group_members_source    (source_assignment_id),
    CONSTRAINT fk_group_members_group  FOREIGN KEY (group_id)             REFERENCES `groups`(group_id),
    CONSTRAINT fk_group_members_source FOREIGN KEY (source_assignment_id) REFERENCES group_plan_assignments(group_plan_assignment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- BẢNG PHÊ DUYỆT ĐA CẤP
-- ============================================================

CREATE TABLE approval_level_configs (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    customer_segment VARCHAR(20)   NOT NULL COMMENT 'INDIVIDUAL | GROUP',
    min_value        DECIMAL(20,2) NULL     COMMENT 'null = không giới hạn dưới',
    max_value        DECIMAL(20,2) NULL     COMMENT 'null = không giới hạn trên',
    required_levels  INT           NOT NULL,
    description      VARCHAR(200)  NULL,
    is_active        TINYINT(1)    NOT NULL DEFAULT 1,
    created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_alc_segment (customer_segment)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE approval_requests (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    request_type     VARCHAR(50)   NOT NULL,
    -- DRAFT | IN_APPROVAL | NEED_REVISION | APPROVED | REJECTED
    status           VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
    customer_segment VARCHAR(20)   NOT NULL DEFAULT 'INDIVIDUAL' COMMENT 'INDIVIDUAL | GROUP',
    requested_by     VARCHAR(100)  NOT NULL,
    entity_type      VARCHAR(50)   NOT NULL,
    entity_id        VARCHAR(150)  NOT NULL,
    request_payload  TEXT          NOT NULL,
    description      VARCHAR(500)  NOT NULL,
    contract_value   DECIMAL(20,2) NULL     COMMENT 'Giá trị để tính số cấp duyệt',
    total_levels     INT           NOT NULL DEFAULT 1,
    current_level    INT           NOT NULL DEFAULT 0,
    -- Legacy single-level fields (giữ tương thích)
    reviewed_by      VARCHAR(100)  NULL,
    review_note      TEXT          NULL,
    reviewed_at      DATETIME      NULL,
    created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_ar_status (status),
    KEY idx_ar_entity (entity_type, entity_id),
    KEY idx_ar_requested_by (requested_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE approval_request_steps (
    id                     BIGINT      NOT NULL AUTO_INCREMENT,
    approval_request_id    BIGINT      NOT NULL,
    step_level             INT         NOT NULL,
    required_approval_level VARCHAR(30) NOT NULL COMMENT 'LEVEL_1 | LEVEL_2 | LEVEL_3',
    -- PENDING | APPROVED | REJECTED | SKIPPED
    status                 VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    decided_by             VARCHAR(100) NULL,
    comment                TEXT        NULL,
    decided_at             DATETIME    NULL,
    created_at             DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_ars_request_level (approval_request_id, step_level),
    KEY idx_ars_status        (status),
    CONSTRAINT fk_ars_request FOREIGN KEY (approval_request_id) REFERENCES approval_requests(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- BẢNG SUBSCRIPTION & CERTIFICATE
-- ============================================================

CREATE TABLE subscriptions (
    subscription_id          BIGINT        NOT NULL AUTO_INCREMENT,
    subscriber_type          VARCHAR(30)   NOT NULL,
    user_id                  BIGINT    NULL,
    group_id                 BIGINT        NULL,
    plan_template_id         BIGINT        NOT NULL,
    pricing_rule_id          BIGINT        NULL,
    group_plan_assignment_id BIGINT        NULL,
    retail_plan_schedule_id  BIGINT        NULL,
    source_type              VARCHAR(30)   NOT NULL DEFAULT 'MANUAL',
    source_id                BIGINT        NULL,
    status                   VARCHAR(30)   NOT NULL DEFAULT 'PENDING',
    start_date               DATE          NULL,
    end_date                 DATE          NULL,
    signing_quota_total      INT           NOT NULL,
    signing_quota_used       INT           NOT NULL DEFAULT 0,
    activated_by             VARCHAR(100)  NULL,
    payment_reference        VARCHAR(200)  NULL,
    created_at               DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (subscription_id),
    KEY idx_subscriptions_user_status       (user_id, status),
    KEY idx_subscriptions_group_status      (group_id, status),
    KEY idx_subscriptions_end_status        (end_date, status),
    KEY idx_subscriptions_group_assignment  (group_plan_assignment_id),
    KEY idx_subscriptions_retail_schedule   (retail_plan_schedule_id),
    KEY idx_subscriptions_type_status       (subscriber_type, status),
    KEY idx_subscriptions_type_created      (subscriber_type, created_at),
    CONSTRAINT fk_subscriptions_group              FOREIGN KEY (group_id)                 REFERENCES `groups`(group_id),
    CONSTRAINT fk_subscriptions_plan_template      FOREIGN KEY (plan_template_id)         REFERENCES plan_templates(plan_template_id),
    CONSTRAINT fk_subscriptions_pricing_rule       FOREIGN KEY (pricing_rule_id)          REFERENCES plan_pricing_rules(plan_pricing_rule_id),
    CONSTRAINT fk_subscriptions_group_assignment   FOREIGN KEY (group_plan_assignment_id) REFERENCES group_plan_assignments(group_plan_assignment_id),
    CONSTRAINT fk_subscriptions_retail_schedule    FOREIGN KEY (retail_plan_schedule_id)  REFERENCES retail_plan_schedules(retail_plan_schedule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE certificate_provisioning_records (
    id                       BIGINT       NOT NULL AUTO_INCREMENT,
    subscription_id          BIGINT       NOT NULL,
    group_plan_assignment_id BIGINT       NULL,
    pricing_rule_id          BIGINT       NULL,
    user_id                BIGINT       NOT NULL,
    request_id               VARCHAR(36)  NOT NULL,
    status                   VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    cert_type                TINYINT      NOT NULL DEFAULT 1 COMMENT '1=INDIVIDUAL 2=INDIVIDUAL_OF_ORG 3=ORGANIZATION',
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
    UNIQUE KEY uk_cpr_request_id    (request_id),
    UNIQUE KEY uk_cpr_cert_user     (subscription_id, user_id),
    UNIQUE KEY uk_cpr_certificate   (certificate_id),
    KEY idx_cpr_status_retry        (status, retry_count),
    KEY idx_cpr_subscription        (subscription_id),
    KEY idx_cpr_assignment_status   (group_plan_assignment_id, status, issued_at),
    KEY idx_cpr_issued_status       (issued_at, status),
    CONSTRAINT fk_cpr_subscription  FOREIGN KEY (subscription_id)          REFERENCES subscriptions(subscription_id),
    CONSTRAINT fk_cpr_assignment    FOREIGN KEY (group_plan_assignment_id) REFERENCES group_plan_assignments(group_plan_assignment_id),
    CONSTRAINT fk_cpr_pricing_rule  FOREIGN KEY (pricing_rule_id)          REFERENCES plan_pricing_rules(plan_pricing_rule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE certificate_usage_records (
    id                       BIGINT       NOT NULL AUTO_INCREMENT,
    certificate_id           VARCHAR(200) NOT NULL,
    user_id                BIGINT       NOT NULL,
    subscription_id          BIGINT       NULL,
    group_plan_assignment_id BIGINT       NULL,
    usage_type               VARCHAR(30)  NOT NULL DEFAULT 'SIGNING',
    quantity                 INT          NOT NULL DEFAULT 1,
    used_at                  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_usage_cert_used         (certificate_id, used_at),
    KEY idx_usage_user_used         (user_id, used_at),
    KEY idx_usage_subscription_used (subscription_id, used_at),
    KEY idx_usage_assignment_used   (group_plan_assignment_id, used_at),
    KEY idx_usage_type_used_at      (usage_type, used_at)
    -- FK bị drop ở V6 nếu áp dụng partition
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Rollup theo ngày để tránh scan toàn bảng khi thống kê
CREATE TABLE certificate_usage_daily (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    certificate_id VARCHAR(200) NOT NULL,
    usage_date     DATE         NOT NULL,
    usage_count    INT          NOT NULL DEFAULT 0,
    distinct_users INT          NOT NULL DEFAULT 0,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cert_daily      (certificate_id, usage_date),
    KEY        idx_cert_daily_date (usage_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- BẢNG AUDIT & THỐNG KÊ
-- ============================================================

CREATE TABLE subscription_audit_logs (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    subscription_id BIGINT       NOT NULL,
    actor           VARCHAR(100) NOT NULL,
    old_status      VARCHAR(50)  NULL,
    new_status      VARCHAR(50)  NOT NULL,
    reason          TEXT         NULL,
    source_type     VARCHAR(50)  NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_sub_audit_subscription (subscription_id, created_at)
    -- FK bị drop ở V6 nếu áp dụng partition
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
    KEY idx_aa_group_assignment  (group_plan_assignment_id),
    KEY idx_aa_retail_schedule   (retail_plan_schedule_id),
    CONSTRAINT fk_aa_group_assignment FOREIGN KEY (group_plan_assignment_id) REFERENCES group_plan_assignments(group_plan_assignment_id),
    CONSTRAINT fk_aa_retail_schedule  FOREIGN KEY (retail_plan_schedule_id)  REFERENCES retail_plan_schedules(retail_plan_schedule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE usage_aggregates (
    usage_aggregate_id   BIGINT        NOT NULL AUTO_INCREMENT,
    aggregate_scope      VARCHAR(30)   NOT NULL,
    scope_id             BIGINT        NOT NULL,
    period_type          VARCHAR(20)   NOT NULL,
    period_key           VARCHAR(20)   NOT NULL,
    certificates_created INT           NOT NULL DEFAULT 0,
    signing_used         INT           NOT NULL DEFAULT 0,
    active_certificates  INT           NOT NULL DEFAULT 0,
    expired_certificates INT           NOT NULL DEFAULT 0,
    revoked_certificates INT           NOT NULL DEFAULT 0,
    amount_due           DECIMAL(15,2) NOT NULL DEFAULT 0,
    currency             CHAR(3)       NOT NULL DEFAULT 'VND',
    created_at           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (usage_aggregate_id),
    UNIQUE KEY uk_usage_aggregate_scope_period (aggregate_scope, scope_id, period_type, period_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE usage_aggregate_rollup_log (
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    period_key     VARCHAR(20) NOT NULL,
    run_at         DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    groups_updated INT         NOT NULL DEFAULT 0,
    status         VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    error_msg      TEXT        NULL,
    PRIMARY KEY (id),
    KEY idx_rollup_log_period (period_key),
    KEY idx_rollup_log_run_at (run_at)
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
    KEY idx_settlement_status       (status),
    CONSTRAINT fk_settlement_group FOREIGN KEY (group_id) REFERENCES `groups`(group_id)
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
    UNIQUE KEY uk_payment_external           (external_reference),
    KEY        idx_payment_subscription      (subscription_id, paid_at),
    KEY        idx_payment_group_assignment  (group_plan_assignment_id, paid_at),
    KEY        idx_payment_settlement        (settlement_statement_id, paid_at),
    CONSTRAINT fk_payment_subscription  FOREIGN KEY (subscription_id)          REFERENCES subscriptions(subscription_id),
    CONSTRAINT fk_payment_assignment    FOREIGN KEY (group_plan_assignment_id)  REFERENCES group_plan_assignments(group_plan_assignment_id),
    CONSTRAINT fk_payment_settlement    FOREIGN KEY (settlement_statement_id)   REFERENCES settlement_statements(settlement_statement_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- SEED DATA CƠ BẢN
-- ============================================================

-- ── Roles ────────────────────────────────────────────────────
INSERT INTO roles (role_id, role_name, display_name, description, is_system_role) VALUES
    (1, 'ROLE_ADMIN',   'Administrator',  'System administrator',                                    1),
    (2, 'ROLE_USER',    'User',           'Default user role',                                       1),
    (3, 'ROLE_LEVEL_1', 'Vai trò cấp 1', 'Quản trị cấp 1',                                          0),
    (4, 'ROLE_LEVEL_2', 'Vai trò cấp 2', 'Quản trị cấp 2',                                          0),
    (5, 'ROLE_LEVEL_3', 'Vai trò cấp 3', 'Vận hành cấp 3',                                          0),
    (6, 'ROLE_LEVEL_4', 'Vai trò cấp 4', 'Xem báo cáo cấp 4',                                       0),
    (7, 'ROLE_PARTNER', 'Đối tác',        'Tài khoản đối tác — chỉ xem báo cáo group được cấp quyền', 0),
    (8, 'ROLE_MANAGER', 'Quản lý',        'Quản lý nội bộ — xem khách hàng và báo cáo cấp dưới',    0);

-- ── Permissions ──────────────────────────────────────────────
-- ID | permission_key               | Module                | Group
-- ── TRANG CHỦ ────────────────────────────────────────────────
-- 1    dashboard:view
-- ── KHÁCH HÀNG PHỔ THÔNG ─────────────────────────────────────
-- 2-5  plan:view/create/update/delete
-- 6    individual:usage:view
-- ── KHÁCH HÀNG ĐẠI LÝ ────────────────────────────────────────
-- 7-9  group:view/create/update
-- 10-12 subscription:view/create/update
-- ── QUẢN LÝ PHÂN QUYỀN ───────────────────────────────────────
-- 13-15 user:view/create/update
-- 16-18 role:view/create/update
-- ── BÁO CÁO ──────────────────────────────────────────────────
-- 19  report:view (vào trang báo cáo)
-- 20  report:group:view (tab đại lý)
-- 21  report:individual:view (tab phổ thông)
-- ── LOGS ─────────────────────────────────────────────────────
-- 22  audit-log:view
-- ── PHÂN QUYỀN DỮ LIỆU ───────────────────────────────────────
-- 23-25 group:view:own / subordinates / assign:owner
-- 26-28 report:view:own / subordinates / partner
-- 29-30 partner:access:grant / revoke
INSERT INTO permissions (permission_id, permission_key, display_name, module_group, group_name, sort_order) VALUES
    (1,  'dashboard:view',              'Xem tổng quan',                        'TRANG_CHU',            'DASHBOARD',        10),
    (2,  'plan:view',                   'Xem gói cước',                         'KHACH_HANG_PHO_THONG', 'PLAN',             20),
    (3,  'plan:create',                 'Tạo gói cước',                         'KHACH_HANG_PHO_THONG', 'PLAN',             21),
    (4,  'plan:update',                 'Cập nhật gói cước',                    'KHACH_HANG_PHO_THONG', 'PLAN',             22),
    (5,  'plan:delete',                 'Xoá gói cước',                         'KHACH_HANG_PHO_THONG', 'PLAN',             23),
    (6,  'individual:usage:view',       'Xem theo dõi sử dụng cá nhân',         'KHACH_HANG_PHO_THONG', 'INDIVIDUAL_USAGE', 24),
    (7,  'group:view',                  'Xem danh sách khách hàng đại lý',      'KHACH_HANG_DAI_LY',    'AGENCY',           30),
    (8,  'group:create',                'Tạo khách hàng đại lý',                'KHACH_HANG_DAI_LY',    'AGENCY',           31),
    (9,  'group:update',                'Cập nhật khách hàng đại lý',           'KHACH_HANG_DAI_LY',    'AGENCY',           32),
    (10, 'subscription:view',           'Xem đăng ký dịch vụ',                  'KHACH_HANG_DAI_LY',    'AGENCY',           33),
    (11, 'subscription:create',         'Tạo đăng ký dịch vụ',                  'KHACH_HANG_DAI_LY',    'AGENCY',           34),
    (12, 'subscription:update',         'Cập nhật đăng ký dịch vụ',             'KHACH_HANG_DAI_LY',    'AGENCY',           35),
    (13, 'user:view',                   'Xem danh sách người dùng',             'QUAN_LY_PHAN_QUYEN',   'USER',             50),
    (14, 'user:create',                 'Tạo người dùng',                       'QUAN_LY_PHAN_QUYEN',   'USER',             51),
    (15, 'user:update',                 'Chỉnh sửa người dùng',                 'QUAN_LY_PHAN_QUYEN',   'USER',             52),
    (16, 'role:view',                   'Xem danh sách vai trò',                'QUAN_LY_PHAN_QUYEN',   'ROLE',             60),
    (17, 'role:create',                 'Tạo vai trò',                          'QUAN_LY_PHAN_QUYEN',   'ROLE',             61),
    (18, 'role:update',                 'Chỉnh sửa vai trò',                    'QUAN_LY_PHAN_QUYEN',   'ROLE',             62),
    (19, 'report:view',                 'Truy cập trang báo cáo',               'BAO_CAO',              'REPORT',           70),
    (20, 'report:group:view',           'Xem báo cáo đại lý',                   'BAO_CAO',              'REPORT',           71),
    (21, 'report:individual:view',      'Xem báo cáo phổ thông',                'BAO_CAO',              'REPORT',           72),
    (22, 'audit-log:view',              'Xem nhật ký hệ thống',                 'QUAN_LY_LOGS',         'AUDIT_LOG',        80),
    (23, 'group:view:own',              'Xem khách hàng của mình',              'KHACH_HANG_DAI_LY',    'DATA_SCOPE',       83),
    (24, 'group:view:subordinates',     'Xem khách hàng của cấp dưới',          'KHACH_HANG_DAI_LY',    'DATA_SCOPE',       84),
    (25, 'group:assign:owner',          'Gán nhân viên phụ trách',              'KHACH_HANG_DAI_LY',    'DATA_SCOPE',       85),
    (26, 'report:view:own',             'Xem báo cáo của khách hàng mình',      'BAO_CAO',              'DATA_SCOPE',       86),
    (27, 'report:view:subordinates',    'Xem báo cáo của khách hàng cấp dưới',  'BAO_CAO',              'DATA_SCOPE',       87),
    (28, 'report:view:partner',         'Xem báo cáo đối tác (self-service)',   'BAO_CAO',              'DATA_SCOPE',       88),
    (29, 'partner:access:grant',        'Cấp quyền đối tác xem group',          'HE_THONG',             'PARTNER',          90),
    (30, 'partner:access:revoke',       'Thu hồi quyền đối tác',                'HE_THONG',             'PARTNER',          91);

-- ── Role-permission mappings ─────────────────────────────────
INSERT INTO role_permissions (role_id, permission_id) VALUES
    -- ROLE_ADMIN (1): toàn quyền
    (1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),(1,9),(1,10),
    (1,11),(1,12),(1,13),(1,14),(1,15),(1,16),(1,17),(1,18),(1,19),(1,20),
    (1,21),(1,22),(1,23),(1,24),(1,25),(1,26),(1,27),(1,28),(1,29),(1,30),

    -- ROLE_USER (2): chỉ dashboard
    (2,1),

    -- ROLE_LEVEL_1 (3): quản trị — cả 2 tab báo cáo
    (3,1),(3,2),(3,3),(3,6),(3,7),(3,13),(3,16),(3,19),(3,20),(3,21),(3,22),(3,23),(3,26),

    -- ROLE_LEVEL_2 (4): phổ thông — chỉ tab phổ thông
    (4,1),(4,2),(4,3),(4,6),(4,19),(4,21),

    -- ROLE_LEVEL_3 (5): vận hành — không có báo cáo
    (5,1),(5,2),(5,3),(5,4),(5,5),(5,6),(5,7),(5,8),(5,9),(5,13),(5,16),

    -- ROLE_LEVEL_4 (6): chỉ xem & tạo gói
    (6,2),(6,3),

    -- ROLE_PARTNER (7): xem báo cáo đối tác
    (7,1),(7,19),(7,20),(7,28),

    -- ROLE_MANAGER (8): quản lý nội bộ
    (8,1),(8,7),(8,10),(8,13),(8,16),(8,19),(8,20),(8,21),(8,23),(8,24),(8,26),(8,27);

-- ── Admin user ───────────────────────────────────────────────
-- Mật khẩu: Admin@123 (BCrypt cost=12)
INSERT INTO user_accounts (user_id, username, email, full_name, password_hash, auth_provider, status, failed_login_attempts, created_by)
VALUES (
    1,
    'admin', 'admin@rs.local', 'System Administrator',
    '$2a$12$u2sex9JV2pkYdLErDN64juv.4ACIftHIswis0HSwZHAL2vWR5F1Si',
    'LOCAL', 'ACTIVE', 0, 'SYSTEM'
);

INSERT INTO user_roles (user_id, role_id)
VALUES (1, 1);


