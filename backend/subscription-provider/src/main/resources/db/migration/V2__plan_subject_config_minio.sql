-- ============================================================
-- V2: Per-subject display config (icon, features, price) for plan templates
--     + MinIO orphan tracking for cleanup job
-- ============================================================

-- Per-subject display metadata for each plan template
-- Stores icon URL (MinIO), features list (newline-separated), and display price string
CREATE TABLE IF NOT EXISTS plan_subject_config (
    id                 BIGINT        NOT NULL AUTO_INCREMENT,
    plan_template_id   BIGINT        NOT NULL,
    subject_type       VARCHAR(30)   NOT NULL COMMENT 'INDIVIDUAL | ORGANIZATION | INDIVIDUAL_OF_ORG',
    icon_url           VARCHAR(500)  NULL     COMMENT 'MinIO storagePath, e.g. public/images/plans/2026/05/12/uuid.png',
    features_text      TEXT          NULL     COMMENT 'Newline-separated list of feature strings shown in public price card',
    PRIMARY KEY (id),
    UNIQUE KEY uq_psc_template_subject (plan_template_id, subject_type),
    CONSTRAINT fk_psc_plan_template FOREIGN KEY (plan_template_id)
        REFERENCES plan_templates (plan_template_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- MinIO orphan tracking: images uploaded but not yet confirmed as attached to an entity
-- Cleanup job deletes unconfirmed rows older than grace period
CREATE TABLE IF NOT EXISTS minio_orphan_tracking (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    bucket      VARCHAR(100)  NOT NULL,
    object_name VARCHAR(1000) NOT NULL,
    uploaded_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed   TINYINT(1)    NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_mot_cleanup (confirmed, uploaded_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
