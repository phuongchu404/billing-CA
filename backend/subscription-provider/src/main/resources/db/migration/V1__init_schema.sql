-- ============================================================
-- V1: Complete initial schema
--
-- Tat ca bang, index, va seed data co ban o day.
-- V2/V5/V7 fixes are folded into this baseline for reset/new database runs.
-- Tuy chon: chay script partition trong db/optional neu can toi uu bang volume cao.
--
-- user_id dung BIGINT AUTO_INCREMENT cho khoa chinh va cac FK lien quan.
-- ============================================================

SET NAMES utf8mb4;

-- ============================================================
-- BANG HE THONG NGUOI DUNG
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
    created_by             VARCHAR(36)  NULL     COMMENT 'Username hoac SYSTEM - khong phai FK',
    manager_user_id        BIGINT   NULL     COMMENT 'Manager truc tiep (null = top-level)',
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
-- BANG CAU HINH VA LOG HE THONG
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
-- BANG KHACH HANG & GOI CUOC
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
    owner_user_id   BIGINT   NULL     COMMENT 'Nhan vien kinh doanh phu trach',
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
    partner_user_id BIGINT  NOT NULL COMMENT 'User co role ROLE_PARTNER',
    group_id        BIGINT      NOT NULL COMMENT 'Group duoc phep xem bao cao',
    granted_by      VARCHAR(36) NOT NULL COMMENT 'Username cua admin cap quyen',
    granted_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked_at      DATETIME    NULL     COMMENT 'NULL = con hieu luc',
    revoked_by      VARCHAR(36) NULL     COMMENT 'Username cua admin thu hoi quyen',
    PRIMARY KEY (id),
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
-- BANG PHE DUYET DA CAP
-- ============================================================

CREATE TABLE approval_level_configs (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    customer_segment VARCHAR(20)   NOT NULL COMMENT 'INDIVIDUAL | GROUP',
    min_value        DECIMAL(20,2) NULL     COMMENT 'null = khong gioi han duoi',
    max_value        DECIMAL(20,2) NULL     COMMENT 'null = khong gioi han tren',
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
    contract_value   DECIMAL(20,2) NULL     COMMENT 'Gia tri de tinh so cap duyet',
    total_levels     INT           NOT NULL DEFAULT 1,
    current_level    INT           NOT NULL DEFAULT 0,
    -- Legacy single-level fields (giu tuong thich)
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
-- BANG SUBSCRIPTION & CERTIFICATE
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
    -- FK bi drop o V6 neu ap dung partition
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Rollup theo ngay de tranh scan toan bang khi thong ke
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
-- BANG AUDIT & THONG KE
-- ============================================================

CREATE TABLE document_upload_records (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL,
    subscription_id BIGINT       NULL,
    certificate_id  VARCHAR(200) NULL,
    document_id     VARCHAR(200) NOT NULL,
    upload_status   VARCHAR(30)  NOT NULL DEFAULT 'SUCCESS',
    uploaded_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_dur_subscription_uploaded (subscription_id, uploaded_at),
    KEY idx_dur_user_uploaded         (user_id, uploaded_at),
    KEY idx_dur_certificate_uploaded  (certificate_id, uploaded_at),
    KEY idx_dur_status_uploaded       (upload_status, uploaded_at),
    CONSTRAINT fk_dur_subscription FOREIGN KEY (subscription_id) REFERENCES subscriptions(subscription_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE certificate_auth_failure_records (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL,
    subscription_id BIGINT       NULL,
    certificate_id  VARCHAR(200) NULL,
    failure_type    VARCHAR(30)  NOT NULL,
    reason_code     VARCHAR(100) NULL,
    failed_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_cafr_subscription_failed (subscription_id, failed_at),
    KEY idx_cafr_user_failed         (user_id, failed_at),
    KEY idx_cafr_certificate_failed  (certificate_id, failed_at),
    KEY idx_cafr_type_failed         (failure_type, failed_at),
    CONSTRAINT fk_cafr_subscription FOREIGN KEY (subscription_id) REFERENCES subscriptions(subscription_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
    -- FK bi drop o V6 neu ap dung partition
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
-- SEED DATA CO BAN
-- ============================================================

-- Roles ----------------------------------------------------
INSERT INTO roles (role_id, role_name, display_name, description, is_system_role) VALUES
    (1, 'ROLE_ADMIN',   'Administrator',  'System administrator', 1),
    (2, 'ROLE_USER',    'User',           'Default user role',    0),
    (3, 'ROLE_PARTNER', 'Doi tac',        'Tai khoan doi tac chi xem bao cao group duoc cap quyen', 0),
    (4, 'ROLE_MANAGER', 'Quan ly',        'Quan ly noi bo xem khach hang va bao cao cap duoi', 0);

-- Permissions ----------------------------------------------
INSERT INTO permissions (permission_id, permission_key, display_name, module_group, group_name, sort_order) VALUES
    (1,  'dashboard:view',              'Xem tong quan',                          'TRANG_CHU',            'DASHBOARD',        10),

    (2,  'plan:view',                   'Xem goi cuoc',                           'KHACH_HANG_PHO_THONG', 'PLAN',             20),
    (3,  'plan:create',                 'Tao goi cuoc',                           'KHACH_HANG_PHO_THONG', 'PLAN',             21),
    (4,  'plan:update',                 'Cap nhat goi cuoc',                      'KHACH_HANG_PHO_THONG', 'PLAN',             22),
    (5,  'plan:delete',                 'Xoa goi cuoc',                           'KHACH_HANG_PHO_THONG', 'PLAN',             23),
    (6,  'individual:usage:view',       'Xem theo doi su dung ca nhan',            'KHACH_HANG_PHO_THONG', 'INDIVIDUAL_USAGE', 24),

    (7,  'group:view',                  'Xem danh sach khach hang dai ly',         'KHACH_HANG_DAI_LY',    'AGENCY',           30),
    (8,  'group:create',                'Tao khach hang dai ly',                   'KHACH_HANG_DAI_LY',    'AGENCY',           31),
    (9,  'group:update',                'Cap nhat khach hang dai ly',              'KHACH_HANG_DAI_LY',    'AGENCY',           32),
    (10, 'subscription:view',           'Xem dang ky dich vu',                     'KHACH_HANG_DAI_LY',    'AGENCY',           33),
    (11, 'subscription:create',         'Tao dang ky dich vu',                     'KHACH_HANG_DAI_LY',    'AGENCY',           34),
    (12, 'subscription:update',         'Cap nhat dang ky dich vu',                'KHACH_HANG_DAI_LY',    'AGENCY',           35),
    (13, 'group:view:own',              'Xem khach hang cua minh',                 'KHACH_HANG_DAI_LY',    'DATA_SCOPE',       36),
    (14, 'group:view:subordinates',     'Xem khach hang cua cap duoi',             'KHACH_HANG_DAI_LY',    'DATA_SCOPE',       37),
    (15, 'group:assign:owner',          'Gan nhan vien phu trach',                 'KHACH_HANG_DAI_LY',    'DATA_SCOPE',       38),

    (16, 'user:view',                   'Xem danh sach nguoi dung',                'QUAN_LY_PHAN_QUYEN',   'USER',             50),
    (17, 'user:create',                 'Tao nguoi dung',                          'QUAN_LY_PHAN_QUYEN',   'USER',             51),
    (18, 'user:update',                 'Chinh sua nguoi dung',                    'QUAN_LY_PHAN_QUYEN',   'USER',             52),
    (19, 'user:delete',                 'Xoa nguoi dung',                          'QUAN_LY_PHAN_QUYEN',   'USER',             53),
    (20, 'role:view',                   'Xem danh sach vai tro',                   'QUAN_LY_PHAN_QUYEN',   'ROLE',             60),
    (21, 'role:create',                 'Tao vai tro',                             'QUAN_LY_PHAN_QUYEN',   'ROLE',             61),
    (22, 'role:update',                 'Chinh sua vai tro',                       'QUAN_LY_PHAN_QUYEN',   'ROLE',             62),
    (23, 'partner:access:grant',        'Cap quyen doi tac xem group',             'QUAN_LY_PHAN_QUYEN',   'PARTNER',          65),
    (24, 'partner:access:revoke',       'Thu hoi quyen doi tac',                   'QUAN_LY_PHAN_QUYEN',   'PARTNER',          66),

    (25, 'report:view',                 'Truy cap trang bao cao',                  'BAO_CAO',              'REPORT',           70),
    (26, 'report:group:view',           'Xem bao cao dai ly',                      'BAO_CAO',              'REPORT',           71),
    (27, 'report:individual:view',      'Xem bao cao pho thong',                   'BAO_CAO',              'REPORT',           72),
    (28, 'report:view:own',             'Xem bao cao cua khach hang minh',         'BAO_CAO',              'DATA_SCOPE',       86),
    (29, 'report:view:subordinates',    'Xem bao cao cua khach hang cap duoi',     'BAO_CAO',              'DATA_SCOPE',       87),
    (30, 'report:view:partner',         'Xem bao cao doi tac',                     'BAO_CAO',              'DATA_SCOPE',       88),
    (31, 'report:event:create',         'Ghi su kien bao cao',                     'BAO_CAO',              'REPORT_EVENT',     89),

    (32, 'audit-log:view',              'Xem nhat ky he thong',                    'QUAN_LY_LOGS',         'AUDIT_LOG',        80),

    (33, 'approval:view',               'Xem yeu cau phe duyet',                   'PHE_DUYET',            'APPROVAL',         92),
    (34, 'approval:level1',             'Truong phong kinh doanh',                 'PHE_DUYET',            'APPROVAL',         93),
    (35, 'approval:level2',             'CFO (Finance Manager)',                   'PHE_DUYET',            'APPROVAL',         94),
    (36, 'approval:level3',             'CEO',                                     'PHE_DUYET',            'APPROVAL',         95);

-- Role-permission mappings ---------------------------------
INSERT INTO role_permissions (role_id, permission_id) VALUES
    -- ROLE_ADMIN (1): toan quyen
    (1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),(1,9),(1,10),
    (1,11),(1,12),(1,13),(1,14),(1,15),(1,16),(1,17),(1,18),(1,19),(1,20),
    (1,21),(1,22),(1,23),(1,24),(1,25),(1,26),(1,27),(1,28),(1,29),(1,30),
    (1,31),(1,32),(1,33),(1,34),(1,35),(1,36),

    -- ROLE_USER (2): chi dashboard
    (2,1),

    -- ROLE_PARTNER (3): xem bao cao doi tac
    (3,1),(3,25),(3,26),(3,30),

    -- ROLE_MANAGER (4): quan ly noi bo
    (4,1),(4,7),(4,10),(4,13),(4,14),(4,16),(4,20),(4,25),(4,26),(4,27),(4,28),(4,29);

-- Admin user -----------------------------------------------
-- Mat khau: Admin@123 (BCrypt cost=12)
INSERT INTO user_accounts (user_id, username, email, full_name, password_hash, auth_provider, status, failed_login_attempts, created_by)
VALUES (
    1,
    'admin', 'admin@rs.local', 'System Administrator',
    '$2a$12$u2sex9JV2pkYdLErDN64juv.4ACIftHIswis0HSwZHAL2vWR5F1Si',
    'LOCAL', 'ACTIVE', 0, 'SYSTEM'
);

INSERT INTO user_roles (user_id, role_id)
VALUES (1, 1);

