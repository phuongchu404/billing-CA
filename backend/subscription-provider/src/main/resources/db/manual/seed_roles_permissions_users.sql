-- ===========================================================
-- SEED: roles, permissions, user_accounts, user_roles,
--       role_permissions
--
-- Chạy file này SAU KHI đã chạy V1 & V2 migration.
-- Mật khẩu mặc định tất cả user: Admin@123
-- (BCrypt cost=12 – đổi trước khi lên production)
-- ===========================================================

SET NAMES utf8mb4;

-- ===========================================================
-- 1. ROLES  (ROLE_ADMIN=1, ROLE_USER=2 đã có trong V1)
-- ===========================================================
INSERT INTO roles (role_id, role_name, display_name, description, is_system_role)
VALUES
    (3, 'ROLE_LEVEL_1', 'Vai trò cấp 1', 'Quản trị cấp 1',  0),
    (4, 'ROLE_LEVEL_2', 'Vai trò cấp 2', 'Quản trị cấp 2',  0),
    (5, 'ROLE_LEVEL_3', 'Vai trò cấp 3', 'Vận hành cấp 3',  0),
    (6, 'ROLE_LEVEL_4', 'Vai trò cấp 4', 'Xem báo cáo cấp 4', 0)
ON DUPLICATE KEY UPDATE
    display_name  = VALUES(display_name),
    description   = VALUES(description),
    updated_at    = CURRENT_TIMESTAMP;

-- ===========================================================
-- 2. PERMISSIONS (khớp với fallback data trong roles/index.vue)
-- ===========================================================
INSERT INTO permissions (permission_id, permission_key, display_name, module_group, group_name, sort_order)
VALUES
    -- DASHBOARD
    (1,  'dashboard:view',        'Xem thông tin',                  'DASHBOARD',               'DASHBOARD',    10),
    -- SUBSCRIPTION_MANAGEMENT / PLAN
    (2,  'plan:view',             'Xem thông tin theo thiết bị',    'SUBSCRIPTION_MANAGEMENT', 'PLAN',         20),
    (3,  'plan:create',           'Xem thông tin theo chi nhánh',   'SUBSCRIPTION_MANAGEMENT', 'PLAN',         21),
    (4,  'plan:update',           'Thêm chi nhánh',                 'SUBSCRIPTION_MANAGEMENT', 'PLAN',         22),
    (5,  'plan:delete',           'Đổi chi nhánh cho thiết bị',     'SUBSCRIPTION_MANAGEMENT', 'PLAN',         23),
    -- SUBSCRIPTION_MANAGEMENT / SUBSCRIPTION
    (6,  'subscription:view',     'Xem thông tin',                  'SUBSCRIPTION_MANAGEMENT', 'SUBSCRIPTION', 30),
    (7,  'subscription:create',   'Tải lên firmware',               'SUBSCRIPTION_MANAGEMENT', 'SUBSCRIPTION', 31),
    (8,  'subscription:update',   'Cấu hình cập nhật firmware',     'SUBSCRIPTION_MANAGEMENT', 'SUBSCRIPTION', 32),
    -- SYSTEM_CONFIGURATION / USER
    (9,  'user:view',             'Xem danh sách người dùng',       'SYSTEM_CONFIGURATION',    'USER',         40),
    (10, 'user:create',           'Tạo người dùng',                 'SYSTEM_CONFIGURATION',    'USER',         41),
    (11, 'user:update',           'Chỉnh sửa người dùng',           'SYSTEM_CONFIGURATION',    'USER',         42),
    -- SYSTEM_CONFIGURATION / ROLE
    (12, 'role:view',             'Xem danh sách vai trò',          'SYSTEM_CONFIGURATION',    'ROLE',         50),
    (13, 'role:create',           'Tạo vai trò',                    'SYSTEM_CONFIGURATION',    'ROLE',         51),
    (14, 'role:update',           'Chỉnh sửa vai trò',              'SYSTEM_CONFIGURATION',    'ROLE',         52),
    -- SYSTEM_CONFIGURATION / AUDIT_LOG
    (15, 'audit-log:view',        'Xem nhật ký hệ thống',           'SYSTEM_CONFIGURATION',    'AUDIT_LOG',    60),
    -- ANALYTICS / REPORT
    (16, 'report:view',              'Xem báo cáo',                    'ANALYTICS',               'REPORT',       70),
    -- ANALYTICS / REPORT – tab-level
    (21, 'report:group:view',        'Xem báo cáo đại lý',             'ANALYTICS',               'REPORT',       71),
    (22, 'report:individual:view',   'Xem báo cáo phổ thông',          'ANALYTICS',               'REPORT',       72)
ON DUPLICATE KEY UPDATE
    display_name  = VALUES(display_name),
    module_group  = VALUES(module_group),
    group_name    = VALUES(group_name),
    sort_order    = VALUES(sort_order);

-- ===========================================================
-- 3. USER ACCOUNTS  (user 1 = admin đã có trong V1)
--    Mật khẩu: Admin@123
-- ===========================================================
INSERT INTO user_accounts (user_id, username, email, full_name, password_hash, auth_provider, status, failed_login_attempts, created_by)
VALUES
    ('00000000-0000-0000-0000-000000000002', 'billing_admin',   'billing_admin@rs.local',   'Nguyễn Thị Bình',  '$2a$12$u2sex9JV2pkYdLErDN64juv.4ACIftHIswis0HSwZHAL2vWR5F1Si', 'LOCAL', 'ACTIVE', 0, '00000000-0000-0000-0000-000000000001'),
    ('00000000-0000-0000-0000-000000000003', 'group_admin',      'group_admin@rs.local',     'Trần Văn Cường',   '$2a$12$u2sex9JV2pkYdLErDN64juv.4ACIftHIswis0HSwZHAL2vWR5F1Si', 'LOCAL', 'ACTIVE', 0, '00000000-0000-0000-0000-000000000001'),
    ('00000000-0000-0000-0000-000000000004', 'operator1',        'operator1@rs.local',       'Lê Minh Đức',      '$2a$12$u2sex9JV2pkYdLErDN64juv.4ACIftHIswis0HSwZHAL2vWR5F1Si', 'LOCAL', 'ACTIVE', 0, '00000000-0000-0000-0000-000000000001'),
    ('00000000-0000-0000-0000-000000000005', 'operator2',        'operator2@rs.local',       'Phạm Thị Hoa',     '$2a$12$u2sex9JV2pkYdLErDN64juv.4ACIftHIswis0HSwZHAL2vWR5F1Si', 'LOCAL', 'ACTIVE', 0, '00000000-0000-0000-0000-000000000001'),
    ('00000000-0000-0000-0000-000000000006', 'viewer1',          'viewer1@rs.local',         'Hoàng Anh Tuấn',   '$2a$12$u2sex9JV2pkYdLErDN64juv.4ACIftHIswis0HSwZHAL2vWR5F1Si', 'LOCAL', 'ACTIVE', 0, '00000000-0000-0000-0000-000000000001'),
    ('00000000-0000-0000-0000-000000000007', 'individual_user1', 'iuser1@customer.local',    'Nguyễn Văn An',    '$2a$12$u2sex9JV2pkYdLErDN64juv.4ACIftHIswis0HSwZHAL2vWR5F1Si', 'LOCAL', 'ACTIVE', 0, '00000000-0000-0000-0000-000000000001'),
    ('00000000-0000-0000-0000-000000000008', 'individual_user2', 'iuser2@customer.local',    'Trần Thị Bảo',     '$2a$12$u2sex9JV2pkYdLErDN64juv.4ACIftHIswis0HSwZHAL2vWR5F1Si', 'LOCAL', 'ACTIVE', 0, '00000000-0000-0000-0000-000000000001'),
    ('00000000-0000-0000-0000-000000000009', 'individual_user3', 'iuser3@customer.local',    'Lý Thành Công',    '$2a$12$u2sex9JV2pkYdLErDN64juv.4ACIftHIswis0HSwZHAL2vWR5F1Si', 'LOCAL', 'INACTIVE', 0, '00000000-0000-0000-0000-000000000001'),
    ('00000000-0000-0000-0000-000000000010', 'group_member1',    'gmember1@abc.com',         'Phan Thị Dung',    '$2a$12$u2sex9JV2pkYdLErDN64juv.4ACIftHIswis0HSwZHAL2vWR5F1Si', 'LOCAL', 'ACTIVE', 0, '00000000-0000-0000-0000-000000000001'),
    ('00000000-0000-0000-0000-000000000011', 'group_member2',    'gmember2@xyz.com',         'Đinh Quang Hải',   '$2a$12$u2sex9JV2pkYdLErDN64juv.4ACIftHIswis0HSwZHAL2vWR5F1Si', 'LOCAL', 'ACTIVE', 0, '00000000-0000-0000-0000-000000000001'),
    ('00000000-0000-0000-0000-000000000012', 'group_member3',    'gmember3@def.com',         'Vũ Thị Lan',       '$2a$12$u2sex9JV2pkYdLErDN64juv.4ACIftHIswis0HSwZHAL2vWR5F1Si', 'LOCAL', 'ACTIVE', 0, '00000000-0000-0000-0000-000000000001')
ON DUPLICATE KEY UPDATE
    full_name     = VALUES(full_name),
    email         = VALUES(email),
    password_hash = IF(password_hash = '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5qXvS4p6UwHuu', VALUES(password_hash), password_hash),
    updated_at    = CURRENT_TIMESTAMP;

-- ===========================================================
-- 4. USER ROLES
-- ROLE_ADMIN   (1): user 1 (V1), 2, 3
-- ROLE_LEVEL_1 (3): user 4, 5, 6
-- ROLE_LEVEL_2 (4): user 7, 8
-- ROLE_LEVEL_3 (5): user 9, 10, 11
-- ROLE_LEVEL_4 (6): user 12
-- ===========================================================
INSERT INTO user_roles (user_id, role_id)
VALUES
    ('00000000-0000-0000-0000-000000000002', 1),   -- billing_admin   → ROLE_ADMIN
    ('00000000-0000-0000-0000-000000000003', 1),   -- group_admin     → ROLE_ADMIN
    ('00000000-0000-0000-0000-000000000004', 3),   -- operator1       → ROLE_LEVEL_1
    ('00000000-0000-0000-0000-000000000005', 3),   -- operator2       → ROLE_LEVEL_1
    ('00000000-0000-0000-0000-000000000006', 3),   -- viewer1         → ROLE_LEVEL_1
    ('00000000-0000-0000-0000-000000000007', 4),   -- individual_user1→ ROLE_LEVEL_2
    ('00000000-0000-0000-0000-000000000008', 4),   -- individual_user2→ ROLE_LEVEL_2
    ('00000000-0000-0000-0000-000000000009', 5),   -- individual_user3→ ROLE_LEVEL_3
    ('00000000-0000-0000-0000-000000000010', 5),   -- group_member1   → ROLE_LEVEL_3
    ('00000000-0000-0000-0000-000000000011', 5),   -- group_member2   → ROLE_LEVEL_3
    ('00000000-0000-0000-0000-000000000012', 6)    -- group_member3   → ROLE_LEVEL_4
ON DUPLICATE KEY UPDATE created_at = created_at;

-- ===========================================================
-- 5. ROLE PERMISSIONS
--
-- Nguyên tắc report:
--   report:view (16)            = thấy menu Reports
--   report:group:view (21)      = thấy tab Đại lý
--   report:individual:view (22) = thấy tab Phổ thông
--
-- ROLE_ADMIN   (1): toàn quyền
-- ROLE_LEVEL_1 (3): quản trị – cả 2 tab báo cáo
-- ROLE_LEVEL_2 (4): phổ thông – chỉ tab Phổ thông
-- ROLE_LEVEL_3 (5): vận hành – không báo cáo
-- ROLE_LEVEL_4 (6): chỉ xem gói
-- ROLE_USER    (2): chỉ dashboard:view
-- ===========================================================
INSERT INTO role_permissions (role_id, permission_id)
VALUES
    -- ROLE_ADMIN: toàn quyền (thêm 21, 22)
    (1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),
    (1,9),(1,10),(1,11),(1,12),(1,13),(1,14),(1,15),(1,16),
    (1,21),(1,22),

    -- ROLE_USER: chỉ xem dashboard
    (2,1),

    -- ROLE_LEVEL_1: cả 2 tab báo cáo
    (3,1),(3,2),(3,3),(3,6),(3,9),(3,12),(3,16),(3,21),(3,22),

    -- ROLE_LEVEL_2: chỉ tab Phổ thông (thêm 22, giữ report:view=16)
    (4,1),(4,2),(4,3),(4,16),(4,22),

    -- ROLE_LEVEL_3: vận hành – không có báo cáo
    (5,1),(5,2),(5,3),(5,4),(5,5),(5,6),(5,9),(5,12),

    -- ROLE_LEVEL_4: chỉ xem & tạo gói
    (6,2),(6,3)
ON DUPLICATE KEY UPDATE role_id = role_id;
