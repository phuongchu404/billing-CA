UPDATE user_accounts
SET password_hash = '$2a$12$u2sex9JV2pkYdLErDN64juv.4ACIftHIswis0HSwZHAL2vWR5F1Si',
    failed_login_attempts = 0,
    locked_until = NULL,
    status = CASE WHEN status = 'LOCKED' THEN 'ACTIVE' ELSE status END
WHERE auth_provider = 'LOCAL'
  AND username IN (
      'admin',
      'billing_admin',
      'group_admin',
      'operator1',
      'operator2',
      'viewer1',
      'individual_user1',
      'individual_user2',
      'individual_user3',
      'group_member1',
      'group_member2',
      'group_member3'
  )
  AND password_hash = '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5qXvS4p6UwHuu';
