-- Add type column to existing settings table
ALTER TABLE system_settings
    ADD COLUMN setting_type VARCHAR(20) NOT NULL DEFAULT 'STRING' AFTER setting_value;

-- Back-fill types for existing rows
UPDATE system_settings SET setting_type = 'BOOLEAN'
WHERE setting_key IN ('email.enabled', 'email.ssl.enabled', 'notification.expiry.enabled');

UPDATE system_settings SET setting_type = 'INTEGER'
WHERE setting_key IN ('email.port');

-- General operational settings
INSERT INTO system_settings (setting_key, setting_value, setting_type, category, description) VALUES
('auto_approval.enabled', 'false', 'BOOLEAN', 'GENERAL', 'Automatically approve all incoming requests without manual review');
