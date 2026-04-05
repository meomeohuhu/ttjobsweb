-- Add job category for CV-based recommendations.

ALTER TABLE jobs
    ADD COLUMN IF NOT EXISTS category VARCHAR(120);

CREATE INDEX IF NOT EXISTS idx_jobs_category ON jobs(category);
