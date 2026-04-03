-- PostgreSQL migration for auth hardening on users table.
-- 1) Rename password -> password_hash
-- 2) Backfill missing role values
-- 3) Enforce NOT NULL + default for role

DO $$
BEGIN
    -- Rename only when old column exists and new column does not exist yet.
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'users' AND column_name = 'password'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'users' AND column_name = 'password_hash'
    ) THEN
        ALTER TABLE users RENAME COLUMN password TO password_hash;
    END IF;
END $$;

UPDATE users
SET role = 'CANDIDATE'
WHERE role IS NULL OR role = '';

ALTER TABLE users
    ALTER COLUMN role SET DEFAULT 'CANDIDATE';

ALTER TABLE users
    ALTER COLUMN role SET NOT NULL;
