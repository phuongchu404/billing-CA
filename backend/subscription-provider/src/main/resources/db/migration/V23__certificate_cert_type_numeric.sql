UPDATE certificate_provisioning_records
    SET cert_type = CASE cert_type
        WHEN 'INDIVIDUAL_OF_ORGANIZATION' THEN '2'
        WHEN 'ORGANIZATION' THEN '3'
        ELSE '1'
    END;

ALTER TABLE certificate_provisioning_records
    MODIFY COLUMN cert_type TINYINT NOT NULL DEFAULT 1;
