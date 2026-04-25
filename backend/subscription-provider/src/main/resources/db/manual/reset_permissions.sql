-- ===========================================================
-- RESET PERMISSIONS
--
-- Xoá toàn bộ role_permissions + permissions, tạo lại đúng
-- theo @PreAuthorize trong từng Controller backend.
--
-- Mapping chuẩn:
--   group:*        → GroupController, GroupCommercialController,
--                    CommercialOrchestrationController (group endpoints)
--   plan:*         → PlanTemplateController, IndividualPlanConfigController,
--                    RetailPlanScheduleController
--   subscription:* → RuntimeSubscriptionController, ApprovalRequestController
--   report:view    → UsageAggregateController, SettlementStatementController,
--                    IndividualUsageTrackingController, AuditTimelineController
--   user:*         → UserController
--   role:*         → RoleController, SystemSettingController
--   audit-log:view → AuditLogController
--   dashboard:view → Dashboard (whitelist)
--
-- Thứ tự chạy:
--   1. Xoá role_permissions (FK → permissions)
--   2. Xoá permissions
--   3. Đảm bảo roles tồn tại
--   4. Insert 19 permissions mới
--   5. Insert role_permissions
-- ===========================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ===========================================================
-- 1. XOÁ DỮ LIỆU CŨ
-- ===========================================================
DELETE FROM role_permissions;
DELETE FROM permissions;
ALTER TABLE permissions AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS = 1;

-- ===========================================================
-- 2. ĐẢM BẢO ROLES TỒN TẠI
-- ===========================================================
INSERT INTO roles (role_id, role_name, display_name, description, is_system_role)
VALUES
    (1, 'ROLE_ADMIN',   'Administrator', 'System administrator', 1),
    (2, 'ROLE_USER',    'User',          'Default user role',     1),
    (3, 'ROLE_LEVEL_1', 'Vai trò cấp 1', 'Quản trị cấp 1',       0),
    (4, 'ROLE_LEVEL_2', 'Vai trò cấp 2', 'Quản trị cấp 2',       0),
    (5, 'ROLE_LEVEL_3', 'Vai trò cấp 3', 'Vận hành cấp 3',       0),
    (6, 'ROLE_LEVEL_4', 'Vai trò cấp 4', 'Xem báo cáo cấp 4',    0)
ON DUPLICATE KEY UPDATE
    display_name = VALUES(display_name),
    description  = VALUES(description),
    updated_at   = CURRENT_TIMESTAMP;

-- ===========================================================
-- 3. PERMISSIONS (19 quyền)
--
-- MODULE: TRANG_CHU  (sidebar: Trang chủ)
--   GROUP: DASHBOARD
--     1. dashboard:view
--
-- MODULE: KHACH_HANG_PHO_THONG  (sidebar: Khách hàng phổ thông)
--   GROUP: PLAN         (IndividualPlanConfigController, PlanTemplateController, RetailPlanScheduleController)
--     2. plan:view
--     3. plan:create
--     4. plan:update
--     5. plan:delete
--
-- MODULE: KHACH_HANG_DAI_LY  (sidebar: Khách hàng đại lý)
--   GROUP: GROUP        (GroupController, GroupCommercialController, CommercialOrchestrationController)
--     6. group:view
--     7. group:create
--     8. group:update
--   GROUP: SUBSCRIPTION (RuntimeSubscriptionController, ApprovalRequestController)
--     9. subscription:view
--    10. subscription:create
--    11. subscription:update
--
-- MODULE: QUAN_LY_PHAN_QUYEN  (sidebar: Quản lý phân quyền)
--   GROUP: USER         (UserController)
--    12. user:view
--    13. user:create
--    14. user:update
--   GROUP: ROLE         (RoleController, SystemSettingController)
--    15. role:view
--    16. role:create
--    17. role:update
--
-- MODULE: DASHBOARD  (sidebar: Dashboard)
--   GROUP: REPORT       (UsageAggregateController, SettlementStatementController,
--                        IndividualUsageTrackingController, AuditTimelineController)
--    18. report:view
--
-- MODULE: QUAN_LY_LOGS  (sidebar: Quản lý logs)
--   GROUP: AUDIT_LOG    (AuditLogController)
--    19. audit-log:view
-- ===========================================================
INSERT INTO permissions (permission_id, permission_key, display_name, module_group, group_name, sort_order)
VALUES
    -- ── TRANG_CHU (sidebar: Trang chủ) ────────────────────
    (1,  'dashboard:view',      'Xem tổng quan',                   'TRANG_CHU',               'DASHBOARD',    10),

    -- ── KHACH_HANG_PHO_THONG (sidebar: Khách hàng phổ thông) ─
    -- Controllers: IndividualPlanConfigController (/api/v1/individual/plan-configs)
    --              PlanTemplateController         (/api/v1/plan-templates)
    --              RetailPlanScheduleController   (/api/v1/retail-plan-schedules)
    (2,  'plan:view',           'Xem thông tin gói cước',          'KHACH_HANG_PHO_THONG',    'PLAN',         20),
    (3,  'plan:create',         'Tạo gói cước',                    'KHACH_HANG_PHO_THONG',    'PLAN',         21),
    (4,  'plan:update',         'Cập nhật gói cước',               'KHACH_HANG_PHO_THONG',    'PLAN',         22),
    (5,  'plan:delete',         'Xoá gói cước',                    'KHACH_HANG_PHO_THONG',    'PLAN',         23),

    -- ── KHACH_HANG_DAI_LY (sidebar: Khách hàng đại lý) ───
    -- Controllers: GroupController         (/api/v1/groups)
    --              GroupCommercialController (/api/v1/groups/{id}/contacts, /api/v1/groups/plan-assignments)
    --              CommercialOrchestrationController (/api/v1/commercial-flows/group-*)
    (6,  'group:view',          'Xem danh sách khách hàng đại lý', 'KHACH_HANG_DAI_LY',       'GROUP',        30),
    (7,  'group:create',        'Tạo khách hàng đại lý',           'KHACH_HANG_DAI_LY',       'GROUP',        31),
    (8,  'group:update',        'Cập nhật khách hàng đại lý',      'KHACH_HANG_DAI_LY',       'GROUP',        32),

    -- Controllers: RuntimeSubscriptionController (/api/v1/runtime-subscriptions)
    --              ApprovalRequestController      (/api/v1/approval-requests)
    (9,  'subscription:view',   'Xem đăng ký dịch vụ',             'KHACH_HANG_DAI_LY',       'SUBSCRIPTION', 40),
    (10, 'subscription:create', 'Tạo đăng ký dịch vụ',             'KHACH_HANG_DAI_LY',       'SUBSCRIPTION', 41),
    (11, 'subscription:update', 'Cập nhật đăng ký dịch vụ',        'KHACH_HANG_DAI_LY',       'SUBSCRIPTION', 42),

    -- ── QUAN_LY_PHAN_QUYEN (sidebar: Quản lý phân quyền) ──
    -- Controller: UserController (/api/v1/admin/users)
    (12, 'user:view',           'Xem danh sách người dùng',        'QUAN_LY_PHAN_QUYEN',      'USER',         50),
    (13, 'user:create',         'Tạo người dùng',                  'QUAN_LY_PHAN_QUYEN',      'USER',         51),
    (14, 'user:update',         'Chỉnh sửa người dùng',            'QUAN_LY_PHAN_QUYEN',      'USER',         52),

    -- Controllers: RoleController (/api/v1/admin/roles)
    --              SystemSettingController (/api/v1/settings)
    (15, 'role:view',           'Xem danh sách vai trò',           'QUAN_LY_PHAN_QUYEN',      'ROLE',         60),
    (16, 'role:create',         'Tạo vai trò',                     'QUAN_LY_PHAN_QUYEN',      'ROLE',         61),
    (17, 'role:update',         'Chỉnh sửa vai trò',               'QUAN_LY_PHAN_QUYEN',      'ROLE',         62),

    -- ── DASHBOARD (sidebar: Dashboard) ─────────────────────
    -- Controllers: UsageAggregateController        (/api/v1/usage-aggregates)
    --              SettlementStatementController    (/api/v1/settlement-statements GET)
    --              IndividualUsageTrackingController (/api/v1/individual/usage-tracking)
    --              AuditTimelineController          (/api/v1/audit-timelines)
    (18, 'report:view',         'Xem báo cáo',                     'DASHBOARD',               'REPORT',       70),

    -- ── QUAN_LY_LOGS (sidebar: Quản lý logs) ───────────────
    -- Controller: AuditLogController (/api/v1/admin/audit-logs)
    (19, 'audit-log:view',      'Xem nhật ký hệ thống',            'QUAN_LY_LOGS',            'AUDIT_LOG',    80);

-- ===========================================================
-- 4. ROLE PERMISSIONS
--
-- ROLE_ADMIN   (1): toàn quyền (19 quyền)
-- ROLE_USER    (2): chỉ dashboard:view
-- ROLE_LEVEL_1 (3): dashboard, plan:view/create, group:view, user:view, role:view, report:view
-- ROLE_LEVEL_2 (4): dashboard, plan:view/create
-- ROLE_LEVEL_3 (5): dashboard, plan:view/create/update/delete, group:view, user:view, role:view
-- ROLE_LEVEL_4 (6): plan:view/create (không có dashboard)
-- ===========================================================
INSERT INTO role_permissions (role_id, permission_id)
VALUES
    -- ROLE_ADMIN: toàn quyền
    (1,1),(1,2),(1,3),(1,4),(1,5),
    (1,6),(1,7),(1,8),
    (1,9),(1,10),(1,11),
    (1,12),(1,13),(1,14),
    (1,15),(1,16),(1,17),
    (1,18),(1,19),

    -- ROLE_USER: chỉ dashboard
    (2,1),

    -- ROLE_LEVEL_1: xem + tạo gói, xem group, xem user/role, xem báo cáo
    (3,1),(3,2),(3,3),(3,6),(3,12),(3,15),(3,18),

    -- ROLE_LEVEL_2: dashboard + xem & tạo gói
    (4,1),(4,2),(4,3),

    -- ROLE_LEVEL_3: quản lý gói đầy đủ, xem group, xem user/role
    (5,1),(5,2),(5,3),(5,4),(5,5),(5,6),(5,12),(5,15),

    -- ROLE_LEVEL_4: chỉ xem & tạo gói
    (6,2),(6,3);
