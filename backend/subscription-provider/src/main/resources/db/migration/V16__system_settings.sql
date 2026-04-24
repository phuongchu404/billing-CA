CREATE TABLE system_settings (
    setting_key   VARCHAR(100) NOT NULL,
    setting_value TEXT         NULL,
    category      VARCHAR(50)  NOT NULL,
    description   VARCHAR(300) NULL,
    updated_at    DATETIME     NULL,
    PRIMARY KEY (setting_key),
    INDEX idx_setting_category (category)
) ENGINE=InnoDB;

INSERT INTO system_settings (setting_key, setting_value, category, description) VALUES
-- Email
('email.enabled',       'false',                          'EMAIL', 'Enable outbound email sending'),
('email.host',          '',                               'EMAIL', 'SMTP server hostname'),
('email.port',          '587',                            'EMAIL', 'SMTP server port'),
('email.username',      '',                               'EMAIL', 'SMTP authentication username'),
('email.password',      '',                               'EMAIL', 'SMTP authentication password'),
('email.from.address',  'noreply@example.com',            'EMAIL', 'Sender email address'),
('email.from.name',     'Subscription Management',        'EMAIL', 'Sender display name'),
('email.ssl.enabled',   'false',                          'EMAIL', 'Use SSL/TLS (false = STARTTLS on port 587)'),
-- Notification
('notification.expiry.enabled',     'false',              'NOTIFICATION', 'Send subscription expiry reminder emails'),
('notification.expiry.days_before', '7,3,1',              'NOTIFICATION', 'Days before expiry to send reminder (comma-separated)'),
('notification.expiry.subject',     'Your subscription expires in {days} days', 'NOTIFICATION', 'Reminder email subject'),
('notification.expiry.body',
 'Dear {userName},\n\nYour subscription for plan "{planName}" will expire on {expiryDate} ({days} days from now).\n\nPlease renew your subscription to continue using our services.\n\nRegards,\nSubscription Management Team',
 'NOTIFICATION', 'Reminder email body — placeholders: {userName} {planName} {expiryDate} {days}');

-- Permissions
INSERT INTO permissions (permission_key, display_name, module_group, group_name, sort_order) VALUES
    ('setting:view', 'View System Settings', 'SYSTEM_CONFIGURATION', 'SETTINGS', 950),
    ('setting:edit', 'Edit System Settings', 'SYSTEM_CONFIGURATION', 'SETTINGS', 960);

INSERT INTO role_permissions (role_id, permission_id)
SELECT 1, permission_id FROM permissions WHERE permission_key IN ('setting:view', 'setting:edit');
