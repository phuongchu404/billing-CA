-- V20: Multi-plan individual subscriptions support
--
-- The initial schema (V1) created the subscriptions table without any unique
-- constraint on (user_id, status). There is therefore no constraint to drop here.
-- The idx_sub_user_status index on (user_id, status) is a plain index (not unique)
-- and is intentionally kept — it still improves query performance even when a user
-- may have multiple subscriptions with the same status.
--
-- Add a covering index on certificate_provisioning_records(subscription_id) to
-- speed up the per-subscription lookups added by the bind-certificate feature.

ALTER TABLE certificate_provisioning_records
    ADD INDEX idx_cert_subscription_id (subscription_id);
