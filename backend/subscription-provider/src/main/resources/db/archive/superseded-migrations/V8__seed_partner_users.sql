-- Seed 2 partner accounts for testing partner access management.
-- Password for both: Admin@123 (BCrypt cost=12)
INSERT INTO user_accounts (user_id, username, email, full_name, password_hash, auth_provider, status, failed_login_attempts, created_by)
VALUES
    (20, 'partner_abc', 'partner@abc.vn', 'Đối tác ABC',
     '$2a$12$u2sex9JV2pkYdLErDN64juv.4ACIftHIswis0HSwZHAL2vWR5F1Si',
     'LOCAL', 'ACTIVE', 0, 'SYSTEM'),
    (21, 'partner_xyz', 'partner@xyz.vn', 'Đối tác XYZ',
     '$2a$12$u2sex9JV2pkYdLErDN64juv.4ACIftHIswis0HSwZHAL2vWR5F1Si',
     'LOCAL', 'ACTIVE', 0, 'SYSTEM')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- Assign ROLE_PARTNER (role_id = 7) to both partner accounts.
INSERT IGNORE INTO user_roles (user_id, role_id)
VALUES (20, 7), (21, 7);
