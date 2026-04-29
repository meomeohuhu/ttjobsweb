-- Track whether the user's active CV is from the builder or an uploaded file.
ALTER TABLE users ADD COLUMN IF NOT EXISTS primary_cv_type VARCHAR(50);
