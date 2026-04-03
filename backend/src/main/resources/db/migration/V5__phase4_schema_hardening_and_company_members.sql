-- Phase 4 schema hardening:
-- 1) CHECK constraints for role/status
-- 2) updated_at columns
-- 3) soft-delete via deleted_at for companies/jobs
-- 4) company_members table for multi-recruiter ownership
-- 5) salary_min/salary_max/currency for jobs

-- ---------- USERS ----------
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

UPDATE users
SET role = UPPER(role)
WHERE role IS NOT NULL;

UPDATE users
SET role = 'CANDIDATE'
WHERE role IS NULL OR role = '';

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'ck_users_role'
    ) THEN
        ALTER TABLE users
            ADD CONSTRAINT ck_users_role
            CHECK (role IN ('CANDIDATE', 'RECRUITER', 'ADMIN'));
    END IF;
END $$;

-- ---------- COMPANIES ----------
ALTER TABLE companies
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

UPDATE companies
SET updated_at = COALESCE(updated_at, created_at, NOW());

-- ---------- JOBS ----------
ALTER TABLE jobs
    ADD COLUMN IF NOT EXISTS salary_min NUMERIC,
    ADD COLUMN IF NOT EXISTS salary_max NUMERIC,
    ADD COLUMN IF NOT EXISTS currency VARCHAR(10),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

UPDATE jobs
SET status = LOWER(status)
WHERE status IS NOT NULL;

UPDATE jobs
SET status = 'open'
WHERE status IS NULL OR status = '';

UPDATE jobs
SET salary_min = COALESCE(salary_min, salary),
    salary_max = COALESCE(salary_max, salary);

UPDATE jobs
SET currency = COALESCE(NULLIF(currency, ''), 'VND');

UPDATE jobs
SET updated_at = COALESCE(updated_at, posted_date, NOW());

ALTER TABLE jobs
    ALTER COLUMN currency SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'ck_jobs_status'
    ) THEN
        ALTER TABLE jobs
            ADD CONSTRAINT ck_jobs_status
            CHECK (status IN ('draft', 'open', 'closed', 'archived'));
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'ck_jobs_salary_range'
    ) THEN
        ALTER TABLE jobs
            ADD CONSTRAINT ck_jobs_salary_range
            CHECK (salary_min IS NULL OR salary_max IS NULL OR salary_min <= salary_max);
    END IF;
END $$;

-- ---------- JOB APPLICATIONS ----------
ALTER TABLE job_applications
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

UPDATE job_applications
SET status = LOWER(status)
WHERE status IS NOT NULL;

UPDATE job_applications
SET status = 'submitted'
WHERE status IS NULL OR status = '';

UPDATE job_applications
SET status = 'submitted'
WHERE status = 'pending';

UPDATE job_applications
SET status = 'hired'
WHERE status = 'accepted';

UPDATE job_applications
SET updated_at = COALESCE(updated_at, application_date, NOW());

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'ck_job_applications_status'
    ) THEN
        ALTER TABLE job_applications
            ADD CONSTRAINT ck_job_applications_status
            CHECK (status IN ('submitted', 'reviewing', 'shortlisted', 'interviewed', 'offered', 'hired', 'rejected', 'withdrawn'));
    END IF;
END $$;

-- ---------- COMPANY MEMBERS ----------
CREATE TABLE IF NOT EXISTS company_members (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    member_role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_company_members_company_user UNIQUE (company_id, user_id)
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'ck_company_members_role'
    ) THEN
        ALTER TABLE company_members
            ADD CONSTRAINT ck_company_members_role
            CHECK (member_role IN ('RECRUITER', 'ADMIN'));
    END IF;
END $$;

-- Backfill owner as ADMIN member.
INSERT INTO company_members (company_id, user_id, member_role, created_at)
SELECT c.id, c.created_by, 'ADMIN', NOW()
FROM companies c
WHERE c.created_by IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM company_members cm
      WHERE cm.company_id = c.id AND cm.user_id = c.created_by
  );

-- ---------- INDEXES ----------
CREATE INDEX IF NOT EXISTS idx_companies_deleted_at ON companies(deleted_at);
CREATE INDEX IF NOT EXISTS idx_jobs_deleted_at ON jobs(deleted_at);
CREATE INDEX IF NOT EXISTS idx_jobs_company_status ON jobs(company_id, status);
CREATE INDEX IF NOT EXISTS idx_job_applications_user ON job_applications(user_id);
CREATE INDEX IF NOT EXISTS idx_job_applications_job ON job_applications(job_id);
CREATE INDEX IF NOT EXISTS idx_company_members_company ON company_members(company_id);
CREATE INDEX IF NOT EXISTS idx_company_members_user ON company_members(user_id);
