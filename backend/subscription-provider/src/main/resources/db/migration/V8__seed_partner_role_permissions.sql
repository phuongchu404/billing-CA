-- ============================================================
-- V8: Seed ROLE_PARTNER + permissions phân quyền dữ liệu
--
-- Thêm role ROLE_PARTNER cho tài khoản đối tác và các
-- permission mới hỗ trợ data-level filtering.
-- ============================================================

SET NAMES utf8mb4;

-- ── 1. Role mới ───────────────────────────────────────────────
INSERT INTO roles (role_id, role_name, display_name, description, is_system_role)
VALUES
    (7, 'ROLE_PARTNER',  'Đối tác', 'Tài khoản đối tác — chỉ xem báo cáo của group được cấp quyền', 0),
    (8, 'ROLE_MANAGER',  'Quản lý', 'Quản lý nội bộ — xem khách hàng và báo cáo của cấp dưới',      0)
ON DUPLICATE KEY UPDATE
    display_name = VALUES(display_name),
    description  = VALUES(description),
    updated_at   = CURRENT_TIMESTAMP;

-- ── 2. Permissions mới ───────────────────────────────────────
-- permission_id 23-30 dành cho data-scope permissions
INSERT INTO permissions (permission_id, permission_key, display_name, module_group, group_name, sort_order)
VALUES
    -- Quản lý khách hàng theo scope
    (23, 'group:view:own',          'Xem khách hàng của mình',                 'CUSTOMER_MANAGEMENT', 'GROUP',  80),
    (24, 'group:view:subordinates', 'Xem khách hàng của cấp dưới',             'CUSTOMER_MANAGEMENT', 'GROUP',  81),
    (25, 'group:assign:owner',      'Gán nhân viên phụ trách khách hàng',      'CUSTOMER_MANAGEMENT', 'GROUP',  82),

    -- Báo cáo theo scope
    (26, 'report:view:own',         'Xem báo cáo của khách hàng mình',         'ANALYTICS',           'REPORT', 83),
    (27, 'report:view:subordinates','Xem báo cáo của khách hàng cấp dưới',     'ANALYTICS',           'REPORT', 84),
    (28, 'report:view:partner',     'Xem báo cáo đối tác (self-service)',       'ANALYTICS',           'REPORT', 85),

    -- Quản lý partner access
    (29, 'partner:access:grant',    'Cấp quyền đối tác xem group',             'SYSTEM_CONFIGURATION','PARTNER', 90),
    (30, 'partner:access:revoke',   'Thu hồi quyền đối tác',                   'SYSTEM_CONFIGURATION','PARTNER', 91)
ON DUPLICATE KEY UPDATE
    display_name = VALUES(display_name),
    module_group = VALUES(module_group),
    group_name   = VALUES(group_name),
    sort_order   = VALUES(sort_order);

-- ── 3. Role-permission mapping ────────────────────────────────

-- ROLE_ADMIN (1): thêm các permission mới — vẫn toàn quyền
INSERT INTO role_permissions (role_id, permission_id)
VALUES
    (1, 23),(1, 24),(1, 25),(1, 26),(1, 27),(1, 28),(1, 29),(1, 30)
ON DUPLICATE KEY UPDATE role_id = role_id;

-- ROLE_MANAGER (8): xem khách hàng mình + cấp dưới, báo cáo mình + cấp dưới
INSERT INTO role_permissions (role_id, permission_id)
VALUES
    (8,  1),   -- dashboard:view
    (8,  6),   -- subscription:view
    (8,  9),   -- user:view
    (8, 16),   -- report:view (menu)
    (8, 21),   -- report:group:view
    (8, 22),   -- report:individual:view
    (8, 23),   -- group:view:own
    (8, 24),   -- group:view:subordinates
    (8, 26),   -- report:view:own
    (8, 27)    -- report:view:subordinates
ON DUPLICATE KEY UPDATE role_id = role_id;

-- ROLE_PARTNER (7): chỉ xem báo cáo của group được cấp quyền
INSERT INTO role_permissions (role_id, permission_id)
VALUES
    (7,  1),   -- dashboard:view
    (7, 16),   -- report:view (menu — để thấy mục Reports)
    (7, 21),   -- report:group:view
    (7, 28)    -- report:view:partner
ON DUPLICATE KEY UPDATE role_id = role_id;

-- ROLE_LEVEL_1 (3): bổ sung xem khách hàng mình + báo cáo mình
INSERT INTO role_permissions (role_id, permission_id)
VALUES
    (3, 23),   -- group:view:own
    (3, 26)    -- report:view:own
ON DUPLICATE KEY UPDATE role_id = role_id;

-- ── 4. Demo user đối tác ─────────────────────────────────────
INSERT INTO user_accounts (user_id, username, email, full_name, password_hash, auth_provider, status, failed_login_attempts, created_by)
VALUES
    ('00000000-0000-0000-0000-000000000020', 'partner_abc',  'partner@abc.vn',  'Đối tác ABC',  '$2a$12$u2sex9JV2pkYdLErDN64juv.4ACIftHIswis0HSwZHAL2vWR5F1Si', 'LOCAL', 'ACTIVE', 0, '00000000-0000-0000-0000-000000000001'),
    ('00000000-0000-0000-0000-000000000021', 'partner_xyz',  'partner@xyz.vn',  'Đối tác XYZ',  '$2a$12$u2sex9JV2pkYdLErDN64juv.4ACIftHIswis0HSwZHAL2vWR5F1Si', 'LOCAL', 'ACTIVE', 0, '00000000-0000-0000-0000-000000000001'),
    ('00000000-0000-0000-0000-000000000022', 'manager_level1','manager1@rs.local','Nguyễn Quản Lý','$2a$12$u2sex9JV2pkYdLErDN64juv.4ACIftHIswis0HSwZHAL2vWR5F1Si', 'LOCAL', 'ACTIVE', 0, '00000000-0000-0000-0000-000000000001')
ON DUPLICATE KEY UPDATE
    full_name  = VALUES(full_name),
    updated_at = CURRENT_TIMESTAMP;

-- Gán role cho demo users
INSERT INTO user_roles (user_id, role_id)
VALUES
    ('00000000-0000-0000-0000-000000000020', 7),  -- partner_abc  → ROLE_PARTNER
    ('00000000-0000-0000-0000-000000000021', 7),  -- partner_xyz  → ROLE_PARTNER
    ('00000000-0000-0000-0000-000000000022', 8)   -- manager_level1 → ROLE_MANAGER
ON DUPLICATE KEY UPDATE created_at = created_at;

-- Manager demo: operator1 và operator2 báo cáo cho manager_level1
UPDATE user_accounts
SET manager_user_id = '00000000-0000-0000-0000-000000000022'
WHERE user_id IN (
    '00000000-0000-0000-0000-000000000004',  -- operator1
    '00000000-0000-0000-0000-000000000005'   -- operator2
);
