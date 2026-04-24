-- Fix default admin password hash (Admin@2026)
UPDATE user_accounts
SET password_hash = '$2a$12$leXpj5kff1RE5qsvVbJrfO4JXRaHk9HdJp6wS7tluz9sKgZQi3TwC'
WHERE username = 'admin';
