ALTER TABLE job_need_preferences
    ADD COLUMN IF NOT EXISTS preferred_skills TEXT,
    ADD COLUMN IF NOT EXISTS excluded_keywords TEXT;
