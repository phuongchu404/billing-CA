-- ============================================================
-- RESET PERMISSIONS
--
-- Xoá toàn bộ role_permissions + permissions rồi tạo lại
-- đúng với @PreAuthorize trong các Controller hiện tại.
--
-- Permission key → Controller / API mapping:
--
-- dashboard:view          → (whitelist, không cần check)
--
-- plan:view               → IndividualPlanConfigController  GET
-- plan:create             → IndividualPlanConfigController  POST
-- plan:update             → IndividualPlanConfigController  PUT
-- plan:delete             → IndividualPlanConfigController  DELETE
-- individual:usage:view   → IndividualUsageTrackingController GET
--
-- group:view              → GroupController                 GET
-- group:create            → GroupController                 POST
-- group:update            → GroupController                 PUT
-- subscription:view       → RuntimeSubscriptionController   GET
-- subscription:create     → RuntimeSubscriptionController   POST
-- subscription:update     → ApprovalRequestController       PUT
--
-- user:view               → UserController                  GET
-- user:create             → UserController                  POST
-- user:update             → UserController                  PUT/PATCH
-- role:view               → RoleController                  GET
-- role:create             → RoleController                  POST
-- role:update             → RoleController                  PUT/DELETE
--
-- report:view             → điều kiện hiển thị menu Reports
-- report:group:view       → ReportService /reports/group (bao gồm /expiring-soon)
-- report:individual:view  → ReportService /reports/individual
--
-- audit-log:view          → AuditLogController              GET
--
-- group:view:own          → DataScopeService (filter nhân viên)
-- group:view:subordinates → DataScopeService (filter manager)
-- group:assign:owner      → GroupController PUT /owner
-- report:view:own         → ReportService (scope cá nhân)
-- report:view:subordinates→ ReportService (scope manager)
-- report:view:partner     → ReportService (scope đối tác)
-- partner:access:grant    → PartnerGroupAccessController POST
-- partner:access:revoke   → PartnerGroupAccessController DELETE
--
-- Thứ tự chạy:
--   1. Xoá role_permissions (FK → permissions)
--   2. Xoá permissions
--   3. Insert 30 permissions
--   4. Insert role_permissions
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM role_permissions WHERE TRUE;
DELETE FROM permissions       WHERE TRUE;
ALTER TABLE permissions AUTO_INCREMENT = 1;
SET FOREIGN_KEY_CHECKS = 1;

-- ── Permissions ───────────────────────────────────────────────
INSERT INTO permissions (permission_id, permission_key, display_name, module_group, group_name, sort_order) VALUES
    (1,  'dashboard:view',           'Xem tổng quan',                        'TRANG_CHU',            'DASHBOARD',        10),
    (2,  'plan:view',                'Xem gói cước',                         'KHACH_HANG_PHO_THONG', 'PLAN',             20),
    (3,  'plan:create',              'Tạo gói cước',                         'KHACH_HANG_PHO_THONG', 'PLAN',             21),
    (4,  'plan:update',              'Cập nhật gói cước',                    'KHACH_HANG_PHO_THONG', 'PLAN',             22),
    (5,  'plan:delete',              'Xoá gói cước',                         'KHACH_HANG_PHO_THONG', 'PLAN',             23),
    (6,  'individual:usage:view',    'Xem theo dõi sử dụng cá nhân',         'KHACH_HANG_PHO_THONG', 'INDIVIDUAL_USAGE', 24),
    (7,  'group:view',               'Xem danh sách khách hàng đại lý',      'KHACH_HANG_DAI_LY',    'AGENCY',           30),
    (8,  'group:create',             'Tạo khách hàng đại lý',                'KHACH_HANG_DAI_LY',    'AGENCY',           31),
    (9,  'group:update',             'Cập nhật khách hàng đại lý',           'KHACH_HANG_DAI_LY',    'AGENCY',           32),
    (10, 'subscription:view',        'Xem đăng ký dịch vụ',                  'KHACH_HANG_DAI_LY',    'AGENCY',           33),
    (11, 'subscription:create',      'Tạo đăng ký dịch vụ',                  'KHACH_HANG_DAI_LY',    'AGENCY',           34),
    (12, 'subscription:update',      'Cập nhật đăng ký dịch vụ',             'KHACH_HANG_DAI_LY',    'AGENCY',           35),
    (13, 'user:view',                'Xem danh sách người dùng',             'QUAN_LY_PHAN_QUYEN',   'USER',             50),
    (14, 'user:create',              'Tạo người dùng',                       'QUAN_LY_PHAN_QUYEN',   'USER',             51),
    (15, 'user:update',              'Chỉnh sửa người dùng',                 'QUAN_LY_PHAN_QUYEN',   'USER',             52),
    (16, 'role:view',                'Xem danh sách vai trò',                'QUAN_LY_PHAN_QUYEN',   'ROLE',             60),
    (17, 'role:create',              'Tạo vai trò',                          'QUAN_LY_PHAN_QUYEN',   'ROLE',             61),
    (18, 'role:update',              'Chỉnh sửa vai trò',                    'QUAN_LY_PHAN_QUYEN',   'ROLE',             62),
    (19, 'report:view',              'Truy cập trang báo cáo',               'BAO_CAO',              'REPORT',           70),
    (20, 'report:group:view',        'Xem báo cáo đại lý',                   'BAO_CAO',              'REPORT',           71),
    (21, 'report:individual:view',   'Xem báo cáo phổ thông',                'BAO_CAO',              'REPORT',           72),
    (22, 'audit-log:view',           'Xem nhật ký hệ thống',                 'QUAN_LY_LOGS',         'AUDIT_LOG',        80),
    (23, 'group:view:own',           'Xem khách hàng của mình',              'KHACH_HANG_DAI_LY',    'DATA_SCOPE',       83),
    (24, 'group:view:subordinates',  'Xem khách hàng của cấp dưới',          'KHACH_HANG_DAI_LY',    'DATA_SCOPE',       84),
    (25, 'group:assign:owner',       'Gán nhân viên phụ trách',              'KHACH_HANG_DAI_LY',    'DATA_SCOPE',       85),
    (26, 'report:view:own',          'Xem báo cáo của khách hàng mình',      'BAO_CAO',              'DATA_SCOPE',       86),
    (27, 'report:view:subordinates', 'Xem báo cáo của khách hàng cấp dưới',  'BAO_CAO',              'DATA_SCOPE',       87),
    (28, 'report:view:partner',      'Xem báo cáo đối tác (self-service)',   'BAO_CAO',              'DATA_SCOPE',       88),
    (29, 'partner:access:grant',     'Cấp quyền đối tác xem group',          'HE_THONG',             'PARTNER',          90),
    (30, 'partner:access:revoke',    'Thu hồi quyền đối tác',                'HE_THONG',             'PARTNER',          91);

-- ── Role-permission mappings ──────────────────────────────────
INSERT INTO role_permissions (role_id, permission_id) VALUES
    -- ROLE_ADMIN (1): toàn quyền
    (1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),(1,9),(1,10),
    (1,11),(1,12),(1,13),(1,14),(1,15),(1,16),(1,17),(1,18),(1,19),(1,20),
    (1,21),(1,22),(1,23),(1,24),(1,25),(1,26),(1,27),(1,28),(1,29),(1,30),

    -- ROLE_USER (2): chỉ dashboard
    (2,1),

    -- ROLE_LEVEL_1 (3): quản trị — cả 2 tab báo cáo
    (3,1),(3,2),(3,3),(3,6),(3,7),(3,13),(3,16),(3,19),(3,20),(3,21),(3,22),(3,23),(3,26),

    -- ROLE_LEVEL_2 (4): phổ thông — chỉ tab phổ thông
    (4,1),(4,2),(4,3),(4,6),(4,19),(4,21),

    -- ROLE_LEVEL_3 (5): vận hành — không có báo cáo
    (5,1),(5,2),(5,3),(5,4),(5,5),(5,6),(5,7),(5,8),(5,9),(5,13),(5,16),

    -- ROLE_LEVEL_4 (6): chỉ xem & tạo gói
    (6,2),(6,3),

    -- ROLE_PARTNER (7): xem báo cáo đối tác
    (7,1),(7,19),(7,20),(7,28),

    -- ROLE_MANAGER (8): quản lý nội bộ
    (8,1),(8,7),(8,10),(8,13),(8,16),(8,19),(8,20),(8,21),(8,23),(8,24),(8,26),(8,27);
