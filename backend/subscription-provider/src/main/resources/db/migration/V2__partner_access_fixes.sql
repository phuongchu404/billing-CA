-- Remove unique constraint: prevents re-granting access after revocation (soft-delete pattern)
ALTER TABLE partner_group_access DROP INDEX uk_partner_group;

-- Add revoked_by column to track who revoked the access
ALTER TABLE partner_group_access
    ADD COLUMN revoked_by VARCHAR(36) NULL COMMENT 'Username của admin thu hồi quyền' AFTER revoked_at;
