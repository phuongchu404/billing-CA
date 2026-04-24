-- Expand contact_email and contact_phone to hold multiple space-separated values
-- Add ref_contract_no and pic_emails (Person In Charge emails)
ALTER TABLE `groups`
    MODIFY COLUMN contact_email TEXT NOT NULL,
    MODIFY COLUMN contact_phone TEXT,
    ADD COLUMN ref_contract_no  VARCHAR(200) NULL,
    ADD COLUMN pic_emails       TEXT         NULL;
