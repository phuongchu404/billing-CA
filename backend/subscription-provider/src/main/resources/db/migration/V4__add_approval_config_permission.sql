-- Thêm permission approval:config cho admin cấu hình quy tắc phê duyệt
INSERT INTO permissions (permission_id, permission_key, display_name, module_group, group_name, sort_order)
VALUES (37, 'approval:config', 'Cau hinh quy tac phe duyet', 'PHE_DUYET', 'APPROVAL', 96);

-- Gán permission này cho ROLE_ADMIN (id=1)
INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 37);
