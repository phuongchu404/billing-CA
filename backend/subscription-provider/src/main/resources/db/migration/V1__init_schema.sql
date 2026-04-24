-- RS Subscription Management Service - Initial Schema
-- Version: 1.0

-- plans
CREATE TABLE plans (
    plan_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_code VARCHAR(50) NOT NULL UNIQUE,
    plan_name VARCHAR(150) NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'VND',
    validity_days INT NOT NULL,
    max_signing_quota INT NOT NULL,
    allow_bulk_signing BOOLEAN NOT NULL DEFAULT FALSE,
    allow_api_access BOOLEAN NOT NULL DEFAULT FALSE,
    is_group_plan BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- groups
CREATE TABLE `groups` (
    group_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_code VARCHAR(100) NOT NULL UNIQUE,
    group_name VARCHAR(200) NOT NULL,
    status ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_by VARCHAR(100) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- subscriptions
CREATE TABLE subscriptions (
    subscription_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subscriber_type ENUM('INDIVIDUAL','GROUP') NOT NULL,
    user_id VARCHAR(100) NULL,
    group_id BIGINT NULL,
    plan_id BIGINT NOT NULL,
    status ENUM('PENDING','ACTIVE','EXPIRED','CANCELLED','SUSPENDED') NOT NULL DEFAULT 'PENDING',
    start_date DATE NULL,
    end_date DATE NULL,
    signing_quota_total INT NOT NULL,
    signing_quota_used INT NOT NULL DEFAULT 0,
    activated_by VARCHAR(100) NULL,
    payment_reference VARCHAR(200) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_sub_plan FOREIGN KEY (plan_id) REFERENCES plans(plan_id),
    CONSTRAINT fk_sub_group FOREIGN KEY (group_id) REFERENCES `groups`(group_id),
    INDEX idx_sub_user_status (user_id, status),
    INDEX idx_sub_group_status (group_id, status),
    INDEX idx_sub_end_status (end_date, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- group_members
CREATE TABLE group_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    role ENUM('OPERATOR','MEMBER') NOT NULL DEFAULT 'MEMBER',
    joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    added_by VARCHAR(100) NOT NULL,
    CONSTRAINT fk_gm_group FOREIGN KEY (group_id) REFERENCES `groups`(group_id),
    UNIQUE KEY uq_group_member (group_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- payment_records
CREATE TABLE payment_records (
    payment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subscription_id BIGINT NOT NULL,
    external_reference VARCHAR(200) NOT NULL UNIQUE,
    amount DECIMAL(12,2) NOT NULL,
    currency CHAR(3) NOT NULL,
    payment_status ENUM('SUCCESS','FAILED','REFUNDED') NOT NULL,
    payment_method VARCHAR(100) NOT NULL,
    paid_at DATETIME NOT NULL,
    raw_payload JSON NULL,
    CONSTRAINT fk_pr_sub FOREIGN KEY (subscription_id) REFERENCES subscriptions(subscription_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- certificate_provisioning_records
CREATE TABLE certificate_provisioning_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subscription_id BIGINT NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    request_id VARCHAR(36) NOT NULL UNIQUE,
    status ENUM('PENDING','COMPLETED','FAILED','FAILED_PERMANENT') NOT NULL DEFAULT 'PENDING',
    certificate_id VARCHAR(200) NULL,
    key_id VARCHAR(200) NULL,
    issued_at DATETIME NULL,
    expires_at DATETIME NULL,
    retry_count INT NOT NULL DEFAULT 0,
    last_attempted_at DATETIME NULL,
    failure_reason TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cpr_sub FOREIGN KEY (subscription_id) REFERENCES subscriptions(subscription_id),
    UNIQUE KEY uq_cert_sub_user (subscription_id, user_id),
    INDEX idx_cert_status_retry (status, retry_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- subscription_audit_logs
CREATE TABLE subscription_audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subscription_id BIGINT NOT NULL,
    actor VARCHAR(100) NOT NULL,
    old_status VARCHAR(50) NULL,
    new_status VARCHAR(50) NOT NULL,
    reason TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_sub FOREIGN KEY (subscription_id) REFERENCES subscriptions(subscription_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Sample plans
INSERT INTO plans (plan_code, plan_name, price, currency, validity_days, max_signing_quota, allow_bulk_signing, allow_api_access, is_group_plan) VALUES
('RS-BASIC-30', 'Basic 30-Day', 99000.00, 'VND', 30, 50, FALSE, FALSE, FALSE),
('RS-PRO-365', 'Professional 365-Day', 990000.00, 'VND', 365, 500, TRUE, FALSE, FALSE),
('RS-ENT-365', 'Enterprise Group 365-Day', 4990000.00, 'VND', 365, 5000, TRUE, TRUE, TRUE);
