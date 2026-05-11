-- Ensure partner:access:grant and partner:access:revoke exist in the permissions table.
-- These were included in V1 but may be missing if the DB was initialized from an older snapshot.
INSERT INTO permissions (permission_key, display_name, module_group, group_name, sort_order)
VALUES
    ('partner:access:grant',  'Cấp quyền đối tác xem group', 'HE_THONG', 'PARTNER', 90),
    ('partner:access:revoke', 'Thu hồi quyền đối tác',        'HE_THONG', 'PARTNER', 91)
ON DUPLICATE KEY UPDATE
    display_name = VALUES(display_name),
    module_group = VALUES(module_group),
    group_name   = VALUES(group_name),
    sort_order   = VALUES(sort_order);

-- Assign both permissions to ROLE_ADMIN (role_id = 1).
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT 1, permission_id
FROM permissions
WHERE permission_key IN ('partner:access:grant', 'partner:access:revoke');
