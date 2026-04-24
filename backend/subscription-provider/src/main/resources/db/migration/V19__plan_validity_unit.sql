-- Store the original validity period unit chosen by the user (for display purposes).
-- validityDays remains the authoritative value used for all business logic.
ALTER TABLE plans
    ADD COLUMN validity_amount INT          NULL,
    ADD COLUMN validity_unit   VARCHAR(10)  NULL;  -- 'DAYS' | 'MONTHS' | 'YEARS'
