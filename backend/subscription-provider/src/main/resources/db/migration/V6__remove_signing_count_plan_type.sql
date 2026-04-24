-- Remove SIGNING_COUNT plan type: certificates always have a validity period
ALTER TABLE plans
    MODIFY COLUMN plan_type ENUM('VALIDITY_PERIOD','COMBINED')
        NOT NULL DEFAULT 'VALIDITY_PERIOD';

-- Restore validity_days to NOT NULL (always required now)
ALTER TABLE plans
    MODIFY COLUMN validity_days INT NOT NULL;
