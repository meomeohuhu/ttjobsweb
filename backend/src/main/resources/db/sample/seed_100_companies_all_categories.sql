-- TTJobs sample dataset.
-- Purpose: populate a realistic public/recruiter dataset without forcing Flyway to run it in production.
--
-- What it creates:
-- - At least 100 companies distributed across all current categories found in jobs.category,
--   plus the built-in TTJobs categories.
-- - 3 open jobs per sample company.
-- - Company logos and job images through deterministic placeholder image URLs.
-- - Skills, job_skills, company_members, company_verifications, and job_requirement_signals when possible.
--
-- Idempotent: rerunning this file will not duplicate companies/jobs created by this seed.
--
-- Run from host:
--   psql "$DATABASE_URL" -f backend/src/main/resources/db/sample/seed_100_companies_all_categories.sql
--
-- Run inside Docker:
--   docker cp backend/src/main/resources/db/sample/seed_100_companies_all_categories.sql ttjobs-postgres:/tmp/seed.sql
--   docker exec -e PGPASSWORD="$POSTGRES_PASSWORD" ttjobs-postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -f /tmp/seed.sql

BEGIN;

CREATE TEMP TABLE ttjobs_sample_categories ON COMMIT DROP AS
WITH base_categories(sort_order, category, label, display_industry, title_prefix, skill_csv, color_bg, color_fg) AS (
    VALUES
        (1,  'INFORMATION-TECHNOLOGY', 'Information Technology', 'IT - Phần mềm', 'Software Engineer', 'Java,Spring Boot,React,Node.js,SQL,Docker,AWS,Python', 'E0E7FF', '2563EB'),
        (2,  'SALES', 'Sales', 'Sales', 'Sales Executive', 'Sales,CRM,Negotiation,Customer Success,Market Research', 'DBEAFE', '1D4ED8'),
        (3,  'MARKETING', 'Marketing', 'Marketing', 'Digital Marketing Specialist', 'SEO,Content Marketing,Google Ads,Social Media,Brand Strategy', 'FCE7F3', 'BE185D'),
        (4,  'HR', 'Human Resources', 'Dịch vụ', 'HR Operations Specialist', 'Recruitment,HR Operations,Payroll,Communication,Onboarding', 'DCFCE7', '15803D'),
        (5,  'FINANCE', 'Finance', 'Tài chính', 'Financial Analyst', 'Financial Analysis,Risk Management,Excel,SQL,Forecasting', 'FEF3C7', 'B45309'),
        (6,  'CUSTOMER-SERVICE', 'Customer Service', 'Dịch vụ', 'Customer Support Specialist', 'Customer Support,Communication,CRM,Customer Success,Service Quality', 'E0F2FE', '0369A1'),
        (7,  'REAL-ESTATE', 'Real Estate', 'Sản phẩm', 'Property Consultant', 'Real Estate Sales,Property Management,Negotiation,CRM,Market Research', 'F3E8FF', '7E22CE'),
        (8,  'ACCOUNTING', 'Accounting', 'Tài chính', 'Accountant', 'Accounting,Tax,Audit,Excel,Compliance', 'ECFCCB', '4D7C0F'),
        (9,  'DESIGN', 'Design', 'Sản phẩm', 'Product Designer', 'Figma,UI Design,UX Research,Design System,Communication', 'FFE4E6', 'BE123C'),
        (10, 'BUSINESS-DEVELOPMENT', 'Business Development', 'Sales', 'Business Development Executive', 'Partnerships,Market Research,Business Strategy,Negotiation,CRM', 'CCFBF1', '0F766E'),
        (11, 'ENGINEERING', 'Engineering', 'Sản xuất', 'Solutions Engineer', 'Python,AWS,Docker,SQL,System Design,Automation', 'EDE9FE', '6D28D9'),
        (12, 'OPERATIONS', 'Operations', 'Sản xuất', 'Operations Coordinator', 'Excel,Communication,Process Improvement,Customer Success,Business Strategy', 'FFEDD5', 'C2410C')
),
db_categories AS (
    SELECT DISTINCT LEFT(UPPER(TRIM(category)), 90) AS category
    FROM jobs
    WHERE category IS NOT NULL
      AND TRIM(category) <> ''
      AND deleted_at IS NULL
),
merged AS (
    SELECT *
    FROM base_categories

    UNION ALL

    SELECT
        1000 + ROW_NUMBER() OVER (ORDER BY dc.category) AS sort_order,
        dc.category,
        LEFT(INITCAP(REPLACE(dc.category, '-', ' ')), 120) AS label,
        CASE
            WHEN dc.category LIKE '%IT%' OR dc.category LIKE '%TECH%' OR dc.category LIKE '%SOFTWARE%' THEN 'IT - Phần mềm'
            WHEN dc.category LIKE '%MARKETING%' THEN 'Marketing'
            WHEN dc.category LIKE '%SALE%' OR dc.category LIKE '%BUSINESS%' THEN 'Sales'
            WHEN dc.category LIKE '%FINANCE%' OR dc.category LIKE '%ACCOUNT%' THEN 'Tài chính'
            WHEN dc.category LIKE '%ENGINEER%' OR dc.category LIKE '%OPERATION%' THEN 'Sản xuất'
            ELSE 'Dịch vụ'
        END AS display_industry,
        LEFT(INITCAP(REPLACE(dc.category, '-', ' ')) || ' Specialist', 120) AS title_prefix,
        'Communication,Excel,Market Research,Business Strategy,Customer Success' AS skill_csv,
        'E2E8F0' AS color_bg,
        '334155' AS color_fg
    FROM db_categories dc
    WHERE NOT EXISTS (
        SELECT 1
        FROM base_categories bc
        WHERE bc.category = dc.category
    )
)
SELECT
    ROW_NUMBER() OVER (ORDER BY sort_order, category) AS idx,
    category,
    label,
    display_industry,
    title_prefix,
    skill_csv,
    color_bg,
    color_fg
FROM merged;

CREATE TEMP TABLE ttjobs_sample_companies ON COMMIT DROP AS
WITH category_count AS (
    SELECT COUNT(*)::int AS total FROM ttjobs_sample_categories
),
company_words AS (
    SELECT
        ARRAY[
            'Nexora','Saigon','Hanoi','Lotus','Mekong','Dragon','BluePeak','Astra','Nova','Bright',
            'Prime','Future','Viet','Cloud','Signal','Talent','Urban','Metro','HomeGrid','FinEdge',
            'Trust','Ledger','Clear','Pixel','Motion','Growth','Scale','Deal','Core','Zenith'
        ]::text[] AS prefixes,
        ARRAY[
            'Labs','Group','Solutions','Partners','Studio','Analytics','Digital','Ventures','Network','Works',
            'Platform','House','Systems','Collective','Hub','Global','Vietnam','Asia','Consulting','Services'
        ]::text[] AS suffixes,
        ARRAY[
            'Ha Noi','Ho Chi Minh City','Da Nang','Binh Duong','Can Tho','Hai Phong',
            'Remote','Hybrid','Nha Trang','Hue'
        ]::text[] AS locations
)
SELECT
    gs.n AS idx,
    cat.category,
    LEFT(cat.display_industry, 240) AS industry,
    cat.color_bg,
    cat.color_fg,
    LEFT(format(
        'TTJobs Sample %s %s %s',
        cw.prefixes[((gs.n - 1) % array_length(cw.prefixes, 1)) + 1],
        cw.suffixes[((gs.n - 1) % array_length(cw.suffixes, 1)) + 1],
        lpad(gs.n::text, 3, '0')
    ), 240) AS name,
    cw.locations[((gs.n - 1) % array_length(cw.locations, 1)) + 1] AS location,
    format('https://sample-%s.ttjobs.local', lpad(gs.n::text, 3, '0')) AS website,
    format(
        'https://placehold.co/160x160/%s/%s.png?text=%s',
        cat.color_bg,
        cat.color_fg,
        replace(left(regexp_replace(cat.label, '[^A-Za-z0-9]+', '', 'g'), 4), ' ', '+')
    ) AS logo_url
FROM generate_series(
        1,
        GREATEST(100, (SELECT total FROM category_count))
    ) AS gs(n)
CROSS JOIN company_words cw
JOIN ttjobs_sample_categories cat
  ON cat.idx = (((gs.n - 1) % (SELECT total FROM category_count)) + 1);

INSERT INTO companies (
    name,
    description,
    location,
    website,
    industry,
    logo_url,
    verification_status,
    created_by,
    created_at,
    updated_at,
    deleted_at
)
SELECT
    sc.name,
    'TTJOBS_SAMPLE_2026 company in ' || LEFT(sc.industry, 120) || '. Built for UI, search, recruiter reports, and matching demos.',
    LEFT(sc.location, 240),
    LEFT(sc.website, 240),
    LEFT(sc.industry, 240),
    sc.logo_url,
    'VERIFIED',
    recruiter.id,
    NOW() - ((sc.idx % 120) * INTERVAL '1 day'),
    NOW(),
    NULL
FROM ttjobs_sample_companies sc
LEFT JOIN LATERAL (
    SELECT u.id
    FROM users u
    WHERE UPPER(u.role) IN ('RECRUITER', 'ADMIN')
    ORDER BY CASE WHEN UPPER(u.role) = 'RECRUITER' THEN 0 ELSE 1 END, u.id
    LIMIT 1
) recruiter ON TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM companies c
    WHERE LOWER(c.name) = LOWER(sc.name)
);

UPDATE companies c
SET
    industry = LEFT(sc.industry, 240),
    logo_url = LEFT(sc.logo_url, 240),
    verification_status = 'VERIFIED',
    updated_at = NOW(),
    deleted_at = NULL
FROM ttjobs_sample_companies sc
WHERE LOWER(c.name) = LOWER(sc.name)
  AND c.description LIKE 'TTJOBS_SAMPLE_2026%';

INSERT INTO company_members (company_id, user_id, member_role, created_at)
SELECT c.id, recruiter.id, 'ADMIN', NOW()
FROM ttjobs_sample_companies sc
JOIN companies c ON LOWER(c.name) = LOWER(sc.name)
JOIN LATERAL (
    SELECT u.id
    FROM users u
    WHERE UPPER(u.role) IN ('RECRUITER', 'ADMIN')
    ORDER BY CASE WHEN UPPER(u.role) = 'RECRUITER' THEN 0 ELSE 1 END, u.id
    LIMIT 1
) recruiter ON TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM company_members cm
    WHERE cm.company_id = c.id
      AND cm.user_id = recruiter.id
);

INSERT INTO company_verifications (
    company_id,
    business_license_url,
    tax_code,
    website,
    note,
    status,
    reviewed_by,
    reviewed_at,
    created_at,
    updated_at
)
SELECT
    c.id,
    'https://placehold.co/900x1200/F8FAFC/0F172A.png?text=Business+License',
    'TTJOBS-' || lpad(sc.idx::text, 6, '0'),
    c.website,
    'Auto verified sample company.',
    'VERIFIED',
    reviewer.id,
    NOW(),
    NOW(),
    NOW()
FROM ttjobs_sample_companies sc
JOIN companies c ON LOWER(c.name) = LOWER(sc.name)
LEFT JOIN LATERAL (
    SELECT u.id
    FROM users u
    WHERE UPPER(u.role) = 'ADMIN'
    ORDER BY u.id
    LIMIT 1
) reviewer ON TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM company_verifications cv
    WHERE cv.company_id = c.id
);

INSERT INTO skills (name)
SELECT DISTINCT TRIM(skill_name)
FROM ttjobs_sample_categories cat
CROSS JOIN LATERAL regexp_split_to_table(cat.skill_csv, ',') AS skill_name
WHERE TRIM(skill_name) <> ''
  AND NOT EXISTS (
      SELECT 1
      FROM skills s
      WHERE LOWER(s.name) = LOWER(TRIM(skill_name))
  );

CREATE TEMP TABLE ttjobs_sample_jobs ON COMMIT DROP AS
WITH level_seed(idx, level, salary_bonus) AS (
    VALUES
        (1, 'ENTRY', 0),
        (2, 'Junior', 3000000),
        (3, 'Mid', 8000000),
        (4, 'Senior', 15000000),
        (5, 'Lead', 22000000)
),
type_seed(idx, job_type) AS (
    VALUES
        (1, 'Full-time'),
        (2, 'Hybrid'),
        (3, 'Remote'),
        (4, 'Contract'),
        (5, 'Part-time'),
        (6, 'Internship')
)
SELECT
    sc.idx AS company_idx,
    slot.n AS slot,
    sc.name AS company_name,
    cat.category,
    cat.label,
    cat.title_prefix,
    cat.skill_csv,
    cat.color_bg,
    cat.color_fg,
    lvl.level AS experience_level,
    typ.job_type,
    LEFT(CASE
        WHEN typ.job_type = 'Remote' THEN 'Remote'
        WHEN typ.job_type = 'Hybrid' THEN sc.location || ' / Hybrid'
        ELSE sc.location
    END, 240) AS location,
    (9000000 + lvl.salary_bonus + ((sc.idx + slot.n) % 8) * 1500000)::numeric AS salary_min,
    (15000000 + lvl.salary_bonus + ((sc.idx + slot.n) % 8) * 1800000)::numeric AS salary_max
FROM ttjobs_sample_companies sc
JOIN ttjobs_sample_categories cat ON cat.category = sc.category
CROSS JOIN generate_series(1, 3) slot(n)
JOIN level_seed lvl ON lvl.idx = (((sc.idx + slot.n - 2) % 5) + 1)
JOIN type_seed typ ON typ.idx = (((sc.idx + slot.n - 2) % 6) + 1);

INSERT INTO jobs (
    title,
    description,
    location,
    salary,
    salary_min,
    salary_max,
    currency,
    job_type,
    experience_level,
    category,
    image_url,
    status,
    posted_date,
    application_deadline,
    updated_at,
    deleted_at,
    company_id
)
SELECT
    LEFT(sj.title_prefix || ' ' || sj.experience_level || ' - ' || lpad(sj.company_idx::text, 3, '0') || '-' || sj.slot, 240),
    LEFT(
        'TTJOBS_SAMPLE_2026 job. Industry: ' || sj.label ||
        '. Responsibilities: deliver outcomes, collaborate across teams, and report progress clearly. Skills: ' ||
        sj.skill_csv || '.',
        240
    ),
    LEFT(sj.location, 240),
    sj.salary_max,
    sj.salary_min,
    sj.salary_max,
    'VND',
    LEFT(sj.job_type, 90),
    LEFT(sj.experience_level, 90),
    LEFT(sj.category, 90),
    LEFT(format(
        'https://placehold.co/900x506/%s/%s.png?text=%s',
        sj.color_bg,
        sj.color_fg,
        replace(sj.category, '-', '+')
    ), 240),
    'open',
    NOW() - (((sj.company_idx + sj.slot) % 45) * INTERVAL '1 day'),
    NOW() + ((30 + ((sj.company_idx + sj.slot) % 60)) * INTERVAL '1 day'),
    NOW(),
    NULL,
    c.id
FROM ttjobs_sample_jobs sj
JOIN companies c ON LOWER(c.name) = LOWER(sj.company_name)
WHERE NOT EXISTS (
    SELECT 1
    FROM jobs j
    WHERE j.company_id = c.id
      AND j.title = LEFT(sj.title_prefix || ' ' || sj.experience_level || ' - ' || lpad(sj.company_idx::text, 3, '0') || '-' || sj.slot, 240)
);

WITH sample_job_skills AS (
    SELECT
        j.id AS job_id,
        TRIM(skill_name) AS skill_name
    FROM ttjobs_sample_jobs sj
    JOIN companies c ON LOWER(c.name) = LOWER(sj.company_name)
    JOIN jobs j ON j.company_id = c.id
        AND j.title = LEFT(sj.title_prefix || ' ' || sj.experience_level || ' - ' || lpad(sj.company_idx::text, 3, '0') || '-' || sj.slot, 240)
    CROSS JOIN LATERAL regexp_split_to_table(sj.skill_csv, ',') AS skill_name
)
INSERT INTO job_skills (job_id, skill_id)
SELECT sjs.job_id, s.id
FROM sample_job_skills sjs
JOIN skills s ON LOWER(s.name) = LOWER(sjs.skill_name)
ON CONFLICT DO NOTHING;

INSERT INTO job_requirement_signals (
    job_id,
    normalized_title,
    seniority,
    skills,
    industries,
    locations,
    salary_min,
    salary_max,
    currency,
    languages,
    evidence,
    raw_text,
    source,
    created_at,
    updated_at
)
SELECT
    j.id,
    LEFT(sj.title_prefix, 240),
    LEFT(sj.experience_level, 40),
    sj.skill_csv,
    sj.label,
    LEFT(sj.location, 240),
    sj.salary_min,
    sj.salary_max,
    'VND',
    'Vietnamese,English',
    'Generated from TTJobs sample seed.',
    j.description,
    'sample_seed',
    NOW(),
    NOW()
FROM ttjobs_sample_jobs sj
JOIN companies c ON LOWER(c.name) = LOWER(sj.company_name)
JOIN jobs j ON j.company_id = c.id
    AND j.title = LEFT(sj.title_prefix || ' ' || sj.experience_level || ' - ' || lpad(sj.company_idx::text, 3, '0') || '-' || sj.slot, 240)
ON CONFLICT (job_id) DO UPDATE SET
    normalized_title = EXCLUDED.normalized_title,
    seniority = EXCLUDED.seniority,
    skills = EXCLUDED.skills,
    industries = EXCLUDED.industries,
    locations = EXCLUDED.locations,
    salary_min = EXCLUDED.salary_min,
    salary_max = EXCLUDED.salary_max,
    currency = EXCLUDED.currency,
    languages = EXCLUDED.languages,
    evidence = EXCLUDED.evidence,
    raw_text = EXCLUDED.raw_text,
    source = EXCLUDED.source,
    updated_at = NOW();

WITH candidate_users AS (
    SELECT
        u.id,
        ROW_NUMBER() OVER (ORDER BY u.id) AS rn
    FROM users u
    WHERE UPPER(u.role) = 'CANDIDATE'
),
candidate_count AS (
    SELECT COUNT(*)::int AS total FROM candidate_users
),
ranked_sample_jobs AS (
    SELECT
        j.id AS job_id,
        ROW_NUMBER() OVER (
            ORDER BY COALESCE(j.salary_max, j.salary, j.salary_min, 0) DESC, j.posted_date DESC, j.id
        ) AS rn
    FROM jobs j
    JOIN companies c ON c.id = j.company_id
    WHERE j.description LIKE 'TTJOBS_SAMPLE_2026%'
      AND j.deleted_at IS NULL
      AND LOWER(j.status) = 'open'
),
sample_saves AS (
    SELECT
        cu.id AS user_id,
        rsj.job_id,
        NOW() - (((rsj.rn + cu.rn) % 30) * INTERVAL '1 day') AS saved_at,
        CASE
            WHEN rsj.rn % 5 = 0 THEN 'Top salary'
            WHEN rsj.rn % 3 = 0 THEN 'Shortlist'
            ELSE 'Sample interest'
        END AS tag
    FROM ranked_sample_jobs rsj
    CROSS JOIN candidate_count cc
    JOIN candidate_users cu
      ON cu.rn <= LEAST(GREATEST(cc.total, 1), 1 + (rsj.rn % 5))
)
INSERT INTO saved_jobs (user_id, job_id, saved_at, note, tag)
SELECT
    ss.user_id,
    ss.job_id,
    ss.saved_at,
    'TTJOBS_SAMPLE_2026 saved job signal.',
    ss.tag
FROM sample_saves ss
WHERE NOT EXISTS (
    SELECT 1
    FROM saved_jobs existing
    WHERE existing.user_id = ss.user_id
      AND existing.job_id = ss.job_id
);

COMMIT;

SELECT
    (SELECT COUNT(*) FROM companies WHERE description LIKE 'TTJOBS_SAMPLE_2026%') AS sample_companies,
    (SELECT COUNT(*) FROM jobs WHERE description LIKE 'TTJOBS_SAMPLE_2026%') AS sample_jobs,
    (SELECT COUNT(DISTINCT category) FROM jobs WHERE description LIKE 'TTJOBS_SAMPLE_2026%') AS covered_categories,
    (SELECT COUNT(*) FROM saved_jobs sj JOIN jobs j ON j.id = sj.job_id WHERE j.description LIKE 'TTJOBS_SAMPLE_2026%') AS sample_saved_jobs;
