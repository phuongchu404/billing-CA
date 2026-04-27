-- ============================================================
-- SEED: Thêm users demo và role-permissions đầy đủ
--
-- Chạy SAU V1__init_schema.sql.
-- Permissions và roles đã được seed trong V1.
-- Mật khẩu mặc định: Admin@123 (BCrypt cost=12)
--
-- Vì user_id là BINARY(16):
--   INSERT dùng UNHEX(REPLACE('<uuid>', '-', ''))
--   SELECT dùng BIN_TO_UUID(user_id)
-- ============================================================

SET NAMES utf8mb4;

-- Shorthand macro để đọc dễ hơn
-- UNHEX(REPLACE('<uuid>', '-', '')) = 16-byte binary

-- ============================================================
-- 1. USERS (admin user 001 đã có trong V1)
-- ============================================================
INSERT INTO user_accounts (user_id, username, email, full_name, password_hash, auth_provider, status, failed_login_attempts, created_by)
VALUES
    (UNHEX(REPLACE('00000000-0000-0000-0000-000000000002', '-', '')), 'billing_admin',    'billing_admin@rs.local',  'Nguyễn Thị Bình',   '$2a$12$u2sex9JV2pkYdLErDN64juv.4ACIftHIswis0HSwZHAL2vWR5F1Si', 'LOCAL', 'ACTIVE',   0, '00000000-0000-0000-0000-000000000001'),
    (UNHEX(REPLACE('00000000-0000-0000-0000-000000000003', '-', '')), 'group_admin',      'group_admin@rs.local',    'Trần Văn Cường',     '$2a$12$u2sex9JV2pkYdLErDN64juv.4ACIftHIswis0HSwZHAL2vWR5F1Si', 'LOCAL', 'ACTIVE',   0, '00000000-0000-0000-0000-000000000001'),
    (UNHEX(REPLACE('00000000-0000-0000-0000-000000000004', '-', '')), 'operator1',        'operator1@rs.local',      'Lê Minh Đức',        '$2a$12$u2sex9JV2pkYdLErDN64juv.4ACIftHIswis0HSwZHAL2vWR5F1Si', 'LOCAL', 'ACTIVE',   0, '00000000-0000-0000-0000-000000000001'),
    (UNHEX(REPLACE('00000000-0000-0000-0000-000000000005', '-', '')), 'operator2',        'operator2@rs.local',      'Phạm Thị Hoa',       '$2a$12$u2sex9JV2pkYdLErDN64juv.4ACIftHIswis0HSwZHAL2vWR5F1Si', 'LOCAL', 'ACTIVE',   0, '00000000-0000-0000-0000-000000000001'),
    (UNHEX(REPLACE('00000000-0000-0000-0000-000000000006', '-', '')), 'viewer1',          'viewer1@rs.local',        'Hoàng Anh Tuấn',     '$2a$12$u2sex9JV2pkYdLErDN64juv.4ACIftHIswis0HSwZHAL2vWR5F1Si', 'LOCAL', 'ACTIVE',   0, '00000000-0000-0000-0000-000000000001'),
    (UNHEX(REPLACE('00000000-0000-0000-0000-000000000007', '-', '')), 'individual_user1', 'iuser1@customer.local',   'Nguyễn Văn An',      '$2a$12$u2sex9JV2pkYdLErDN64juv.4ACIftHIswis0HSwZHAL2vWR5F1Si', 'LOCAL', 'ACTIVE',   0, '00000000-0000-0000-0000-000000000001'),
    (UNHEX(REPLACE('00000000-0000-0000-0000-000000000008', '-', '')), 'individual_user2', 'iuser2@customer.local',   'Trần Thị Bảo',       '$2a$12$u2sex9JV2pkYdLErDN64juv.4ACIftHIswis0HSwZHAL2vWR5F1Si', 'LOCAL', 'ACTIVE',   0, '00000000-0000-0000-0000-000000000001'),
    (UNHEX(REPLACE('00000000-0000-0000-0000-000000000009', '-', '')), 'individual_user3', 'iuser3@customer.local',   'Lý Thành Công',      '$2a$12$u2sex9JV2pkYdLErDN64juv.4ACIftHIswis0HSwZHAL2vWR5F1Si', 'LOCAL', 'INACTIVE', 0, '00000000-0000-0000-0000-000000000001'),
    (UNHEX(REPLACE('00000000-0000-0000-0000-000000000010', '-', '')), 'group_member1',    'gmember1@abc.com',        'Phan Thị Dung',      '$2a$12$u2sex9JV2pkYdLErDN64juv.4ACIftHIswis0HSwZHAL2vWR5F1Si', 'LOCAL', 'ACTIVE',   0, '00000000-0000-0000-0000-000000000001'),
    (UNHEX(REPLACE('00000000-0000-0000-0000-000000000011', '-', '')), 'group_member2',    'gmember2@xyz.com',        'Đinh Quang Hải',     '$2a$12$u2sex9JV2pkYdLErDN64juv.4ACIftHIswis0HSwZHAL2vWR5F1Si', 'LOCAL', 'ACTIVE',   0, '00000000-0000-0000-0000-000000000001'),
    (UNHEX(REPLACE('00000000-0000-0000-0000-000000000012', '-', '')), 'group_member3',    'gmember3@def.com',        'Vũ Thị Lan',         '$2a$12$u2sex9JV2pkYdLErDN64juv.4ACIftHIswis0HSwZHAL2vWR5F1Si', 'LOCAL', 'ACTIVE',   0, '00000000-0000-0000-0000-000000000001'),
    -- Partner & Manager accounts
    (UNHEX(REPLACE('00000000-0000-0000-0000-000000000020', '-', '')), 'partner_abc',      'partner@abc.vn',          'Đối tác ABC',        '$2a$12$u2sex9JV2pkYdLErDN64juv.4ACIftHIswis0HSwZHAL2vWR5F1Si', 'LOCAL', 'ACTIVE',   0, '00000000-0000-0000-0000-000000000001'),
    (UNHEX(REPLACE('00000000-0000-0000-0000-000000000021', '-', '')), 'partner_xyz',      'partner@xyz.vn',          'Đối tác XYZ',        '$2a$12$u2sex9JV2pkYdLErDN64juv.4ACIftHIswis0HSwZHAL2vWR5F1Si', 'LOCAL', 'ACTIVE',   0, '00000000-0000-0000-0000-000000000001'),
    (UNHEX(REPLACE('00000000-0000-0000-0000-000000000022', '-', '')), 'manager_level1',   'manager1@rs.local',       'Nguyễn Quản Lý',     '$2a$12$u2sex9JV2pkYdLErDN64juv.4ACIftHIswis0HSwZHAL2vWR5F1Si', 'LOCAL', 'ACTIVE',   0, '00000000-0000-0000-0000-000000000001')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- ============================================================
-- 2. USER ROLES
-- ============================================================
INSERT INTO user_roles (user_id, role_id) VALUES
    -- ROLE_ADMIN (1)
    (UNHEX(REPLACE('00000000-0000-0000-0000-000000000002', '-', '')), 1),
    (UNHEX(REPLACE('00000000-0000-0000-0000-000000000003', '-', '')), 1),
    -- ROLE_LEVEL_1 (3)
    (UNHEX(REPLACE('00000000-0000-0000-0000-000000000004', '-', '')), 3),
    (UNHEX(REPLACE('00000000-0000-0000-0000-000000000005', '-', '')), 3),
    (UNHEX(REPLACE('00000000-0000-0000-0000-000000000006', '-', '')), 3),
    -- ROLE_LEVEL_2 (4)
    (UNHEX(REPLACE('00000000-0000-0000-0000-000000000007', '-', '')), 4),
    (UNHEX(REPLACE('00000000-0000-0000-0000-000000000008', '-', '')), 4),
    -- ROLE_LEVEL_3 (5)
    (UNHEX(REPLACE('00000000-0000-0000-0000-000000000009', '-', '')), 5),
    (UNHEX(REPLACE('00000000-0000-0000-0000-000000000010', '-', '')), 5),
    (UNHEX(REPLACE('00000000-0000-0000-0000-000000000011', '-', '')), 5),
    -- ROLE_LEVEL_4 (6)
    (UNHEX(REPLACE('00000000-0000-0000-0000-000000000012', '-', '')), 6),
    -- ROLE_PARTNER (7)
    (UNHEX(REPLACE('00000000-0000-0000-0000-000000000020', '-', '')), 7),
    (UNHEX(REPLACE('00000000-0000-0000-0000-000000000021', '-', '')), 7),
    -- ROLE_MANAGER (8)
    (UNHEX(REPLACE('00000000-0000-0000-0000-000000000022', '-', '')), 8)
ON DUPLICATE KEY UPDATE created_at = created_at;

-- ============================================================
-- 3. MANAGER HIERARCHY (operator1, operator2 báo cáo manager_level1)
-- ============================================================
UPDATE user_accounts
SET manager_user_id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000022', '-', ''))
WHERE user_id IN (
    UNHEX(REPLACE('00000000-0000-0000-0000-000000000004', '-', '')),
    UNHEX(REPLACE('00000000-0000-0000-0000-000000000005', '-', ''))
);
