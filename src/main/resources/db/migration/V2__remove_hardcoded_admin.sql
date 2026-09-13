-- V2: Remove old hardcoded admin account
-- DataSeeder will recreate admin using ADMIN_EMAIL and ADMIN_PASSWORD env vars on next startup
DELETE FROM users WHERE email = 'admin@grievance.com' AND role = 'ADMIN';