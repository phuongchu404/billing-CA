ALTER TABLE certificate_provisioning_records
    ADD COLUMN cert_type VARCHAR(30) NOT NULL DEFAULT 'INDIVIDUAL';
