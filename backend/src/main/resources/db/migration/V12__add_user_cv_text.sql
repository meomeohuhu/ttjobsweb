-- Store extracted CV text for recommendations.

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS cv_text TEXT;
