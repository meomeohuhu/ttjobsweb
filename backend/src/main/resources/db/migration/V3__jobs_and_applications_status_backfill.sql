-- PostgreSQL migration for Phase 3 status fields.
-- 1) Ensure jobs.status exists and has safe default values for legacy rows
-- 2) Normalize old application status values to the new lifecycle vocabulary

ALTER TABLE jobs
    ADD COLUMN IF NOT EXISTS status varchar(50);

UPDATE jobs
SET status = 'open'
WHERE status IS NULL OR status = '';

ALTER TABLE jobs
    ALTER COLUMN status SET NOT NULL;

UPDATE job_applications
SET status = LOWER(status)
WHERE status IS NOT NULL;

UPDATE job_applications
SET status = 'submitted'
WHERE status = 'pending';

UPDATE job_applications
SET status = 'hired'
WHERE status = 'accepted';

UPDATE job_applications
SET status = 'rejected'
WHERE status = 'rejected';
