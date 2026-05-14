ALTER TABLE saved_searches
    ADD COLUMN IF NOT EXISTS excluded_keywords TEXT;

INSERT INTO saved_searches (
    user_id,
    name,
    keyword,
    location,
    category,
    job_type,
    experience_level,
    salary_min,
    salary_max,
    remote_only,
    skills,
    excluded_keywords,
    alert_frequency,
    active,
    created_at,
    updated_at
)
SELECT
    p.user_id,
    'Nhu cau viec lam',
    p.desired_title,
    p.desired_location,
    p.desired_category,
    p.desired_job_type,
    p.desired_experience_level,
    p.min_salary,
    p.max_salary,
    COALESCE(p.remote_only, FALSE),
    p.preferred_skills,
    p.excluded_keywords,
    'DAILY',
    TRUE,
    COALESCE(p.created_at, CURRENT_TIMESTAMP),
    COALESCE(p.updated_at, CURRENT_TIMESTAMP)
FROM job_need_preferences p
WHERE EXISTS (
    SELECT 1
    FROM users u
    WHERE u.id = p.user_id
)
AND NOT EXISTS (
    SELECT 1
    FROM saved_searches s
    WHERE s.user_id = p.user_id
      AND s.name = 'Nhu cau viec lam'
);

DROP TABLE IF EXISTS job_need_preferences;
DROP TABLE IF EXISTS cvs;
