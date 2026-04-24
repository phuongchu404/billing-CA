-- Add group member validity mode to plans
ALTER TABLE plans
    ADD COLUMN group_member_validity_mode ENUM('GROUP_FOLLOWS','INDIVIDUAL_START') NOT NULL DEFAULT 'GROUP_FOLLOWS'
    AFTER is_group_plan;

-- Add per-member validity dates to group_members (used when mode = INDIVIDUAL_START)
ALTER TABLE group_members
    ADD COLUMN member_start_date DATE NULL AFTER added_by,
    ADD COLUMN member_end_date   DATE NULL AFTER member_start_date;
