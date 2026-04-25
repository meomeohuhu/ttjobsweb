-- Add CV builder specific fields to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS cv_role VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS cv_objective TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS cv_experience_highlights TEXT;
