-- Add module_group column (nullable first, then set values, then enforce NOT NULL)
ALTER TABLE permissions ADD COLUMN module_group VARCHAR(100) NULL AFTER permission_id;

-- DASHBOARD module
UPDATE permissions SET module_group = 'DASHBOARD'
WHERE permission_key LIKE 'dashboard:%';

-- SUBSCRIPTION_MANAGEMENT module
UPDATE permissions SET module_group = 'SUBSCRIPTION_MANAGEMENT'
WHERE permission_key LIKE 'plan:%'
   OR permission_key LIKE 'subscription:%'
   OR permission_key LIKE 'partner:%'
   OR permission_key LIKE 'certificate:%';

-- SYSTEM_CONFIGURATION module
UPDATE permissions SET module_group = 'SYSTEM_CONFIGURATION'
WHERE permission_key LIKE 'user:%'
   OR permission_key LIKE 'role:%';

-- ANALYTICS module
UPDATE permissions SET module_group = 'ANALYTICS'
WHERE permission_key LIKE 'report:%';

-- Now enforce NOT NULL
ALTER TABLE permissions MODIFY COLUMN module_group VARCHAR(100) NOT NULL;
