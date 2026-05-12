-- 1. Move partner access permissions into QUAN_LY_PHAN_QUYEN to match frontend menu structure
-- UPDATE permissions
-- SET module_group = 'QUAN_LY_PHAN_QUYEN',
--     group_name   = 'PARTNER_ACCESS'
-- WHERE permission_key IN ('partner:access:grant', 'partner:access:revoke');

-- 2. Add missing user:delete permission (frontend user.ts uses this key; only user:update existed)
-- INSERT INTO permissions (permission_key, display_name, module_group, group_name, sort_order)
-- VALUES ('user:delete', 'Xóa người dùng', 'QUAN_LY_PHAN_QUYEN', 'USER', 53);

-- 3. Add missing approval permissions (frontend nav.ts uses these; none existed in DB)
-- INSERT INTO permissions (permission_key, display_name, module_group, group_name, sort_order)
-- VALUES
--     ('approval:view',   'Xem yêu cầu phê duyệt', 'PHE_DUYET', 'APPROVAL', 92),
--     ('approval:level1', 'Trưởng phòng kinh doanh', 'PHE_DUYET', 'APPROVAL', 93),
--     ('approval:level2', 'CFO (Finance Manager)',   'PHE_DUYET', 'APPROVAL', 94),
--     ('approval:level3', 'CEO',                     'PHE_DUYET', 'APPROVAL', 95);
