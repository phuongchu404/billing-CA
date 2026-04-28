UPDATE permissions
SET display_name = 'Trưởng phòng kinh doanh'
WHERE permission_key = 'approval:level1';

UPDATE permissions
SET display_name = 'CFO (Finance Manager)'
WHERE permission_key = 'approval:level2';

UPDATE permissions
SET display_name = 'CEO'
WHERE permission_key = 'approval:level3';
