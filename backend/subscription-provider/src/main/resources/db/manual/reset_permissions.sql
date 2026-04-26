-- ===========================================================
-- RESET PERMISSIONS
--
-- Xoá toàn bộ role_permissions + permissions, tạo lại đúng
-- theo @PreAuthorize trong từng Controller backend.
--
-- ┌─ Mapping permission_key → Controller / API ───────────────
-- │
-- │  DASHBOARD
-- │    dashboard:view       → whitelist (không cần check)
-- │
-- │  KHÁCH HÀNG PHỔ THÔNG
-- │    plan:view            → IndividualPlanConfigController  GET
-- │    plan:create          → IndividualPlanConfigController  POST
-- │    plan:update          → IndividualPlanConfigController  POST/{id}
-- │    plan:delete          → IndividualPlanConfigController  DELETE
-- │    individual:usage:view→ IndividualUsageTrackingController GET
-- │
-- │  KHÁCH HÀNG ĐẠI LÝ
-- │    group:view           → GroupController                 GET
-- │    group:create         → GroupController                 POST
-- │    group:update         → GroupController                 PUT
-- │    subscription:view    → RuntimeSubscriptionController   GET
-- │    subscription:create  → RuntimeSubscriptionController   POST
-- │    subscription:update  → ApprovalRequestController       PUT
-- │
-- │  QUẢN LÝ PHÂN QUYỀN
-- │    user:view            → UserController                  GET
-- │    user:create          → UserController                  POST
-- │    user:update          → UserController                  PUT/PATCH
-- │    role:view            → RoleController                  GET
-- │    role:create          → RoleController                  POST
-- │    role:update          → RoleController                  PUT/DELETE
-- │
-- │  DASHBOARD BÁO CÁO THỐNG KÊ (Reports)
-- │    report:view          → điều kiện hiển thị menu Reports
-- │                           UsageAggregateController        GET (report:view OR report:group:view)
-- │                           SettlementStatementController   GET (report:view OR report:group:view)
-- │    report:group:view    → ReportsController /reports/group         GET
-- │                           ReportsController /reports/group/expiring-soon GET
-- │                           (nav.ts: 1 button bao gồm cả expiring-soon api)
-- │    report:individual:view → ReportsController /reports/individual  GET
-- │
-- │  QUẢN LÝ LOGS
-- │    audit-log:view       → AuditLogController              GET
-- │
-- └───────────────────────────────────────────────────────────
--
-- Thứ tự chạy:
--   1. Xoá role_permissions (FK → permissions)
--   2. Xoá permissions
--   3. Đảm bảo roles tồn tại
--   4. Insert 22 permissions
--   5. Insert role_permissions
-- ===========================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ===========================================================
-- 1. XOÁ DỮ LIỆU CŨ
-- ===========================================================
DELETE FROM role_permissions WHERE TRUE;
DELETE FROM permissions WHERE TRUE;
ALTER TABLE permissions AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS = 1;

-- ===========================================================
-- 2. ĐẢM BẢO ROLES TỒN TẠI
-- ===========================================================
INSERT INTO roles (role_id, role_name, display_name, description, is_system_role)
VALUES
    (1, 'ROLE_ADMIN',   'Administrator',  'System administrator', 1),
    (2, 'ROLE_USER',    'User',           'Default user role',    1),
    (3, 'ROLE_LEVEL_1', 'Vai trò cấp 1',  'Quản trị cấp 1',      0),
    (4, 'ROLE_LEVEL_2', 'Vai trò cấp 2',  'Quản trị cấp 2',      0),
    (5, 'ROLE_LEVEL_3', 'Vai trò cấp 3',  'Vận hành cấp 3',      0),
    (6, 'ROLE_LEVEL_4', 'Vai trò cấp 4',  'Xem báo cáo cấp 4',   0)
ON DUPLICATE KEY UPDATE
    display_name = VALUES(display_name),
    description  = VALUES(description),
    updated_at   = CURRENT_TIMESTAMP;

-- ===========================================================
-- 3. PERMISSIONS (22 quyền)
--
-- MODULE: TRANG_CHU
--   GROUP: DASHBOARD
--      1. dashboard:view
--
-- MODULE: KHACH_HANG_PHO_THONG
--   GROUP: PLAN         (IndividualPlanConfigController, PlanTemplateController)
--      2. plan:view
--      3. plan:create
--      4. plan:update
--      5. plan:delete
--   GROUP: INDIVIDUAL_USAGE  (IndividualUsageTrackingController)
--     20. individual:usage:view
--
-- MODULE: KHACH_HANG_DAI_LY
--   GROUP: AGENCY  (GroupController, GroupCommercialController,
--                   CommercialOrchestrationController,
--                   RuntimeSubscriptionController, ApprovalRequestController)
--      6. group:view
--      7. group:create
--      8. group:update
--      9. subscription:view
--     10. subscription:create
--     11. subscription:update
--
-- MODULE: QUAN_LY_PHAN_QUYEN
--   GROUP: USER         (UserController)
--     12. user:view
--     13. user:create
--     14. user:update
--   GROUP: ROLE         (RoleController, SystemSettingController)
--     15. role:view
--     16. role:create
--     17. role:update
--
-- MODULE: DASHBOARD (báo cáo thống kê — không có sidebar menu riêng)
--   GROUP: REPORT
--     18. report:view              → điều kiện hiển thị menu Reports
--     21. report:group:view        → tab Đại lý  (bao gồm /reports/group + /reports/group/expiring-soon)
--     22. report:individual:view   → tab Phổ thông (/reports/individual)
--
--   Lưu ý nav.ts:
--     - Menu Reports kiểm tra permissionKey = 'report:view'
--     - Trong role management UI chỉ hiện 2 checkbox (type: button):
--         "Xem báo cáo đại lý"    → report:group:view
--         "Xem báo cáo phổ thông" → report:individual:view
--     - /reports/group/expiring-soon là type: api → ẩn khỏi UI,
--       tự động được cấp khi có report:group:view
--
-- MODULE: QUAN_LY_LOGS
--   GROUP: AUDIT_LOG    (AuditLogController)
--     19. audit-log:view
-- ===========================================================
INSERT INTO permissions (permission_id, permission_key, display_name, module_group, group_name, sort_order)
VALUES
    -- ── TRANG_CHU ─────────────────────────────────────────────────────
    (1,  'dashboard:view',          'Xem tổng quan',                     'TRANG_CHU',            'DASHBOARD',        10),

    -- ── KHACH_HANG_PHO_THONG ──────────────────────────────────────────
    (2,  'plan:view',               'Xem thông tin gói cước',             'KHACH_HANG_PHO_THONG', 'PLAN',             20),
    (3,  'plan:create',             'Tạo gói cước',                       'KHACH_HANG_PHO_THONG', 'PLAN',             21),
    (4,  'plan:update',             'Cập nhật gói cước',                  'KHACH_HANG_PHO_THONG', 'PLAN',             22),
    (5,  'plan:delete',             'Xoá gói cước',                       'KHACH_HANG_PHO_THONG', 'PLAN',             23),
    (6, 'individual:usage:view',   'Xem theo dõi sử dụng cá nhân',       'KHACH_HANG_PHO_THONG', 'INDIVIDUAL_USAGE', 24),

    -- ── KHACH_HANG_DAI_LY ─────────────────────────────────────────────
    (7,  'group:view',              'Xem danh sách khách hàng đại lý',    'KHACH_HANG_DAI_LY',    'AGENCY',           30),
    (8,  'group:create',            'Tạo khách hàng đại lý',              'KHACH_HANG_DAI_LY',    'AGENCY',           31),
    (9,  'group:update',            'Cập nhật khách hàng đại lý',         'KHACH_HANG_DAI_LY',    'AGENCY',           32),
    (10,  'subscription:view',       'Xem đăng ký dịch vụ',                'KHACH_HANG_DAI_LY',    'AGENCY',           33),
    (11, 'subscription:create',     'Tạo đăng ký dịch vụ',                'KHACH_HANG_DAI_LY',    'AGENCY',           34),
    (12, 'subscription:update',     'Cập nhật đăng ký dịch vụ',           'KHACH_HANG_DAI_LY',    'AGENCY',           35),

    -- ── QUAN_LY_PHAN_QUYEN ────────────────────────────────────────────
    (13, 'user:view',               'Xem danh sách người dùng',           'QUAN_LY_PHAN_QUYEN',   'USER',             50),
    (14, 'user:create',             'Tạo người dùng',                     'QUAN_LY_PHAN_QUYEN',   'USER',             51),
    (15, 'user:update',             'Chỉnh sửa người dùng',               'QUAN_LY_PHAN_QUYEN',   'USER',             52),
    (16, 'role:view',               'Xem danh sách vai trò',              'QUAN_LY_PHAN_QUYEN',   'ROLE',             60),
    (17, 'role:create',             'Tạo vai trò',                        'QUAN_LY_PHAN_QUYEN',   'ROLE',             61),
    (18, 'role:update',             'Chỉnh sửa vai trò',                  'QUAN_LY_PHAN_QUYEN',   'ROLE',             62),

    -- ── DASHBOARD BÁO CÁO ─────────────────────────────────────────────
    -- report:view: quyền vào trang Reports (kiểm tra ở menu sidebar)
    -- UsageAggregateController GET và SettlementStatementController GET
    --   chấp nhận cả 'report:view' lẫn 'report:group:view'
    (19, 'report:view',             'Truy cập trang báo cáo',             'DASHBOARD',            'REPORT',           70),
    -- report:group:view: tab Đại lý + API expiring-soon (tự động bao gồm)
    (20, 'report:group:view',       'Xem báo cáo đại lý',                 'DASHBOARD',            'REPORT',           71),
    -- report:individual:view: tab Phổ thông
    (21, 'report:individual:view',  'Xem báo cáo phổ thông',              'DASHBOARD',            'REPORT',           72),

    -- ── QUAN_LY_LOGS ──────────────────────────────────────────────────
    (12, 'audit-log:view',          'Xem nhật ký hệ thống',               'QUAN_LY_LOGS',         'AUDIT_LOG',        80);

-- ===========================================================
-- 4. ROLE PERMISSIONS
--
-- Nguyên tắc báo cáo:
--   report:view (18)            = thấy menu Reports (bắt buộc kèm theo 21 hoặc 22)
--   report:group:view (21)      = thấy tab Đại lý + tự động có /expiring-soon
--   report:individual:view (22) = thấy tab Phổ thông
--
-- ROLE_ADMIN   (1): toàn quyền — 22 permissions
-- ROLE_USER    (2): chỉ dashboard:view
-- ROLE_LEVEL_1 (3): quản trị — xem cả 2 tab báo cáo
-- ROLE_LEVEL_2 (4): phổ thông — chỉ xem tab Phổ thông
-- ROLE_LEVEL_3 (5): vận hành — quản lý gói, không xem báo cáo
-- ROLE_LEVEL_4 (6): chỉ xem & tạo gói cước
-- ===========================================================
INSERT INTO role_permissions (role_id, permission_id)
VALUES
    -- ── ROLE_ADMIN: toàn quyền ────────────────────────────────────────
    (1, 1),                              -- dashboard:view
    (1, 2),(1, 3),(1, 4),(1, 5),(1,20),  -- plan:* + individual:usage:view
    (1, 6),(1, 7),(1, 8),                -- group:*
    (1, 9),(1,10),(1,11),                -- subscription:*
    (1,12),(1,13),(1,14),                -- user:*
    (1,15),(1,16),(1,17),                -- role:*
    (1,18),(1,21),(1,22),                -- report:view + group + individual
    (1,19),                              -- audit-log:view

    -- ── ROLE_USER: chỉ dashboard ──────────────────────────────────────
    (2, 1),

    -- ── ROLE_LEVEL_1: quản trị cấp 1 — cả 2 tab báo cáo ──────────────
    (3, 1),                              -- dashboard:view
    (3, 2),(3, 3),(3,20),                -- plan:view, plan:create, individual:usage:view
    (3, 6),(3,12),(3,15),                -- group:view, user:view, role:view
    (3,18),(3,21),(3,22),                -- report:view + cả 2 tab

    -- ── ROLE_LEVEL_2: cấp 2 — chỉ tab Phổ thông ──────────────────────
    (4, 1),                              -- dashboard:view
    (4, 2),(4, 3),(4,20),                -- plan:view, plan:create, individual:usage:view
    (4,18),(4,22),                       -- report:view + chỉ tab Phổ thông (không có 21)

    -- ── ROLE_LEVEL_3: vận hành — không có báo cáo ────────────────────
    (5, 1),                              -- dashboard:view
    (5, 2),(5, 3),(5, 4),(5, 5),(5,20),  -- plan:* + individual:usage:view
    (5, 6),(5,12),(5,15),                -- group:view, user:view, role:view

    -- ── ROLE_LEVEL_4: chỉ xem & tạo gói ──────────────────────────────
    (6, 2),(6, 3);                       -- plan:view, plan:create
