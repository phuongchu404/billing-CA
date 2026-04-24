ALTER TABLE certificate_provisioning_records
    ADD COLUMN usage_count INT NOT NULL DEFAULT 0;

ALTER TABLE certificate_provisioning_records
    ADD UNIQUE INDEX uq_certificate_id (certificate_id);
