-- Separate member cap from signing quota for group plans
ALTER TABLE plans
    ADD COLUMN max_members INT NULL AFTER max_signing_quota;
