-- Add effective date range and visibility flag to plans
ALTER TABLE plans
    ADD COLUMN effective_from DATE         NULL AFTER max_members,
    ADD COLUMN effective_to   DATE         NULL AFTER effective_from,
    ADD COLUMN is_visible     TINYINT(1)   NOT NULL DEFAULT 1 AFTER effective_to;
