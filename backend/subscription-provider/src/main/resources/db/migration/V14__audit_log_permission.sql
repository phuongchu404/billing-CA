INSERT INTO permissions (permission_key, display_name, module_group, group_name, sort_order)
VALUES ('audit-log:view', 'View Audit Logs', 'SYSTEM_CONFIGURATION', 'AUDIT_LOG', 900);

-- Grant to ROLE_ADMIN only
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1, permission_id FROM permissions WHERE permission_key = 'audit-log:view';
