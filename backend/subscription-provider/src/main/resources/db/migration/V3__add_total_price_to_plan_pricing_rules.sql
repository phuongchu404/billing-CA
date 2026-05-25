-- ============================================================
-- V3: Add total_price column to plan_pricing_rules
--     Stores the total package price (unit_price * range span)
--     Null when rangeMax is unlimited — user must fill manually
-- ============================================================

ALTER TABLE plan_pricing_rules
    ADD COLUMN total_price DECIMAL(15,2) NULL
        COMMENT 'Total price for this pricing tier. Auto-calculated as unit_price*(rangeMax-rangeMin) when rangeMax is set; NULL when rangeMax is unlimited (user-entered).'
        AFTER unit_price;
