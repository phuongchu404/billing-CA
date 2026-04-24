CREATE TABLE approval_requests (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    request_type VARCHAR(50)  NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    requested_by VARCHAR(100) NOT NULL,
    request_payload TEXT       NOT NULL,
    description  VARCHAR(500) NOT NULL,
    reviewed_by  VARCHAR(100) NULL,
    review_note  TEXT         NULL,
    reviewed_at  DATETIME     NULL,
    created_at   DATETIME     NOT NULL,
    updated_at   DATETIME     NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_approval_status (status),
    INDEX idx_approval_requested_by (requested_by)
) ENGINE=InnoDB;

-- Permissions
INSERT INTO permissions (permission_key, display_name, module_group, group_name, sort_order)
VALUES
    ('approval:view',   'View Approval Requests',  'SUBSCRIPTION_MANAGEMENT', 'APPROVAL', 1000),
    ('approval:review', 'Approve / Deny Requests', 'SUBSCRIPTION_MANAGEMENT', 'APPROVAL', 1010),
    ('approval:submit', 'Submit Approval Requests','SUBSCRIPTION_MANAGEMENT', 'APPROVAL', 1020);

-- Grant all three to ROLE_ADMIN (role_id = 1)
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1, permission_id FROM permissions
WHERE permission_key IN ('approval:view', 'approval:review', 'approval:submit');

-- Grant view + submit to ROLE_OPERATOR
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r, permissions p
WHERE r.role_name = 'ROLE_OPERATOR'
  AND p.permission_key IN ('approval:view', 'approval:submit');
