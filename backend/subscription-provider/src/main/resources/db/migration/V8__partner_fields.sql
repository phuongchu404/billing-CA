-- Add Partner credential and contact fields (safe for tables with existing rows)

-- Step 1: add as nullable so existing rows are not rejected
ALTER TABLE `groups`
    ADD COLUMN username      VARCHAR(100) NULL AFTER group_name,
    ADD COLUMN password      VARCHAR(255) NULL AFTER username,
    ADD COLUMN contact_email VARCHAR(200) NULL AFTER password,
    ADD COLUMN contact_phone VARCHAR(20)  NULL AFTER contact_email;

-- Step 2: fill placeholder values for any pre-existing rows
UPDATE `groups` SET username      = CONCAT('partner_', group_id)                            WHERE username IS NULL;
UPDATE `groups` SET password      = '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi' WHERE password IS NULL;
UPDATE `groups` SET contact_email = CONCAT(LOWER(group_code), '@placeholder.local')         WHERE contact_email IS NULL;

-- Step 3: apply NOT NULL + unique constraints now that all rows have values
ALTER TABLE `groups`
    MODIFY COLUMN username      VARCHAR(100) NOT NULL,
    MODIFY COLUMN password      VARCHAR(255) NOT NULL,
    MODIFY COLUMN contact_email VARCHAR(200) NOT NULL,
    ADD UNIQUE KEY uq_groups_username (username);
