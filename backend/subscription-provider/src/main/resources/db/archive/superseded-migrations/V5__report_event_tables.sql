-- ============================================================
-- V5: Event sources for report dashboard upload/auth metrics
-- ============================================================

CREATE TABLE IF NOT EXISTS document_upload_records (
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

CREATE TABLE IF NOT EXISTS certificate_auth_failure_records (
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

INSERT INTO permissions (permission_key, display_name, module_group, group_name, sort_order)
VALUES ('report:event:create', 'Ghi sự kiện báo cáo', 'BAO_CAO', 'REPORT_EVENT', 73)
ON DUPLICATE KEY UPDATE
    display_name = VALUES(display_name),
    module_group = VALUES(module_group),
    group_name = VALUES(group_name),
    sort_order = VALUES(sort_order);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
JOIN permissions p ON p.permission_key = 'report:event:create'
WHERE r.role_name = 'ROLE_ADMIN'
ON DUPLICATE KEY UPDATE permission_id = VALUES(permission_id);
