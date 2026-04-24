-- RS Subscription Management Service - Auth Schema
-- Version: 1.1

-- roles
CREATE TABLE roles (
    role_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(100) NOT NULL UNIQUE,
    display_name VARCHAR(200) NOT NULL,
    description TEXT NULL,
    is_system_role BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- user_accounts
CREATE TABLE user_accounts (
    user_id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(200) NOT NULL UNIQUE,
    full_name VARCHAR(200) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    auth_provider ENUM('LOCAL','SSO') NOT NULL DEFAULT 'LOCAL',
    status ENUM('ACTIVE','INACTIVE','LOCKED') NOT NULL DEFAULT 'ACTIVE',
    failed_login_attempts INT NOT NULL DEFAULT 0,
    locked_until DATETIME NULL,
    last_login_at DATETIME NULL,
    created_by VARCHAR(36) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- user_roles
CREATE TABLE user_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_by VARCHAR(36) NOT NULL,
    assigned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES user_accounts(user_id),
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles(role_id),
    UNIQUE KEY uq_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- refresh_tokens
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(36) NOT NULL UNIQUE,
    user_id VARCHAR(36) NOT NULL,
    status ENUM('ACTIVE','REVOKED','EXPIRED') NOT NULL DEFAULT 'ACTIVE',
    expires_at DATETIME NOT NULL,
    issued_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked_at DATETIME NULL,
    user_agent VARCHAR(500) NULL,
    ip_address VARCHAR(45) NULL,
    CONSTRAINT fk_rt_user FOREIGN KEY (user_id) REFERENCES user_accounts(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- auth_audit_log
CREATE TABLE auth_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(36) NULL,
    username VARCHAR(100) NOT NULL,
    event_type ENUM('LOGIN_SUCCESS','LOGIN_FAILURE','LOGOUT','PASSWORD_CHANGE','PASSWORD_RESET','ACCOUNT_LOCKED') NOT NULL,
    ip_address VARCHAR(45) NULL,
    user_agent VARCHAR(500) NULL,
    details TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- password_history
CREATE TABLE password_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ph_user FOREIGN KEY (user_id) REFERENCES user_accounts(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- password_reset_tokens
CREATE TABLE password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(36) NOT NULL UNIQUE,
    user_id VARCHAR(36) NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES user_accounts(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- System roles
INSERT INTO roles (role_name, display_name, description, is_system_role) VALUES
('ROLE_ADMIN', 'System Administrator', 'Full access: manage plans, all subscriptions, all groups, all users, all reports, certificate retry, role management.', TRUE),
('ROLE_OPERATOR', 'Group Operator', 'Manage own group members, view own group subscription and usage, assign plans to own group.', TRUE),
('ROLE_USER', 'Standard User', 'View own subscription, initiate individual purchase, request own certificate.', TRUE);

-- Default admin user (password: Admin@2026)
-- BCrypt hash for "Admin@2026" with strength 12
INSERT INTO user_accounts (user_id, username, email, full_name, password_hash, auth_provider, status, created_by) VALUES
('00000000-0000-0000-0000-000000000001', 'admin', 'admin@rs.local', 'System Administrator',
 '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5qXvS4p6UwHuu', 'LOCAL', 'ACTIVE',
 '00000000-0000-0000-0000-000000000001');

INSERT INTO user_roles (user_id, role_id, assigned_by) VALUES
('00000000-0000-0000-0000-000000000001', 1, '00000000-0000-0000-0000-000000000001');
