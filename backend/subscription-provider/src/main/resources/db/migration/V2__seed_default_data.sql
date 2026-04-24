-- ============================================================
-- Seed default data for billing-CA system
-- Generated from entity model + db migration analysis
-- ============================================================

-- ============================================================
-- 1. PERMISSIONS
-- ============================================================
INSERT INTO permissions (permission_key, display_name, module_group, group_name, sort_order) VALUES
-- User Management
('USER_VIEW',             'Xem danh sách người dùng',         'USER_MANAGEMENT',        'User',       1),
('USER_CREATE',           'Tạo người dùng mới',               'USER_MANAGEMENT',        'User',       2),
('USER_UPDATE',           'Cập nhật thông tin người dùng',    'USER_MANAGEMENT',        'User',       3),
('USER_DELETE',           'Xóa người dùng',                   'USER_MANAGEMENT',        'User',       4),
('USER_LOCK',             'Khóa/Mở khóa người dùng',          'USER_MANAGEMENT',        'User',       5),
('USER_RESET_PASSWORD',   'Reset mật khẩu người dùng',        'USER_MANAGEMENT',        'User',       6),
-- Role Management
('ROLE_VIEW',             'Xem danh sách vai trò',            'ROLE_MANAGEMENT',        'Role',       10),
('ROLE_CREATE',           'Tạo vai trò mới',                  'ROLE_MANAGEMENT',        'Role',       11),
('ROLE_UPDATE',           'Cập nhật vai trò',                 'ROLE_MANAGEMENT',        'Role',       12),
('ROLE_DELETE',           'Xóa vai trò',                      'ROLE_MANAGEMENT',        'Role',       13),
('ROLE_ASSIGN_PERMISSION','Gán quyền cho vai trò',            'ROLE_MANAGEMENT',        'Role',       14),
-- Plan Template
('PLAN_TEMPLATE_VIEW',    'Xem danh sách gói dịch vụ',        'PLAN_TEMPLATE',           'Plan Template',  20),
('PLAN_TEMPLATE_CREATE',  'Tạo gói dịch vụ mới',              'PLAN_TEMPLATE',           'Plan Template',  21),
('PLAN_TEMPLATE_UPDATE',  'Cập nhật gói dịch vụ',             'PLAN_TEMPLATE',           'Plan Template',  22),
('PLAN_TEMPLATE_DELETE',  'Xóa gói dịch vụ',                  'PLAN_TEMPLATE',           'Plan Template',  23),
('PLAN_TEMPLATE_CLONE',   'Nhân bản gói dịch vụ',             'PLAN_TEMPLATE',           'Plan Template',  24),
('PLAN_TEMPLATE_ACTIVATE','Kích hoạt gói dịch vụ',            'PLAN_TEMPLATE',           'Plan Template',  25),
-- Pricing Rule
('PRICING_RULE_VIEW',     'Xem quy tắc giá',                  'PRICING_RULE',            'Pricing Rule',   30),
('PRICING_RULE_CREATE',   'Tạo quy tắc giá mới',              'PRICING_RULE',            'Pricing Rule',   31),
('PRICING_RULE_UPDATE',   'Cập nhật quy tắc giá',             'PRICING_RULE',            'Pricing Rule',   32),
('PRICING_RULE_DELETE',   'Xóa quy tắc giá',                  'PRICING_RULE',            'Pricing Rule',   33),
-- Group Management
('GROUP_VIEW',            'Xem danh sách nhóm/đại lý',        'GROUP_MANAGEMENT',        'Group',          40),
('GROUP_CREATE',          'Tạo nhóm/đại lý mới',              'GROUP_MANAGEMENT',        'Group',          41),
('GROUP_UPDATE',          'Cập nhật nhóm/đại lý',             'GROUP_MANAGEMENT',        'Group',          42),
('GROUP_DELETE',          'Xóa nhóm/đại lý',                  'GROUP_MANAGEMENT',        'Group',          43),
-- Group Contact
('GROUP_CONTACT_VIEW',    'Xem liên hệ nhóm',                 'GROUP_CONTACT',           'Group Contact',  45),
('GROUP_CONTACT_CREATE',  'Thêm liên hệ nhóm',                'GROUP_CONTACT',           'Group Contact',  46),
('GROUP_CONTACT_UPDATE',  'Cập nhật liên hệ nhóm',            'GROUP_CONTACT',           'Group Contact',  47),
('GROUP_CONTACT_DELETE',  'Xóa liên hệ nhóm',                 'GROUP_CONTACT',           'Group Contact',  48),
-- Group Plan Assignment
('GROUP_ASSIGNMENT_VIEW',      'Xem gán gói cho nhóm',           'GROUP_PLAN_ASSIGNMENT',   'Group Assignment', 50),
('GROUP_ASSIGNMENT_CREATE',    'Tạo yêu cầu gán gói',            'GROUP_PLAN_ASSIGNMENT',   'Group Assignment', 51),
('GROUP_ASSIGNMENT_APPROVE',   'Phê duyệt gán gói',              'GROUP_PLAN_ASSIGNMENT',   'Group Assignment', 52),
('GROUP_ASSIGNMENT_ACTIVATE',  'Kích hoạt gán gói',              'GROUP_PLAN_ASSIGNMENT',   'Group Assignment', 53),
('GROUP_ASSIGNMENT_STOP',      'Dừng gán gói',                   'GROUP_PLAN_ASSIGNMENT',   'Group Assignment', 54),
-- Retail Plan Schedule
('RETAIL_SCHEDULE_VIEW',       'Xem lịch gói retail',             'RETAIL_PLAN_SCHEDULE',    'Retail Schedule',   60),
('RETAIL_SCHEDULE_CREATE',     'Tạo lịch gói retail',             'RETAIL_PLAN_SCHEDULE',    'Retail Schedule',   61),
('RETAIL_SCHEDULE_APPROVE',    'Phê duyệt lịch gói retail',       'RETAIL_PLAN_SCHEDULE',    'Retail Schedule',   62),
('RETAIL_SCHEDULE_ACTIVATE',   'Kích hoạt lịch gói retail',       'RETAIL_PLAN_SCHEDULE',    'Retail Schedule',   63),
-- Subscription
('SUBSCRIPTION_VIEW',          'Xem danh sách subscription',      'SUBSCRIPTION',            'Subscription',      70),
('SUBSCRIPTION_CREATE',        'Tạo subscription mới',            'SUBSCRIPTION',            'Subscription',      71),
('SUBSCRIPTION_CANCEL',        'Hủy subscription',                'SUBSCRIPTION',            'Subscription',      72),
('SUBSCRIPTION_SUSPEND',       'Tạm ngưng subscription',           'SUBSCRIPTION',            'Subscription',      73),
('SUBSCRIPTION_RENEW',         'Gia hạn subscription',             'SUBSCRIPTION',            'Subscription',      74),
-- Certificate
('CERTIFICATE_VIEW',           'Xem chứng chỉ số',                'CERTIFICATE',             'Certificate',       80),
('CERTIFICATE_PROVISION',      'Cấp chứng chỉ số',                'CERTIFICATE',             'Certificate',       81),
('CERTIFICATE_REVOKE',         'Thu hồi chứng chỉ số',            'CERTIFICATE',             'Certificate',       82),
-- Approval Request
('APPROVAL_VIEW',              'Xem yêu cầu phê duyệt',           'APPROVAL_REQUEST',        'Approval',          90),
('APPROVAL_APPROVE',           'Phê duyệt yêu cầu',               'APPROVAL_REQUEST',        'Approval',          91),
('APPROVAL_REJECT',            'Từ chối yêu cầu',                  'APPROVAL_REQUEST',        'Approval',          92),
-- Usage Aggregate
('USAGE_AGGREGATE_VIEW',       'Xem thống kê sử dụng',             'USAGE_AGGREGATE',         'Usage Aggregate',   100),
('USAGE_AGGREGATE_UPSERT',     'Cập nhật thống kê sử dụng',        'USAGE_AGGREGATE',         'Usage Aggregate',   101),
-- Settlement
('SETTLEMENT_VIEW',            'Xem báo cáo đối soát',             'SETTLEMENT',              'Settlement',        110),
('SETTLEMENT_GENERATE',        'Tạo báo cáo đối soát',             'SETTLEMENT',              'Settlement',        111),
('SETTLEMENT_FINALIZE',        'Chốt báo cáo đối soát',            'SETTLEMENT',              'Settlement',        112),
('SETTLEMENT_EXPORT',          'Xuất báo cáo đối soát',            'SETTLEMENT',              'Settlement',        113),
-- Payment
('PAYMENT_VIEW',               'Xem lịch sử thanh toán',           'PAYMENT',                 'Payment',           120),
('PAYMENT_RECORD',             'Ghi nhận thanh toán',              'PAYMENT',                 'Payment',           121),
-- System Setting
('SYSTEM_SETTING_VIEW',        'Xem cấu hình hệ thống',            'SYSTEM_SETTING',          'System Setting',    130),
('SYSTEM_SETTING_UPDATE',      'Cập nhật cấu hình hệ thống',       'SYSTEM_SETTING',          'System Setting',    131),
-- Audit Log
('AUDIT_LOG_VIEW',             'Xem nhật ký hệ thống',             'AUDIT_LOG',               'Audit Log',         140),
('AUDIT_TIMELINE_VIEW',        'Xem timeline audit',               'AUDIT_LOG',               'Audit Log',         141),
-- Dashboard
('DASHBOARD_VIEW',             'Xem bảng điều khiển',              'DASHBOARD',               'Dashboard',         150);

-- ============================================================
-- 2. ROLE PERMISSIONS
-- ============================================================
-- Admin (role_id=1): ALL permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1, permission_id FROM permissions;

-- User (role_id=2): view-only + limited actions
INSERT INTO role_permissions (role_id, permission_id)
SELECT 2, permission_id FROM permissions
WHERE permission_key IN (
    'USER_VIEW','PLAN_TEMPLATE_VIEW','PRICING_RULE_VIEW','GROUP_VIEW',
    'GROUP_CONTACT_VIEW','GROUP_ASSIGNMENT_VIEW','RETAIL_SCHEDULE_VIEW',
    'SUBSCRIPTION_VIEW','SUBSCRIPTION_CREATE','CERTIFICATE_VIEW','CERTIFICATE_PROVISION',
    'APPROVAL_VIEW','USAGE_AGGREGATE_VIEW','SETTLEMENT_VIEW','PAYMENT_VIEW',
    'AUDIT_LOG_VIEW','AUDIT_TIMELINE_VIEW','DASHBOARD_VIEW'
);

-- ============================================================
-- 3. SYSTEM SETTINGS
-- ============================================================
INSERT INTO system_settings (setting_key, setting_value, setting_type, category, description) VALUES
-- Auth
('auth.max_failed_login_attempts',    '5',             'INTEGER',  'AUTH',     'Số lần đăng nhập sai tối đa trước khi khóa'),
('auth.lock_duration_minutes',        '30',            'INTEGER',  'AUTH',     'Thời gian khóa tài khoản (phút)'),
('auth.password_min_length',          '8',             'INTEGER',  'AUTH',     'Độ dài mật khẩu tối thiểu'),
('auth.password_require_uppercase',   'true',          'BOOLEAN',  'AUTH',     'Yêu cầu chữ hoa'),
('auth.password_require_lowercase',   'true',          'BOOLEAN',  'AUTH',     'Yêu cầu chữ thường'),
('auth.password_require_digit',       'true',          'BOOLEAN',  'AUTH',     'Yêu cầu số'),
('auth.password_require_special',     'true',          'BOOLEAN',  'AUTH',     'Yêu cầu ký tự đặc biệt'),
('auth.password_history_count',       '5',             'INTEGER',  'AUTH',     'Số mật khẩu cũ lưu trữ'),
('auth.refresh_token_expire_hours',   '24',            'INTEGER',  'AUTH',     'Refresh token hết hạn (giờ)'),
('auth.password_reset_expire_minutes','30',            'INTEGER',  'AUTH',     'Token reset mật khẩu hết hạn (phút)'),
-- Certificate
('cert.default_validity_months',      '12',            'INTEGER',  'CERT',     'Thời hạn chứng chỉ mặc định (tháng)'),
('cert.max_retry_count',              '3',             'INTEGER',  'CERT',     'Số lần thử cấp chứng chỉ tối đa'),
('cert.retry_interval_minutes',       '5',             'INTEGER',  'CERT',     'Khoảng thời gian thử lại (phút)'),
-- Subscription
('sub.default_grace_period_days',     '7',             'INTEGER',  'SUB',      'Số ngày gia hạn sau hết hạn'),
('sub.auto_expire_enabled',           'true',          'BOOLEAN',  'SUB',      'Tự động hết hạn subscription'),
('sub.max_quota_overflow_percent',    '10',            'INTEGER',  'SUB',      'Phần trăm vượt quota cho phép'),
-- Billing
('billing.currency_default',          'VND',           'STRING',   'BILLING',  'Tiền tệ mặc định'),
('billing.tax_percent',               '10',            'DECIMAL',  'BILLING',  'Phần trăm thuế'),
('billing.settlement_auto_finalize',  'false',         'BOOLEAN',  'BILLING',  'Tự động chốt đối soát'),
('billing.settlement_period_days',    '30',            'INTEGER',  'BILLING',  'Chu kỳ đối soát (ngày)'),
-- Usage
('usage.alert_threshold_percent',     '80',            'INTEGER',  'USAGE',    'Ngưỡng cảnh báo sử dụng (%)'),
('usage.aggregate_cron_expression',   '0 0 2 * * ?',   'STRING',   'USAGE',    'Cron tổng hợp usage'),
-- Notification
('notification.email_enabled',        'true',          'BOOLEAN',  'NOTIFICATION', 'Bật gửi email thông báo'),
('notification.usage_alert_enabled',  'true',          'BOOLEAN',  'NOTIFICATION', 'Bật cảnh báo sử dụng'),
('notification.expiry_alert_days',    '7',             'INTEGER',  'NOTIFICATION', 'Cảnh báo hết hạn trước (ngày)'),
-- System
('system.maintenance_mode',           'false',         'BOOLEAN',  'SYSTEM',   'Chế độ bảo trì'),
('system.max_export_rows',            '10000',         'INTEGER',  'SYSTEM',   'Số dòng tối đa xuất báo cáo'),
('system.default_page_size',          '20',            'INTEGER',  'SYSTEM',   'Kích thước trang mặc định'),
('system.max_page_size',              '100',           'INTEGER',  'SYSTEM',   'Kích thước trang tối đa'),
('system.timezone',                   'Asia/Bangkok',  'STRING',   'SYSTEM',   'Múi giờ hệ thống'),
('system.app_name',                   'Billing-CA',    'STRING',   'SYSTEM',   'Tên ứng dụng'),
('system.app_version',                '1.0.0',         'STRING',   'SYSTEM',   'Phiên bản ứng dụng');

-- ============================================================
-- 4. PLAN TEMPLATES
-- ============================================================
INSERT INTO plan_templates (plan_template_id, plan_code, plan_name, description, customer_segment, template_scope, status, effective_from, effective_to, is_visible, allow_bulk_signing, allow_api_access, created_by, version_no) VALUES
(1, 'RETAIL-INDIVIDUAL-12M',   'Gói Cá Nhân 12 Tháng',              'Gói chứng chỉ số cá nhân, thời hạn 12 tháng',              'INDIVIDUAL', 'PUBLIC',          'AVAILABLE', '2025-01-01', '2026-12-31', 1, 0, 1, 'SYSTEM', 1),
(2, 'RETAIL-INDIVIDUAL-6M',    'Gói Cá Nhân 6 Tháng',               'Gói chứng chỉ số cá nhân, thời hạn 6 tháng',               'INDIVIDUAL', 'PUBLIC',          'AVAILABLE', '2025-01-01', '2026-12-31', 1, 0, 1, 'SYSTEM', 1),
(3, 'RETAIL-INDIVIDUAL-1M',    'Gói Cá Nhân 1 Tháng',               'Gói chứng chỉ số cá nhân, thời hạn 1 tháng, dùng thử',    'INDIVIDUAL', 'PUBLIC',          'AVAILABLE', '2025-01-01', '2026-12-31', 1, 0, 1, 'SYSTEM', 1),
(4, 'GROUP-ORG-12M',           'Gói Doanh Nghiệp 12 Tháng',         'Gói chứng chỉ số tổ chức, thời hạn 12 tháng',              'GROUP',      'PUBLIC',          'AVAILABLE', '2025-01-01', '2026-12-31', 1, 1, 1, 'SYSTEM', 1),
(5, 'GROUP-ORG-6M',            'Gói Doanh Nghiệp 6 Tháng',          'Gói chứng chỉ số tổ chức, thời hạn 6 tháng',               'GROUP',      'PUBLIC',          'AVAILABLE', '2025-01-01', '2026-12-31', 1, 1, 1, 'SYSTEM', 1),
(6, 'GROUP-ORG-1M',            'Gói Doanh Nghiệp 1 Tháng',          'Gói chứng chỉ số tổ chức, thời hạn 1 tháng, dùng thử',    'GROUP',      'PUBLIC',          'AVAILABLE', '2025-01-01', '2026-12-31', 1, 1, 1, 'SYSTEM', 1),
(7, 'GROUP-INDIVIDUAL-ORG-12M','Gói Cá Nhân Trong Tổ Chức 12 Tháng','Gói chứng chỉ cá nhân thuộc tổ chức, thời hạn 12 tháng',   'GROUP',      'PARTNER_PRIVATE', 'AVAILABLE', '2025-01-01', '2026-12-31', 1, 1, 1, 'SYSTEM', 1),
(8, 'INTERNAL-PLAN',           'Gói Nội Bộ',                        'Gói nội bộ hệ thống',                                      'INDIVIDUAL', 'SYSTEM',          'AVAILABLE', '2025-01-01', '2099-12-31', 0, 0, 1, 'SYSTEM', 1);

-- ============================================================
-- 5. PLAN PRICING RULES
-- ============================================================
INSERT INTO plan_pricing_rules (plan_template_id, subject_type, certificate_validity_value, certificate_validity_unit, pricing_metric, range_min, range_max, unit_price, currency, quota_total, sort_order, is_active) VALUES
-- Retail Individual 12M
(1, 'INDIVIDUAL', 12, 'MONTH', 'CERTIFICATE_COUNT', 1,   10,  150000.00, 'VND', NULL, 1, 1),
(1, 'INDIVIDUAL', 12, 'MONTH', 'CERTIFICATE_COUNT', 11,  50,  130000.00, 'VND', NULL, 2, 1),
(1, 'INDIVIDUAL', 12, 'MONTH', 'CERTIFICATE_COUNT', 51,  100, 110000.00, 'VND', NULL, 3, 1),
(1, 'INDIVIDUAL', 12, 'MONTH', 'CERTIFICATE_COUNT', 101, NULL, 90000.00, 'VND', NULL, 4, 1),
-- Retail Individual 6M
(2, 'INDIVIDUAL', 6, 'MONTH', 'CERTIFICATE_COUNT', 1,   10,  100000.00, 'VND', NULL, 1, 1),
(2, 'INDIVIDUAL', 6, 'MONTH', 'CERTIFICATE_COUNT', 11,  50,  85000.00,  'VND', NULL, 2, 1),
(2, 'INDIVIDUAL', 6, 'MONTH', 'CERTIFICATE_COUNT', 51,  100, 70000.00,  'VND', NULL, 3, 1),
(2, 'INDIVIDUAL', 6, 'MONTH', 'CERTIFICATE_COUNT', 101, NULL, 55000.00, 'VND', NULL, 4, 1),
-- Retail Individual 1M (Trial)
(3, 'INDIVIDUAL', 1, 'MONTH', 'CERTIFICATE_COUNT', 1,   5,   50000.00,  'VND', NULL, 1, 1),
(3, 'INDIVIDUAL', 1, 'MONTH', 'CERTIFICATE_COUNT', 6,   NULL, 40000.00, 'VND', NULL, 2, 1),
-- Group Organization 12M
(4, 'ORGANIZATION', 12, 'MONTH', 'CERTIFICATE_COUNT', 1,   50,  200000.00, 'VND', NULL, 1, 1),
(4, 'ORGANIZATION', 12, 'MONTH', 'CERTIFICATE_COUNT', 51,  200, 170000.00, 'VND', NULL, 2, 1),
(4, 'ORGANIZATION', 12, 'MONTH', 'CERTIFICATE_COUNT', 201, 500, 140000.00, 'VND', NULL, 3, 1),
(4, 'ORGANIZATION', 12, 'MONTH', 'CERTIFICATE_COUNT', 501, NULL, 110000.00, 'VND', NULL, 4, 1),
-- Group Organization 6M
(5, 'ORGANIZATION', 6, 'MONTH', 'CERTIFICATE_COUNT', 1,   50,  160000.00, 'VND', NULL, 1, 1),
(5, 'ORGANIZATION', 6, 'MONTH', 'CERTIFICATE_COUNT', 51,  200, 130000.00, 'VND', NULL, 2, 1),
(5, 'ORGANIZATION', 6, 'MONTH', 'CERTIFICATE_COUNT', 201, 500, 100000.00, 'VND', NULL, 3, 1),
(5, 'ORGANIZATION', 6, 'MONTH', 'CERTIFICATE_COUNT', 501, NULL, 80000.00,  'VND', NULL, 4, 1),
-- Group Organization 1M (Trial)
(6, 'ORGANIZATION', 1, 'MONTH', 'CERTIFICATE_COUNT', 1,   10,  80000.00,  'VND', NULL, 1, 1),
(6, 'ORGANIZATION', 1, 'MONTH', 'CERTIFICATE_COUNT', 11,  NULL, 60000.00, 'VND', NULL, 2, 1),
-- Group Individual of Org 12M
(7, 'INDIVIDUAL_OF_ORG', 12, 'MONTH', 'SIGNING_COUNT', 1,   100, 5000.00,  'VND', NULL, 1, 1),
(7, 'INDIVIDUAL_OF_ORG', 12, 'MONTH', 'SIGNING_COUNT', 101, 500, 4000.00,  'VND', NULL, 2, 1),
(7, 'INDIVIDUAL_OF_ORG', 12, 'MONTH', 'SIGNING_COUNT', 501, NULL, 3000.00, 'VND', NULL, 3, 1),
-- Internal Plan
(8, 'INDIVIDUAL', 12, 'MONTH', 'CERTIFICATE_COUNT', 1, NULL, 0.00, 'VND', 100, 1, 1);

-- ============================================================
-- 6. ADDITIONAL ROLES
-- ============================================================
INSERT INTO roles (role_id, role_name, display_name, description, is_system_role) VALUES
(3, 'ROLE_BILLING_ADMIN', 'Billing Administrator', 'Quản trị viên thanh toán',    1),
(4, 'ROLE_GROUP_ADMIN',   'Group Administrator',   'Quản trị viên nhóm/đại lý',   1),
(5, 'ROLE_OPERATOR',      'Operator',              'Người vận hành',               1),
(6, 'ROLE_VIEWER',        'Viewer',                'Người xem',                    1);

-- Billing Admin permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT 3, permission_id FROM permissions
WHERE permission_key IN (
    'DASHBOARD_VIEW','PLAN_TEMPLATE_VIEW','PRICING_RULE_VIEW','SUBSCRIPTION_VIEW',
    'SETTLEMENT_VIEW','SETTLEMENT_GENERATE','SETTLEMENT_FINALIZE','SETTLEMENT_EXPORT',
    'PAYMENT_VIEW','PAYMENT_RECORD','USAGE_AGGREGATE_VIEW','AUDIT_LOG_VIEW','AUDIT_TIMELINE_VIEW'
);

-- Group Admin permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT 4, permission_id FROM permissions
WHERE permission_key IN (
    'DASHBOARD_VIEW',
    'GROUP_VIEW','GROUP_CREATE','GROUP_UPDATE',
    'GROUP_CONTACT_VIEW','GROUP_CONTACT_CREATE','GROUP_CONTACT_UPDATE','GROUP_CONTACT_DELETE',
    'GROUP_ASSIGNMENT_VIEW','GROUP_ASSIGNMENT_CREATE',
    'SUBSCRIPTION_VIEW','CERTIFICATE_VIEW','USAGE_AGGREGATE_VIEW','PAYMENT_VIEW'
);

-- Operator permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT 5, permission_id FROM permissions
WHERE permission_key IN (
    'DASHBOARD_VIEW','PLAN_TEMPLATE_VIEW','PRICING_RULE_VIEW',
    'GROUP_VIEW','GROUP_ASSIGNMENT_VIEW','RETAIL_SCHEDULE_VIEW',
    'SUBSCRIPTION_VIEW','SUBSCRIPTION_CREATE',
    'CERTIFICATE_VIEW','CERTIFICATE_PROVISION',
    'APPROVAL_VIEW','USAGE_AGGREGATE_VIEW','PAYMENT_VIEW'
);

-- Viewer permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT 6, permission_id FROM permissions
WHERE permission_key IN (
    'DASHBOARD_VIEW','USER_VIEW','PLAN_TEMPLATE_VIEW','PRICING_RULE_VIEW',
    'GROUP_VIEW','GROUP_ASSIGNMENT_VIEW','RETAIL_SCHEDULE_VIEW',
    'SUBSCRIPTION_VIEW','CERTIFICATE_VIEW','APPROVAL_VIEW',
    'USAGE_AGGREGATE_VIEW','SETTLEMENT_VIEW','PAYMENT_VIEW',
    'AUDIT_LOG_VIEW','AUDIT_TIMELINE_VIEW'
);

-- ============================================================
-- 7. ADDITIONAL USER ACCOUNTS
-- Password for all seed users: Admin@123
-- ============================================================
INSERT INTO user_accounts (user_id, username, email, full_name, password_hash, auth_provider, status, failed_login_attempts, created_by) VALUES
('00000000-0000-0000-0000-000000000002', 'billing_admin', 'billing@rs.local',    'Billing Administrator',  '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5qXvS4p6UwHuu', 'LOCAL', 'ACTIVE', 0, 'SYSTEM'),
('00000000-0000-0000-0000-000000000003', 'group_admin',   'group@rs.local',      'Group Administrator',    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5qXvS4p6UwHuu', 'LOCAL', 'ACTIVE', 0, 'SYSTEM'),
('00000000-0000-0000-0000-000000000004', 'operator1',     'op1@rs.local',        'Operator One',           '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5qXvS4p6UwHuu', 'LOCAL', 'ACTIVE', 0, 'SYSTEM'),
('00000000-0000-0000-0000-000000000005', 'operator2',     'op2@rs.local',        'Operator Two',           '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5qXvS4p6UwHuu', 'LOCAL', 'ACTIVE', 0, 'SYSTEM'),
('00000000-0000-0000-0000-000000000006', 'viewer1',       'viewer@rs.local',     'Viewer User',            '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5qXvS4p6UwHuu', 'LOCAL', 'ACTIVE', 0, 'SYSTEM'),
('00000000-0000-0000-0000-000000000007', 'retail_user1',  'retail1@example.com', 'Retail User One',        '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5qXvS4p6UwHuu', 'LOCAL', 'ACTIVE', 0, 'SYSTEM'),
('00000000-0000-0000-0000-000000000008', 'retail_user2',  'retail2@example.com', 'Retail User Two',        '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5qXvS4p6UwHuu', 'LOCAL', 'ACTIVE', 0, 'SYSTEM'),
('00000000-0000-0000-0000-000000000009', 'retail_user3',  'retail3@example.com', 'Retail User Three',      '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5qXvS4p6UwHuu', 'LOCAL', 'ACTIVE', 0, 'SYSTEM'),
('00000000-0000-0000-0000-000000000010', 'groupuser_abc', 'abc@company.com',     'ABC Company User',       '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5qXvS4p6UwHuu', 'LOCAL', 'ACTIVE', 0, 'SYSTEM'),
('00000000-0000-0000-0000-000000000011', 'groupuser_xyz', 'xyz@company.com',     'XYZ Company User',       '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5qXvS4p6UwHuu', 'LOCAL', 'ACTIVE', 0, 'SYSTEM'),
('00000000-0000-0000-0000-000000000012', 'groupuser_def', 'def@company.com',     'DEF Company User',       '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5qXvS4p6UwHuu', 'LOCAL', 'ACTIVE', 0, 'SYSTEM');

-- ============================================================
-- 8. USER ROLES
-- ============================================================
INSERT INTO user_roles (user_id, role_id) VALUES
('00000000-0000-0000-0000-000000000002', 3),  -- billing_admin -> BILLING_ADMIN
('00000000-0000-0000-0000-000000000003', 4),  -- group_admin -> GROUP_ADMIN
('00000000-0000-0000-0000-000000000004', 5),  -- operator1 -> OPERATOR
('00000000-0000-0000-0000-000000000005', 5),  -- operator2 -> OPERATOR
('00000000-0000-0000-0000-000000000006', 6),  -- viewer1 -> VIEWER
('00000000-0000-0000-0000-000000000007', 2),  -- retail_user1 -> USER
('00000000-0000-0000-0000-000000000008', 2),  -- retail_user2 -> USER
('00000000-0000-0000-0000-000000000009', 2),  -- retail_user3 -> USER
('00000000-0000-0000-0000-000000000010', 2),  -- groupuser_abc -> USER
('00000000-0000-0000-0000-000000000011', 2),  -- groupuser_xyz -> USER
('00000000-0000-0000-0000-000000000012', 2);  -- groupuser_def -> USER

-- ============================================================
-- 9. GROUPS
-- ============================================================
INSERT INTO `groups` (group_id, group_code, group_name, username, password, ref_contract_no, status, created_by) VALUES
(1, 'GRP-COMP001', 'Công ty TNHH ABC',    'grp_abc', 'hashed_pwd_abc', 'HD-2025-001', 'ACTIVE',   '00000000-0000-0000-0000-000000000001'),
(2, 'GRP-COMP002', 'Công ty Cổ phần XYZ', 'grp_xyz', 'hashed_pwd_xyz', 'HD-2025-002', 'ACTIVE',   '00000000-0000-0000-0000-000000000001'),
(3, 'GRP-COMP003', 'Tập đoàn DEF',         'grp_def', 'hashed_pwd_def', 'HD-2025-003', 'ACTIVE',   '00000000-0000-0000-0000-000000000001'),
(4, 'GRP-COMP004', 'Công ty TNHH GHI',     'grp_ghi', 'hashed_pwd_ghi', NULL,          'INACTIVE', '00000000-0000-0000-0000-000000000001');

-- ============================================================
-- 10. GROUP CONTACTS
-- ============================================================
INSERT INTO group_contacts (group_id, contact_type, email, full_name, phone, is_primary, receive_usage_alert, is_active) VALUES
(1, 'CONTRACT', 'contract@abc.com',  'Nguyễn Văn A', '0901234567', 1, 1, 1),
(1, 'BILLING',  'billing@abc.com',   'Trần Thị B',   '0901234568', 1, 1, 1),
(1, 'SUPPORT',  'support@abc.com',   'Lê Văn C',     '0901234569', 0, 0, 1),
(1, 'PIC',      'pic@abc.com',       'Phạm Thị D',   '0901234570', 1, 1, 1),
(2, 'CONTRACT', 'contract@xyz.com',  'Hoàng Văn E',  '0912345678', 1, 1, 1),
(2, 'BILLING',  'billing@xyz.com',   'Ngô Thị F',    '0912345679', 1, 1, 1),
(2, 'PIC',      'pic@xyz.com',       'Vũ Văn G',     '0912345680', 1, 0, 1),
(3, 'CONTRACT', 'contract@def.com',  'Đặng Văn H',   '0923456789', 1, 1, 1),
(3, 'BILLING',  'billing@def.com',   'Bùi Thị I',    '0923456790', 1, 1, 1),
(3, 'SUPPORT',  'support@def.com',   'Trịnh Văn K',  '0923456791', 0, 1, 1),
(3, 'PIC',      'pic@def.com',       'Lý Thị L',     '0923456792', 1, 1, 1),
(4, 'CONTRACT', 'contract@ghi.com',  'Phan Văn M',   '0934567890', 1, 0, 1),
(4, 'BILLING',  'billing@ghi.com',   'Đỗ Thị N',     '0934567891', 1, 0, 1);

-- ============================================================
-- 11. GROUP MEMBERS
-- ============================================================
INSERT INTO group_members (group_id, user_id, role, added_by, member_start_date, member_end_date) VALUES
(1, '00000000-0000-0000-0000-000000000003', 'OPERATOR', '00000000-0000-0000-0000-000000000001', '2025-01-15', '2026-01-15'),
(1, '00000000-0000-0000-0000-000000000004', 'MEMBER',   '00000000-0000-0000-0000-000000000003', '2025-01-15', '2026-01-15'),
(1, '00000000-0000-0000-0000-000000000005', 'MEMBER',   '00000000-0000-0000-0000-000000000003', '2025-02-01', '2026-02-01'),
(1, '00000000-0000-0000-0000-000000000010', 'MEMBER',   '00000000-0000-0000-0000-000000000003', '2025-01-20', '2026-01-15'),
(2, '00000000-0000-0000-0000-000000000004', 'OPERATOR', '00000000-0000-0000-0000-000000000001', '2025-02-01', '2026-02-01'),
(2, '00000000-0000-0000-0000-000000000005', 'MEMBER',   '00000000-0000-0000-0000-000000000004', '2025-02-01', '2026-02-01'),
(2, '00000000-0000-0000-0000-000000000011', 'MEMBER',   '00000000-0000-0000-0000-000000000004', '2025-02-15', '2026-02-01'),
(3, '00000000-0000-0000-0000-000000000004', 'OPERATOR', '00000000-0000-0000-0000-000000000001', '2025-03-01', '2026-03-01'),
(3, '00000000-0000-0000-0000-000000000005', 'MEMBER',   '00000000-0000-0000-0000-000000000004', '2025-03-01', '2026-03-01'),
(3, '00000000-0000-0000-0000-000000000012', 'MEMBER',   '00000000-0000-0000-0000-000000000004', '2025-03-10', '2026-03-01');

-- ============================================================
-- 12. GROUP PLAN ASSIGNMENTS
-- ============================================================
INSERT INTO group_plan_assignments (group_plan_assignment_id, group_id, plan_template_id, assignment_status, requested_by, requested_at, approved_by, approved_at, apply_from, apply_to, activated_at, stopped_at, stop_reason) VALUES
(1, 1, 4, 'ACTIVE',    '00000000-0000-0000-0000-000000000003', '2025-01-10 08:00:00', '00000000-0000-0000-0000-000000000001', '2025-01-11 09:00:00', '2025-01-15', '2026-01-15', '2025-01-15 00:00:00', NULL, NULL),
(2, 1, 7, 'ACTIVE',    '00000000-0000-0000-0000-000000000003', '2025-01-10 08:30:00', '00000000-0000-0000-0000-000000000001', '2025-01-11 09:30:00', '2025-01-15', '2026-01-15', '2025-01-15 00:00:00', NULL, NULL),
(3, 2, 4, 'ACTIVE',    '00000000-0000-0000-0000-000000000004', '2025-01-20 10:00:00', '00000000-0000-0000-0000-000000000001', '2025-01-21 11:00:00', '2025-02-01', '2026-02-01', '2025-02-01 00:00:00', NULL, NULL),
(4, 2, 5, 'APPROVED',  '00000000-0000-0000-0000-000000000004', '2025-03-01 14:00:00', '00000000-0000-0000-0000-000000000001', '2025-03-02 09:00:00', '2025-04-01', '2025-10-01', NULL, NULL, NULL),
(5, 3, 4, 'ACTIVE',    '00000000-0000-0000-0000-000000000004', '2025-02-15 09:00:00', '00000000-0000-0000-0000-000000000001', '2025-02-16 10:00:00', '2025-03-01', '2026-03-01', '2025-03-01 00:00:00', NULL, NULL),
(6, 3, 6, 'REQUESTED', '00000000-0000-0000-0000-000000000004', '2025-04-01 11:00:00', NULL, NULL, '2025-05-01', '2025-06-01', NULL, NULL, NULL),
(7, 1, 5, 'STOPPED',   '00000000-0000-0000-0000-000000000003', '2024-06-01 08:00:00', '00000000-0000-0000-0000-000000000001', '2024-06-02 09:00:00', '2024-07-01', '2024-12-31', '2024-07-01 00:00:00', '2025-01-10 00:00:00', 'Chuyển sang gói 12 tháng'),
(8, 4, 6, 'REJECTED',  '00000000-0000-0000-0000-000000000004', '2025-03-10 15:00:00', NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- ============================================================
-- 13. RETAIL PLAN SCHEDULES
-- ============================================================
INSERT INTO retail_plan_schedules (retail_plan_schedule_id, plan_template_id, schedule_status, apply_from, apply_to, requested_by, requested_at, approved_by, approved_at) VALUES
(1, 1, 'ACTIVE',    '2025-01-01', '2025-06-30', '00000000-0000-0000-0000-000000000001', '2024-12-20 08:00:00', '00000000-0000-0000-0000-000000000001', '2024-12-21 09:00:00'),
(2, 2, 'ACTIVE',    '2025-01-01', '2025-06-30', '00000000-0000-0000-0000-000000000001', '2024-12-20 08:30:00', '00000000-0000-0000-0000-000000000001', '2024-12-21 09:30:00'),
(3, 3, 'ACTIVE',    '2025-01-01', '2025-03-31', '00000000-0000-0000-0000-000000000001', '2024-12-20 09:00:00', '00000000-0000-0000-0000-000000000001', '2024-12-21 10:00:00'),
(4, 1, 'AVAILABLE', '2025-07-01', '2025-12-31', '00000000-0000-0000-0000-000000000001', '2025-05-01 08:00:00', NULL, NULL),
(5, 2, 'REQUESTED', '2025-07-01', '2025-12-31', '00000000-0000-0000-0000-000000000001', '2025-05-01 08:30:00', NULL, NULL),
(6, 3, 'INACTIVE',  '2024-07-01', '2024-09-30', '00000000-0000-0000-0000-000000000001', '2024-06-01 08:00:00', '00000000-0000-0000-0000-000000000001', '2024-06-02 09:00:00');

-- ============================================================
-- 14. SUBSCRIPTIONS
-- ============================================================
INSERT INTO subscriptions (subscription_id, subscriber_type, user_id, group_id, plan_template_id, pricing_rule_id, group_plan_assignment_id, retail_plan_schedule_id, source_type, status, start_date, end_date, signing_quota_total, signing_quota_used, activated_by, payment_reference) VALUES
-- Individual retail
(1,  'INDIVIDUAL', '00000000-0000-0000-0000-000000000007', NULL, 1, 1,  NULL, 1, 'RETAIL_PURCHASE', 'ACTIVE',    '2025-01-15', '2026-01-15', 10,   3, 'SYSTEM', 'PAY-2025-001'),
(2,  'INDIVIDUAL', '00000000-0000-0000-0000-000000000008', NULL, 2, 5,  NULL, 2, 'RETAIL_PURCHASE', 'ACTIVE',    '2025-02-01', '2025-08-01', 50,  15, 'SYSTEM', 'PAY-2025-002'),
(3,  'INDIVIDUAL', '00000000-0000-0000-0000-000000000009', NULL, 3, 9,  NULL, 3, 'RETAIL_PURCHASE', 'EXPIRED',   '2024-07-01', '2024-10-01', 5,    5, 'SYSTEM', 'PAY-2024-010'),
-- Group assignments
(4,  'GROUP',      NULL, 1, 4, 11, 1, NULL, 'GROUP_ASSIGNMENT', 'ACTIVE',    '2025-01-15', '2026-01-15', 200,  85, '00000000-0000-0000-0000-000000000001', 'PAY-2025-G001'),
(5,  'GROUP',      NULL, 1, 7, 19, 2, NULL, 'GROUP_ASSIGNMENT', 'ACTIVE',    '2025-01-15', '2026-01-15', 500, 120, '00000000-0000-0000-0000-000000000001', 'PAY-2025-G002'),
(6,  'GROUP',      NULL, 2, 4, 11, 3, NULL, 'GROUP_ASSIGNMENT', 'ACTIVE',    '2025-02-01', '2026-02-01', 100,  42, '00000000-0000-0000-0000-000000000001', 'PAY-2025-G003'),
(7,  'GROUP',      NULL, 3, 4, 11, 5, NULL, 'GROUP_ASSIGNMENT', 'ACTIVE',    '2025-03-01', '2026-03-01', 500, 230, '00000000-0000-0000-0000-000000000001', 'PAY-2025-G004'),
-- Other statuses
(8,  'INDIVIDUAL', '00000000-0000-0000-0000-000000000007', NULL, 3, 9,  NULL, 3, 'RETAIL_PURCHASE', 'SUSPENDED', '2025-03-01', '2025-04-01', 5,    2, 'SYSTEM', NULL),
(9,  'INDIVIDUAL', '00000000-0000-0000-0000-000000000008', NULL, 1, 1,  NULL, 4, 'RETAIL_PURCHASE', 'PENDING',   '2025-07-01', '2026-07-01', 20,   0, NULL, NULL),
(10, 'GROUP',      NULL, 1, 5, 15, 7, NULL, 'GROUP_ASSIGNMENT', 'CANCELLED', '2024-07-01', '2024-12-31', 100,  30, '00000000-0000-0000-0000-000000000001', 'PAY-2024-G010'),
(11, 'INDIVIDUAL', '00000000-0000-0000-0000-000000000009', NULL, 2, 5,  NULL, 2, 'RETAIL_PURCHASE', 'ACTIVE',    '2025-03-15', '2025-09-15', 30,   8, 'SYSTEM', 'PAY-2025-011');

-- ============================================================
-- 15. CERTIFICATE PROVISIONING RECORDS
-- ============================================================
INSERT INTO certificate_provisioning_records (id, subscription_id, group_plan_assignment_id, pricing_rule_id, user_id, request_id, status, cert_type, certificate_id, key_id, issued_at, expires_at, retry_count, usage_count, last_attempted_at, failure_reason) VALUES
(1,  1,  NULL, 1,  '00000000-0000-0000-0000-000000000007', 'req-001', 'COMPLETED', 1, 'CERT-IND-001', 'KEY-001', '2025-01-15 10:00:00', '2026-01-15 10:00:00', 0, 3,  NULL, NULL),
(2,  2,  NULL, 5,  '00000000-0000-0000-0000-000000000008', 'req-002', 'COMPLETED', 1, 'CERT-IND-002', 'KEY-002', '2025-02-01 11:00:00', '2025-08-01 11:00:00', 0, 15, NULL, NULL),
(3,  3,  NULL, 9,  '00000000-0000-0000-0000-000000000009', 'req-003', 'COMPLETED', 1, 'CERT-IND-003', 'KEY-003', '2024-07-01 09:00:00', '2024-10-01 09:00:00', 0, 5,  NULL, NULL),
(4,  4,  1,    11, '00000000-0000-0000-0000-000000000010', 'req-004', 'COMPLETED', 3, 'CERT-ORG-001', 'KEY-004', '2025-01-16 08:00:00', '2026-01-16 08:00:00', 0, 20, NULL, NULL),
(5,  4,  1,    11, '00000000-0000-0000-0000-000000000004', 'req-005', 'COMPLETED', 1, 'CERT-IND-004', 'KEY-005', '2025-01-17 09:00:00', '2026-01-17 09:00:00', 0, 10, NULL, NULL),
(6,  5,  2,    19, '00000000-0000-0000-0000-000000000005', 'req-006', 'COMPLETED', 2, 'CERT-IO-001',  'KEY-006', '2025-01-18 10:00:00', '2026-01-18 10:00:00', 0, 25, NULL, NULL),
(7,  6,  3,    11, '00000000-0000-0000-0000-000000000011', 'req-007', 'COMPLETED', 3, 'CERT-ORG-002', 'KEY-007', '2025-02-02 08:00:00', '2026-02-02 08:00:00', 0, 15, NULL, NULL),
(8,  7,  5,    11, '00000000-0000-0000-0000-000000000012', 'req-008', 'COMPLETED', 3, 'CERT-ORG-003', 'KEY-008', '2025-03-02 08:00:00', '2026-03-02 08:00:00', 0, 50, NULL, NULL),
(9,  11, NULL, 5,  '00000000-0000-0000-0000-000000000009', 'req-009', 'COMPLETED', 1, 'CERT-IND-005', 'KEY-009', '2025-03-15 10:00:00', '2025-09-15 10:00:00', 0, 8,  NULL, NULL),
(10, 8,  NULL, 9,  '00000000-0000-0000-0000-000000000007', 'req-010', 'FAILED',    1, NULL,           NULL,      NULL,                  NULL,                  3, 0,  '2025-03-01 12:00:00', 'Lỗi kết nối CA server'),
(11, 9,  NULL, 1,  '00000000-0000-0000-0000-000000000008', 'req-011', 'PENDING',   1, NULL,           NULL,      NULL,                  NULL,                  0, 0,  NULL, NULL);

-- ============================================================
-- 16. CERTIFICATE USAGE RECORDS
-- ============================================================
INSERT INTO certificate_usage_records (id, certificate_id, user_id, subscription_id, group_plan_assignment_id, usage_type, quantity, used_at) VALUES
(1,  'CERT-IND-001', '00000000-0000-0000-0000-000000000007', 1,  NULL, 'SIGNING',              1, '2025-01-20 14:30:00'),
(2,  'CERT-IND-001', '00000000-0000-0000-0000-000000000007', 1,  NULL, 'SIGNING',              1, '2025-02-05 09:15:00'),
(3,  'CERT-IND-001', '00000000-0000-0000-0000-000000000007', 1,  NULL, 'SIGNING',              1, '2025-03-10 16:45:00'),
(4,  'CERT-IND-002', '00000000-0000-0000-0000-000000000008', 2,  NULL, 'SIGNING',              1, '2025-02-03 10:00:00'),
(5,  'CERT-IND-002', '00000000-0000-0000-0000-000000000008', 2,  NULL, 'SIGNING',              1, '2025-02-10 11:30:00'),
(6,  'CERT-IND-002', '00000000-0000-0000-0000-000000000008', 2,  NULL, 'SIGNING',              1, '2025-02-15 14:00:00'),
(7,  'CERT-ORG-001', '00000000-0000-0000-0000-000000000010', 4,  1,    'SIGNING',              1, '2025-01-20 08:00:00'),
(8,  'CERT-ORG-001', '00000000-0000-0000-0000-000000000010', 4,  1,    'CERTIFICATE_RENEWED',  1, '2025-02-01 09:00:00'),
(9,  'CERT-IND-004', '00000000-0000-0000-0000-000000000004', 4,  1,    'SIGNING',              1, '2025-01-25 10:30:00'),
(10, 'CERT-IO-001',  '00000000-0000-0000-0000-000000000005', 5,  2,    'SIGNING',              1, '2025-01-22 11:00:00'),
(11, 'CERT-ORG-002', '00000000-0000-0000-0000-000000000011', 6,  3,    'SIGNING',              1, '2025-02-05 08:30:00'),
(12, 'CERT-ORG-003', '00000000-0000-0000-0000-000000000012', 7,  5,    'SIGNING',              1, '2025-03-05 09:00:00'),
(13, 'CERT-IND-005', '00000000-0000-0000-0000-000000000009', 11, NULL, 'SIGNING',              1, '2025-03-20 13:00:00'),
(14, 'CERT-IND-003', '00000000-0000-0000-0000-000000000009', 3,  NULL, 'CERTIFICATE_REVOKED',  1, '2024-10-01 09:00:00');

-- ============================================================
-- 17. USAGE AGGREGATES
-- ============================================================
INSERT INTO usage_aggregates (aggregate_scope, scope_id, period_type, period_key, certificates_created, signing_used, active_certificates, expired_certificates, revoked_certificates, amount_due, currency) VALUES
-- Group 1, Assignment 1 - Monthly aggregates
('GROUP_ASSIGNMENT', 1, 'MONTH', '2025-01', 2, 15, 2, 0, 0, 3000000.00,  'VND'),
('GROUP_ASSIGNMENT', 1, 'MONTH', '2025-02', 1, 20, 3, 0, 0, 2500000.00,  'VND'),
('GROUP_ASSIGNMENT', 1, 'MONTH', '2025-03', 0, 25, 3, 0, 0, 2000000.00,  'VND'),
-- Group 1, Assignment 2 - Monthly aggregates
('GROUP_ASSIGNMENT', 2, 'MONTH', '2025-01', 1, 30, 1, 0, 0, 150000.00,   'VND'),
('GROUP_ASSIGNMENT', 2, 'MONTH', '2025-02', 2, 45, 3, 0, 0, 225000.00,   'VND'),
('GROUP_ASSIGNMENT', 2, 'MONTH', '2025-03', 0, 40, 3, 0, 0, 200000.00,   'VND'),
-- Group 2, Assignment 3
('GROUP_ASSIGNMENT', 3, 'MONTH', '2025-02', 1, 10, 1, 0, 0, 1700000.00,  'VND'),
('GROUP_ASSIGNMENT', 3, 'MONTH', '2025-03', 2, 18, 3, 0, 0, 2800000.00,  'VND'),
-- Group 3, Assignment 5
('GROUP_ASSIGNMENT', 5, 'MONTH', '2025-03', 1, 35, 1, 0, 0, 5500000.00,  'VND'),
-- Retail Plan 1
('RETAIL_PLAN',      1, 'MONTH', '2025-01', 1,  3, 1, 0, 0, 150000.00,   'VND'),
('RETAIL_PLAN',      1, 'MONTH', '2025-02', 0,  5, 1, 0, 0, 250000.00,   'VND'),
-- Retail Plan 2
('RETAIL_PLAN',      2, 'MONTH', '2025-02', 1, 10, 1, 0, 0, 850000.00,   'VND'),
('RETAIL_PLAN',      2, 'MONTH', '2025-03', 1, 12, 2, 0, 0, 1020000.00,  'VND'),
-- Group scope aggregates
('GROUP',            1, 'MONTH', '2025-01', 3, 45, 3, 0, 0, 3150000.00,  'VND'),
('GROUP',            1, 'MONTH', '2025-02', 3, 65, 6, 0, 0, 2725000.00,  'VND'),
('GROUP',            2, 'MONTH', '2025-02', 1, 10, 1, 0, 0, 1700000.00,  'VND'),
('GROUP',            2, 'MONTH', '2025-03', 2, 18, 3, 0, 0, 2800000.00,  'VND'),
('GROUP',            3, 'MONTH', '2025-03', 1, 35, 1, 0, 0, 5500000.00,  'VND'),
-- User scope
('USER',             7, 'MONTH', '2025-01', 1,  3, 1, 0, 0, 150000.00,   'VND'),
('USER',             8, 'MONTH', '2025-02', 1, 10, 1, 0, 0, 850000.00,   'VND');

-- ============================================================
-- 18. APPROVAL REQUESTS
-- ============================================================
INSERT INTO approval_requests (id, request_type, status, requested_by, entity_type, entity_id, request_payload, description, reviewed_by, review_note, reviewed_at) VALUES
(1, 'REQUEST_GROUP_PLAN_ASSIGNMENT', 'APPROVED', '00000000-0000-0000-0000-000000000003', 'GROUP_PLAN_ASSIGNMENT', '1', '{"group_id":1,"plan_template_id":4,"apply_from":"2025-01-15","apply_to":"2026-01-15"}', 'Yêu cầu gán gói DN 12 tháng cho ABC',    '00000000-0000-0000-0000-000000000001', 'Đã duyệt', '2025-01-11 09:00:00'),
(2, 'REQUEST_GROUP_PLAN_ASSIGNMENT', 'APPROVED', '00000000-0000-0000-0000-000000000003', 'GROUP_PLAN_ASSIGNMENT', '2', '{"group_id":1,"plan_template_id":7,"apply_from":"2025-01-15","apply_to":"2026-01-15"}', 'Yêu cầu gán gói CN trong TC cho ABC',    '00000000-0000-0000-0000-000000000001', 'Đã duyệt', '2025-01-11 09:30:00'),
(3, 'REQUEST_GROUP_PLAN_ASSIGNMENT', 'APPROVED', '00000000-0000-0000-0000-000000000004', 'GROUP_PLAN_ASSIGNMENT', '3', '{"group_id":2,"plan_template_id":4,"apply_from":"2025-02-01","apply_to":"2026-02-01"}', 'Yêu cầu gán gói DN 12 tháng cho XYZ',    '00000000-0000-0000-0000-000000000001', 'Đã duyệt', '2025-01-21 11:00:00'),
(4, 'REQUEST_GROUP_PLAN_ASSIGNMENT', 'APPROVED', '00000000-0000-0000-0000-000000000004', 'GROUP_PLAN_ASSIGNMENT', '4', '{"group_id":2,"plan_template_id":5,"apply_from":"2025-04-01","apply_to":"2025-10-01"}', 'Yêu cầu gán gói DN 6 tháng cho XYZ',     '00000000-0000-0000-0000-000000000001', 'Đã duyệt', '2025-03-02 09:00:00'),
(5, 'REQUEST_GROUP_PLAN_ASSIGNMENT', 'APPROVED', '00000000-0000-0000-0000-000000000004', 'GROUP_PLAN_ASSIGNMENT', '5', '{"group_id":3,"plan_template_id":4,"apply_from":"2025-03-01","apply_to":"2026-03-01"}', 'Yêu cầu gán gói DN 12 tháng cho DEF',    '00000000-0000-0000-0000-000000000001', 'Đã duyệt', '2025-02-16 10:00:00'),
(6, 'REQUEST_GROUP_PLAN_ASSIGNMENT', 'PENDING',  '00000000-0000-0000-0000-000000000004', 'GROUP_PLAN_ASSIGNMENT', '6', '{"group_id":3,"plan_template_id":6,"apply_from":"2025-05-01","apply_to":"2025-06-01"}', 'Yêu cầu gán gói DN 1 tháng cho DEF',     NULL, NULL, NULL),
(7, 'REQUEST_GROUP_PLAN_ASSIGNMENT', 'DENIED',   '00000000-0000-0000-0000-000000000004', 'GROUP_PLAN_ASSIGNMENT', '8', '{"group_id":4,"plan_template_id":6,"apply_from":"2025-04-01","apply_to":"2025-05-01"}', 'Yêu cầu gán gói DN 1 tháng cho GHI',     '00000000-0000-0000-0000-000000000001', 'GHI chưa ký HĐ', '2025-03-11 09:00:00'),
(8, 'CREATE_PLAN_TEMPLATE',          'APPROVED', '00000000-0000-0000-0000-000000000001', 'PLAN_TEMPLATE',          '4', '{"plan_code":"GROUP-ORG-12M","customer_segment":"GROUP"}',                            'Tạo gói Doanh Nghiệp 12 tháng',          '00000000-0000-0000-0000-000000000001', 'OK', NULL),
(9, 'REQUEST_RETAIL_PLAN_SCHEDULE',  'APPROVED', '00000000-0000-0000-0000-000000000001', 'RETAIL_PLAN_SCHEDULE',   '5', '{"plan_template_id":2,"apply_from":"2025-07-01","apply_to":"2025-12-31"}',             'Yêu cầu lịch retail 6M kỳ H2/2025',      NULL, NULL, NULL),
(10, 'REQUEST_RETAIL_PLAN_SCHEDULE', 'PENDING',  '00000000-0000-0000-0000-000000000001', 'RETAIL_PLAN_SCHEDULE',   '6', '{"plan_template_id":3,"apply_from":"2025-07-01","apply_to":"2025-12-31"}',             'Yêu cầu lịch retail 1M kỳ H2/2025',      NULL, NULL, NULL);

-- ============================================================
-- 19. SETTLEMENT STATEMENTS
-- ============================================================
INSERT INTO settlement_statements (settlement_statement_id, group_id, from_date, to_date, status, total_certificates, total_signings, total_amount, currency, generated_at, generated_by) VALUES
(1, 1, '2025-01-01', '2025-01-31', 'FINALIZED', 3,  45, 5875000.00,  'VND', '2025-02-01 02:00:00', 'SYSTEM'),
(2, 1, '2025-02-01', '2025-02-28', 'FINALIZED', 3,  65, 5450000.00,  'VND', '2025-03-01 02:00:00', 'SYSTEM'),
(3, 1, '2025-03-01', '2025-03-31', 'DRAFT',     0,  65, 2200000.00,  'VND', '2025-04-01 02:00:00', 'SYSTEM'),
(4, 2, '2025-02-01', '2025-02-28', 'FINALIZED', 1,  10, 1700000.00,  'VND', '2025-03-01 02:00:00', 'SYSTEM'),
(5, 2, '2025-03-01', '2025-03-31', 'DRAFT',     2,  18, 2800000.00,  'VND', '2025-04-01 02:00:00', 'SYSTEM'),
(6, 3, '2025-03-01', '2025-03-31', 'DRAFT',     1,  35, 5500000.00,  'VND', '2025-04-01 02:00:00', 'SYSTEM'),
(7, 1, '2025-04-01', '2025-04-30', 'EXPORTED',  2,  50, 4500000.00,  'VND', '2025-05-01 02:00:00', '00000000-0000-0000-0000-000000000002');

-- ============================================================
-- 20. PAYMENT RECORDS
-- ============================================================
INSERT INTO payment_records (payment_id, subscription_id, group_plan_assignment_id, settlement_statement_id, external_reference, amount, currency, payment_status, payment_scope, payment_method, paid_at, raw_payload) VALUES
(1,  1,  NULL, NULL, 'EXT-PAY-001', 150000.00,  'VND', 'SUCCESS', 'SUBSCRIPTION',      'BANK_TRANSFER', '2025-01-14 10:00:00', NULL),
(2,  2,  NULL, NULL, 'EXT-PAY-002', 850000.00,  'VND', 'SUCCESS', 'SUBSCRIPTION',      'BANK_TRANSFER', '2025-01-30 11:00:00', NULL),
(3,  4,  1,    NULL, 'EXT-PAY-G01', 34000000.00,'VND', 'SUCCESS', 'GROUP_ASSIGNMENT',  'BANK_TRANSFER', '2025-01-14 09:00:00', NULL),
(4,  5,  2,    NULL, 'EXT-PAY-G02', 1500000.00, 'VND', 'SUCCESS', 'GROUP_ASSIGNMENT',  'BANK_TRANSFER', '2025-01-14 09:30:00', NULL),
(5,  6,  3,    NULL, 'EXT-PAY-G03', 17000000.00,'VND', 'SUCCESS', 'GROUP_ASSIGNMENT',  'BANK_TRANSFER', '2025-01-31 10:00:00', NULL),
(6,  7,  5,    NULL, 'EXT-PAY-G04', 70000000.00,'VND', 'SUCCESS', 'GROUP_ASSIGNMENT',  'BANK_TRANSFER', '2025-02-28 09:00:00', NULL),
(7,  NULL, NULL, 1,  'EXT-PAY-S01', 5875000.00, 'VND', 'SUCCESS', 'SETTLEMENT',        'BANK_TRANSFER', '2025-02-05 08:00:00', NULL),
(8,  NULL, NULL, 2,  'EXT-PAY-S02', 5450000.00, 'VND', 'SUCCESS', 'SETTLEMENT',        'BANK_TRANSFER', '2025-03-05 08:00:00', NULL),
(9,  NULL, NULL, 4,  'EXT-PAY-S03', 1700000.00, 'VND', 'SUCCESS', 'SETTLEMENT',        'BANK_TRANSFER', '2025-03-05 08:30:00', NULL),
(10, NULL, NULL, 7,  'EXT-PAY-S04', 4500000.00, 'VND', 'FAILED',  'SETTLEMENT',        'BANK_TRANSFER', '2025-05-02 08:00:00', '{"error": "Insufficient funds"}'),
(11, 3,   NULL, NULL,'EXT-PAY-003', 250000.00,  'VND', 'SUCCESS', 'SUBSCRIPTION',      'CREDIT_CARD',   '2024-06-28 14:00:00', NULL),
(12, 11,  NULL, NULL,'EXT-PAY-011', 850000.00,  'VND', 'SUCCESS', 'SUBSCRIPTION',      'BANK_TRANSFER', '2025-03-14 10:00:00', NULL);

-- ============================================================
-- 21. ASSIGNMENT AUDITS
-- ============================================================
INSERT INTO assignment_audits (assignment_audit_id, group_plan_assignment_id, retail_plan_schedule_id, assignment_type, action, old_status, new_status, actor, note) VALUES
(1,  1, NULL, 'GROUP_PLAN', 'REQUEST',  NULL,        'REQUESTED', '00000000-0000-0000-0000-000000000003', 'Tạo yêu cầu gán gói'),
(2,  1, NULL, 'GROUP_PLAN', 'APPROVE',  'REQUESTED', 'APPROVED',  '00000000-0000-0000-0000-000000000001', 'Phê duyệt'),
(3,  1, NULL, 'GROUP_PLAN', 'ACTIVATE', 'APPROVED',  'ACTIVE',    '00000000-0000-0000-0000-000000000001', 'Kích hoạt'),
(4,  2, NULL, 'GROUP_PLAN', 'REQUEST',  NULL,        'REQUESTED', '00000000-0000-0000-0000-000000000003', 'Tạo yêu cầu gán gói IO'),
(5,  2, NULL, 'GROUP_PLAN', 'APPROVE',  'REQUESTED', 'APPROVED',  '00000000-0000-0000-0000-000000000001', 'Phê duyệt'),
(6,  2, NULL, 'GROUP_PLAN', 'ACTIVATE', 'APPROVED',  'ACTIVE',    '00000000-0000-0000-0000-000000000001', 'Kích hoạt'),
(7,  3, NULL, 'GROUP_PLAN', 'REQUEST',  NULL,        'REQUESTED', '00000000-0000-0000-0000-000000000004', 'Tạo yêu cầu gán gói DN'),
(8,  3, NULL, 'GROUP_PLAN', 'APPROVE',  'REQUESTED', 'APPROVED',  '00000000-0000-0000-0000-000000000001', 'Phê duyệt'),
(9,  3, NULL, 'GROUP_PLAN', 'ACTIVATE', 'APPROVED',  'ACTIVE',    '00000000-0000-0000-0000-000000000001', 'Kích hoạt'),
(10, 7, NULL, 'GROUP_PLAN', 'REQUEST',  NULL,        'REQUESTED', '00000000-0000-0000-0000-000000000003', 'Tạo yêu cầu gói 6M'),
(11, 7, NULL, 'GROUP_PLAN', 'ACTIVATE', 'APPROVED',  'ACTIVE',    '00000000-0000-0000-0000-000000000001', 'Kích hoạt'),
(12, 7, NULL, 'GROUP_PLAN', 'STOP',     'ACTIVE',    'STOPPED',   '00000000-0000-0000-0000-000000000001', 'Chuyển sang gói 12 tháng'),
(13, NULL, 1, 'RETAIL_PLAN', 'REQUEST',  NULL,        'REQUESTED', '00000000-0000-0000-0000-000000000001', 'Tạo lịch retail'),
(14, NULL, 1, 'RETAIL_PLAN', 'APPROVE',  'REQUESTED', 'APPROVED',  '00000000-0000-0000-0000-000000000001', 'Phê duyệt lịch'),
(15, NULL, 1, 'RETAIL_PLAN', 'ACTIVATE', 'APPROVED',  'ACTIVE',    '00000000-0000-0000-0000-000000000001', 'Kích hoạt lịch'),
(16, 8, NULL, 'GROUP_PLAN', 'REQUEST',  NULL,        'REQUESTED', '00000000-0000-0000-0000-000000000004', 'Tạo yêu cầu cho GHI'),
(17, NULL, 6, 'RETAIL_PLAN', 'ACTIVATE', 'APPROVED',  'ACTIVE',    '00000000-0000-0000-0000-000000000001', 'Kích hoạt'),
(18, NULL, 6, 'RETAIL_PLAN', 'EXPIRE',   'ACTIVE',    'INACTIVE',  'SYSTEM',                                  'Hết hạn tự động');

-- ============================================================
-- 22. SUBSCRIPTION AUDIT LOGS
-- ============================================================
INSERT INTO subscription_audit_logs (subscription_id, actor, old_status, new_status, reason, source_type) VALUES
(1,  'SYSTEM',                                    NULL,        'PENDING',   'Tạo subscription từ retail purchase',    'RETAIL_PURCHASE'),
(1,  'SYSTEM',                                    'PENDING',   'ACTIVE',    'Kích hoạt subscription',                  'MANUAL'),
(2,  'SYSTEM',                                    NULL,        'PENDING',   'Tạo subscription từ retail purchase',    'RETAIL_PURCHASE'),
(2,  'SYSTEM',                                    'PENDING',   'ACTIVE',    'Kích hoạt subscription',                  'MANUAL'),
(3,  'SYSTEM',                                    NULL,        'PENDING',   'Tạo subscription từ retail purchase',    'RETAIL_PURCHASE'),
(3,  'SYSTEM',                                    'PENDING',   'ACTIVE',    'Kích hoạt subscription',                  'MANUAL'),
(3,  'SYSTEM',                                    'ACTIVE',    'EXPIRED',   'Hết hạn tự động',                         'MANUAL'),
(4,  '00000000-0000-0000-0000-000000000001',      NULL,        'PENDING',   'Tạo subscription từ group assignment',   'GROUP_ASSIGNMENT'),
(4,  '00000000-0000-0000-0000-000000000001',      'PENDING',   'ACTIVE',    'Kích hoạt subscription',                  'GROUP_ASSIGNMENT'),
(5,  '00000000-0000-0000-0000-000000000001',      NULL,        'PENDING',   'Tạo subscription từ group assignment',   'GROUP_ASSIGNMENT'),
(5,  '00000000-0000-0000-0000-000000000001',      'PENDING',   'ACTIVE',    'Kích hoạt subscription',                  'GROUP_ASSIGNMENT'),
(6,  '00000000-0000-0000-0000-000000000001',      NULL,        'PENDING',   'Tạo subscription từ group assignment',   'GROUP_ASSIGNMENT'),
(6,  '00000000-0000-0000-0000-000000000001',      'PENDING',   'ACTIVE',    'Kích hoạt subscription',                  'GROUP_ASSIGNMENT'),
(7,  '00000000-0000-0000-0000-000000000001',      NULL,        'PENDING',   'Tạo subscription từ group assignment',   'GROUP_ASSIGNMENT'),
(7,  '00000000-0000-0000-0000-000000000001',      'PENDING',   'ACTIVE',    'Kích hoạt subscription',                  'GROUP_ASSIGNMENT'),
(8,  'SYSTEM',                                    NULL,        'PENDING',   'Tạo subscription từ retail purchase',    'RETAIL_PURCHASE'),
(8,  '00000000-0000-0000-0000-000000000001',      'PENDING',   'ACTIVE',    'Kích hoạt',                               'MANUAL'),
(8,  '00000000-0000-0000-0000-000000000001',      'ACTIVE',    'SUSPENDED', 'Tạm ngưng do vi phạm',                   'MANUAL'),
(9,  'SYSTEM',                                    NULL,        'PENDING',   'Tạo subscription chờ kích hoạt',         'RETAIL_PURCHASE'),
(10, '00000000-0000-0000-0000-000000000001',      NULL,        'PENDING',   'Tạo subscription từ group assignment',   'GROUP_ASSIGNMENT'),
(10, '00000000-0000-0000-0000-000000000001',      'PENDING',   'ACTIVE',    'Kích hoạt',                               'GROUP_ASSIGNMENT'),
(10, '00000000-0000-0000-0000-000000000001',      'ACTIVE',    'CANCELLED', 'Hủy do chấm dứt hợp đồng',              'MANUAL'),
(11, 'SYSTEM',                                    NULL,        'PENDING',   'Tạo subscription từ retail purchase',    'RETAIL_PURCHASE'),
(11, 'SYSTEM',                                    'PENDING',   'ACTIVE',    'Kích hoạt subscription',                  'MANUAL');

-- ============================================================
-- 23. ADMIN AUDIT LOGS
-- ============================================================
INSERT INTO admin_audit_logs (actor, action, entity_type, entity_id, details) VALUES
('00000000-0000-0000-0000-000000000001', 'CREATE_USER',      'USER',                '00000000-0000-0000-0000-000000000002', 'Tạo user billing_admin'),
('00000000-0000-0000-0000-000000000001', 'CREATE_USER',      'USER',                '00000000-0000-0000-0000-000000000003', 'Tạo user group_admin'),
('00000000-0000-0000-0000-000000000001', 'CREATE_USER',      'USER',                '00000000-0000-0000-0000-000000000004', 'Tạo user operator1'),
('00000000-0000-0000-0000-000000000001', 'CREATE_USER',      'USER',                '00000000-0000-0000-0000-000000000005', 'Tạo user operator2'),
('00000000-0000-0000-0000-000000000001', 'CREATE_USER',      'USER',                '00000000-0000-0000-0000-000000000006', 'Tạo user viewer1'),
('00000000-0000-0000-0000-000000000001', 'ASSIGN_ROLE',      'USER',                '00000000-0000-0000-0000-000000000002', 'Gán role BILLING_ADMIN'),
('00000000-0000-0000-0000-000000000001', 'ASSIGN_ROLE',      'USER',                '00000000-0000-0000-0000-000000000003', 'Gán role GROUP_ADMIN'),
('00000000-0000-0000-0000-000000000001', 'CREATE_GROUP',     'GROUP',               '1', 'Tạo group Công ty TNHH ABC'),
('00000000-0000-0000-0000-000000000001', 'CREATE_GROUP',     'GROUP',               '2', 'Tạo group Công ty Cổ phần XYZ'),
('00000000-0000-0000-0000-000000000001', 'CREATE_GROUP',     'GROUP',               '3', 'Tạo group Tập đoàn DEF'),
('00000000-0000-0000-0000-000000000001', 'CREATE_PLAN',      'PLAN_TEMPLATE',       '1', 'Tạo gói RETAIL-INDIVIDUAL-12M'),
('00000000-0000-0000-0000-000000000001', 'CREATE_PLAN',      'PLAN_TEMPLATE',       '4', 'Tạo gói GROUP-ORG-12M'),
('00000000-0000-0000-0000-000000000001', 'UPDATE_SETTING',   'SYSTEM_SETTING',      'auth.max_failed_login_attempts', 'Cập nhật cấu hình đăng nhập'),
('00000000-0000-0000-0000-000000000001', 'UPDATE_PERMISSION','ROLE',                '1', 'Gán toàn bộ quyền cho ROLE_ADMIN'),
('00000000-0000-0000-0000-000000000001', 'APPROVE_ASSIGNMENT','GROUP_ASSIGNMENT',   '1', 'Phê duyệt gán gói cho ABC'),
('00000000-0000-0000-0000-000000000001', 'REJECT_ASSIGNMENT','GROUP_ASSIGNMENT',    '8', 'Từ chối gán gói cho GHI - chưa ký HĐ');