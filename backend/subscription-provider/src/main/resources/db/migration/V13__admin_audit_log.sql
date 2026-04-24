CREATE TABLE admin_audit_logs (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor       VARCHAR(150) NOT NULL,
    action      VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50)  NOT NULL,
    entity_id   VARCHAR(150) NOT NULL,
    details     TEXT,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_admin_audit_actor       (actor),
    INDEX idx_admin_audit_action      (action),
    INDEX idx_admin_audit_entity_type (entity_type),
    INDEX idx_admin_audit_entity_id   (entity_id),
    INDEX idx_admin_audit_created_at  (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
