ALTER TABLE job_applications
    ADD COLUMN IF NOT EXISTS cv_text_snapshot TEXT;
