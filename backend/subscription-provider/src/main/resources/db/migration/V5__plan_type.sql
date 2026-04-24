-- Add plan_type to distinguish expiry behaviour
ALTER TABLE plans
    ADD COLUMN plan_type ENUM('VALIDITY_PERIOD','SIGNING_COUNT','COMBINED')
        NOT NULL DEFAULT 'VALIDITY_PERIOD'
        AFTER plan_code;

-- Make validity_days nullable (SIGNING_COUNT plans have no validity period)
ALTER TABLE plans
    MODIFY COLUMN validity_days INT NULL;
