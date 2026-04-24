-- Permission table
CREATE TABLE permissions (
    permission_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    permission_key VARCHAR(100) NOT NULL UNIQUE,
    display_name  VARCHAR(200) NOT NULL,
    group_name    VARCHAR(100) NOT NULL,
    sort_order    INT NOT NULL DEFAULT 0
);

-- Role-Permission mapping
CREATE TABLE role_permissions (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE,
    CONSTRAINT fk_rp_perm FOREIGN KEY (permission_id) REFERENCES permissions(permission_id) ON DELETE CASCADE,
    UNIQUE KEY uq_role_perm (role_id, permission_id)
);

-- Seed permissions grouped by domain area
-- DASHBOARD
INSERT INTO permissions (permission_key, display_name, group_name, sort_order) VALUES
('dashboard:view', 'View Dashboard', 'DASHBOARD', 100);

-- PLANS
INSERT INTO permissions (permission_key, display_name, group_name, sort_order) VALUES
('plan:view',   'View Plans',   'PLAN', 200),
('plan:create', 'Create Plan',  'PLAN', 201),
('plan:update', 'Update Plan',  'PLAN', 202),
('plan:delete', 'Deactivate Plan', 'PLAN', 203);

-- SUBSCRIPTIONS
INSERT INTO permissions (permission_key, display_name, group_name, sort_order) VALUES
('subscription:view',     'View Subscriptions',     'SUBSCRIPTION', 300),
('subscription:activate', 'Activate Subscription',  'SUBSCRIPTION', 301),
('subscription:suspend',  'Suspend Subscription',   'SUBSCRIPTION', 302),
('subscription:cancel',   'Cancel Subscription',    'SUBSCRIPTION', 303);

-- PARTNERS
INSERT INTO permissions (permission_key, display_name, group_name, sort_order) VALUES
('partner:view',        'View Partners',       'PARTNER', 400),
('partner:create',      'Create Partner',      'PARTNER', 401),
('partner:update',      'Update Partner',      'PARTNER', 402),
('partner:manage-members', 'Manage Members',   'PARTNER', 403),
('partner:assign-plan', 'Assign Plan',         'PARTNER', 404);

-- CERTIFICATES
INSERT INTO permissions (permission_key, display_name, group_name, sort_order) VALUES
('certificate:view',  'View Certificates',  'CERTIFICATE', 500),
('certificate:retry', 'Retry Provisioning', 'CERTIFICATE', 501);

-- USERS
INSERT INTO permissions (permission_key, display_name, group_name, sort_order) VALUES
('user:view',           'View Users',        'USER', 600),
('user:create',         'Create User',       'USER', 601),
('user:update',         'Update User',       'USER', 602),
('user:delete',         'Delete User',       'USER', 603),
('user:assign-roles',   'Assign Roles',      'USER', 604),
('user:reset-password', 'Reset Password',    'USER', 605);

-- ROLES
INSERT INTO permissions (permission_key, display_name, group_name, sort_order) VALUES
('role:view',              'View Roles',          'ROLE', 700),
('role:create',            'Create Role',         'ROLE', 701),
('role:update',            'Update Role',         'ROLE', 702),
('role:delete',            'Delete Role',         'ROLE', 703),
('role:assign-permissions', 'Assign Permissions', 'ROLE', 704);

-- REPORTS
INSERT INTO permissions (permission_key, display_name, group_name, sort_order) VALUES
('report:view',   'View Reports',   'REPORT', 800),
('report:export', 'Export Reports', 'REPORT', 801);

-- Grant ALL permissions to ROLE_ADMIN (role_id=1)
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1, permission_id FROM permissions;

-- Grant limited permissions to ROLE_OPERATOR (role_id=2)
INSERT INTO role_permissions (role_id, permission_id)
SELECT 2, permission_id FROM permissions
WHERE permission_key IN (
    'dashboard:view',
    'subscription:view',
    'partner:view', 'partner:update', 'partner:manage-members',
    'certificate:view'
);

-- Grant basic permissions to ROLE_USER (role_id=3)
INSERT INTO role_permissions (role_id, permission_id)
SELECT 3, permission_id FROM permissions
WHERE permission_key IN (
    'dashboard:view',
    'subscription:view',
    'certificate:view'
);
