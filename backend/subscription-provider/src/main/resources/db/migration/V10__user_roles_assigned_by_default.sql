-- Allow JPA @ManyToMany inserts to user_roles without explicitly providing assigned_by.
-- 'system' is used as the fallback when the assignment is done programmatically.
ALTER TABLE user_roles
    MODIFY COLUMN assigned_by VARCHAR(36) NOT NULL DEFAULT 'system';
