-- Demo dataset for public job search and matching.
-- Creates 30 companies and 300 open jobs across existing categories/job types.

WITH company_seed(idx, name, industry, location, website, logo_url) AS (
    VALUES
        (1, 'Astra Tech Labs', 'Information Technology', 'Ha Noi', 'https://astra-tech.example.com', 'https://placehold.co/96x96/E0E7FF/2563EB?text=AT'),
        (2, 'BluePeak Commerce', 'Sales', 'Ho Chi Minh City', 'https://bluepeak.example.com', 'https://placehold.co/96x96/DBEAFE/1D4ED8?text=BP'),
        (3, 'Nova Marketing House', 'Marketing', 'Da Nang', 'https://nova-marketing.example.com', 'https://placehold.co/96x96/FCE7F3/BE185D?text=NM'),
        (4, 'PeopleFirst Group', 'Human Resources', 'Ha Noi', 'https://peoplefirst.example.com', 'https://placehold.co/96x96/DCFCE7/15803D?text=PF'),
        (5, 'FinEdge Capital', 'Finance', 'Ho Chi Minh City', 'https://finedge.example.com', 'https://placehold.co/96x96/FEF3C7/B45309?text=FE'),
        (6, 'CareLine Services', 'Customer Service', 'Remote', 'https://careline.example.com', 'https://placehold.co/96x96/E0F2FE/0369A1?text=CL'),
        (7, 'UrbanNest Realty', 'Real Estate', 'Ha Noi', 'https://urbannest.example.com', 'https://placehold.co/96x96/F3E8FF/7E22CE?text=UN'),
        (8, 'LedgerPro Advisors', 'Accounting', 'Ho Chi Minh City', 'https://ledgerpro.example.com', 'https://placehold.co/96x96/ECFCCB/4D7C0F?text=LP'),
        (9, 'PixelCraft Studio', 'Design', 'Da Nang', 'https://pixelcraft.example.com', 'https://placehold.co/96x96/FFE4E6/BE123C?text=PC'),
        (10, 'GrowthForge Partners', 'Business Development', 'Hybrid', 'https://growthforge.example.com', 'https://placehold.co/96x96/CCFBF1/0F766E?text=GF'),
        (11, 'CloudNexus Software', 'Information Technology', 'Ho Chi Minh City', 'https://cloudnexus.example.com', 'https://placehold.co/96x96/E0E7FF/4338CA?text=CN'),
        (12, 'MarketLane Retail', 'Sales', 'Da Nang', 'https://marketlane.example.com', 'https://placehold.co/96x96/DBEAFE/2563EB?text=ML'),
        (13, 'SignalWave Media', 'Marketing', 'Ha Noi', 'https://signalwave.example.com', 'https://placehold.co/96x96/FCE7F3/DB2777?text=SW'),
        (14, 'TalentBridge Vietnam', 'Human Resources', 'Remote', 'https://talentbridge.example.com', 'https://placehold.co/96x96/DCFCE7/16A34A?text=TB'),
        (15, 'TrustBank Digital', 'Finance', 'Ha Noi', 'https://trustbank.example.com', 'https://placehold.co/96x96/FEF3C7/D97706?text=TD'),
        (16, 'AnswerHub Support', 'Customer Service', 'Ho Chi Minh City', 'https://answerhub.example.com', 'https://placehold.co/96x96/E0F2FE/0284C7?text=AH'),
        (17, 'MetroSpace Property', 'Real Estate', 'Da Nang', 'https://metrospace.example.com', 'https://placehold.co/96x96/F3E8FF/9333EA?text=MS'),
        (18, 'ClearBooks Consulting', 'Accounting', 'Ha Noi', 'https://clearbooks.example.com', 'https://placehold.co/96x96/ECFCCB/65A30D?text=CB'),
        (19, 'BrightUX Collective', 'Design', 'Remote', 'https://brightux.example.com', 'https://placehold.co/96x96/FFE4E6/E11D48?text=BU'),
        (20, 'ScalePoint Ventures', 'Business Development', 'Ho Chi Minh City', 'https://scalepoint.example.com', 'https://placehold.co/96x96/CCFBF1/14B8A6?text=SP'),
        (21, 'CodeHarbor Asia', 'Information Technology', 'Da Nang', 'https://codeharbor.example.com', 'https://placehold.co/96x96/E0E7FF/4F46E5?text=CH'),
        (22, 'PrimeSales Network', 'Sales', 'Ha Noi', 'https://primesales.example.com', 'https://placehold.co/96x96/DBEAFE/1E40AF?text=PS'),
        (23, 'BrandPilot Agency', 'Marketing', 'Ho Chi Minh City', 'https://brandpilot.example.com', 'https://placehold.co/96x96/FCE7F3/BE185D?text=BA'),
        (24, 'WorkWell People', 'Human Resources', 'Da Nang', 'https://workwell.example.com', 'https://placehold.co/96x96/DCFCE7/15803D?text=WW'),
        (25, 'MoneyMap Analytics', 'Finance', 'Remote', 'https://moneymap.example.com', 'https://placehold.co/96x96/FEF3C7/B45309?text=MM'),
        (26, 'HappyDesk CX', 'Customer Service', 'Ha Noi', 'https://happydesk.example.com', 'https://placehold.co/96x96/E0F2FE/0369A1?text=HD'),
        (27, 'HomeGrid Realty', 'Real Estate', 'Ho Chi Minh City', 'https://homegrid.example.com', 'https://placehold.co/96x96/F3E8FF/7E22CE?text=HG'),
        (28, 'TaxWise Partners', 'Accounting', 'Remote', 'https://taxwise.example.com', 'https://placehold.co/96x96/ECFCCB/4D7C0F?text=TW'),
        (29, 'MotionBox Design', 'Design', 'Ha Noi', 'https://motionbox.example.com', 'https://placehold.co/96x96/FFE4E6/BE123C?text=MB'),
        (30, 'DealCraft Global', 'Business Development', 'Da Nang', 'https://dealcraft.example.com', 'https://placehold.co/96x96/CCFBF1/0F766E?text=DC')
)
INSERT INTO companies (name, description, location, website, industry, logo_url, created_at, updated_at)
SELECT
    cs.name,
    'Demo company for TTJobs sample data in ' || cs.industry || '.',
    cs.location,
    cs.website,
    cs.industry,
    cs.logo_url,
    NOW(),
    NOW()
FROM company_seed cs
WHERE NOT EXISTS (
    SELECT 1 FROM companies c WHERE LOWER(c.name) = LOWER(cs.name)
);

INSERT INTO skills (name)
SELECT skill_name
FROM unnest(ARRAY[
    'Java', 'Spring Boot', 'React', 'Node.js', 'SQL', 'Docker', 'AWS', 'Python',
    'Sales', 'CRM', 'Negotiation', 'Customer Success', 'SEO', 'Content Marketing',
    'Google Ads', 'Social Media', 'Recruitment', 'HR Operations', 'Payroll',
    'Financial Analysis', 'Risk Management', 'Excel', 'Customer Support',
    'Communication', 'Real Estate Sales', 'Property Management', 'Accounting',
    'Tax', 'Audit', 'Figma', 'UI Design', 'UX Research', 'Partnerships',
    'Market Research', 'Business Strategy'
]::text[]) AS t(skill_name)
WHERE NOT EXISTS (
    SELECT 1 FROM skills s WHERE LOWER(s.name) = LOWER(t.skill_name)
);

WITH company_seed(idx, name) AS (
    VALUES
        (1, 'Astra Tech Labs'), (2, 'BluePeak Commerce'), (3, 'Nova Marketing House'),
        (4, 'PeopleFirst Group'), (5, 'FinEdge Capital'), (6, 'CareLine Services'),
        (7, 'UrbanNest Realty'), (8, 'LedgerPro Advisors'), (9, 'PixelCraft Studio'),
        (10, 'GrowthForge Partners'), (11, 'CloudNexus Software'), (12, 'MarketLane Retail'),
        (13, 'SignalWave Media'), (14, 'TalentBridge Vietnam'), (15, 'TrustBank Digital'),
        (16, 'AnswerHub Support'), (17, 'MetroSpace Property'), (18, 'ClearBooks Consulting'),
        (19, 'BrightUX Collective'), (20, 'ScalePoint Ventures'), (21, 'CodeHarbor Asia'),
        (22, 'PrimeSales Network'), (23, 'BrandPilot Agency'), (24, 'WorkWell People'),
        (25, 'MoneyMap Analytics'), (26, 'HappyDesk CX'), (27, 'HomeGrid Realty'),
        (28, 'TaxWise Partners'), (29, 'MotionBox Design'), (30, 'DealCraft Global')
),
category_seed(idx, category, title_prefix, skill_csv, base_description) AS (
    VALUES
        (1, 'INFORMATION-TECHNOLOGY', 'Software Engineer', 'Java,Spring Boot,React,SQL,Docker', 'Build and maintain production software for growing product teams'),
        (2, 'SALES', 'Sales Executive', 'Sales,CRM,Negotiation,Customer Success', 'Own sales pipeline and close new business opportunities'),
        (3, 'MARKETING', 'Digital Marketing Specialist', 'SEO,Content Marketing,Google Ads,Social Media', 'Plan and execute digital campaigns across key channels'),
        (4, 'HR', 'HR Operations Specialist', 'Recruitment,HR Operations,Payroll,Communication', 'Support hiring, onboarding and people operations'),
        (5, 'FINANCE', 'Financial Analyst', 'Financial Analysis,Risk Management,Excel,SQL', 'Analyze financial performance and support planning decisions'),
        (6, 'CUSTOMER-SERVICE', 'Customer Support Specialist', 'Customer Support,Communication,CRM,Customer Success', 'Help customers resolve issues with clear and friendly support'),
        (7, 'REAL-ESTATE', 'Property Consultant', 'Real Estate Sales,Property Management,Negotiation,CRM', 'Advise customers and manage real estate opportunities'),
        (8, 'ACCOUNTING', 'Accountant', 'Accounting,Tax,Audit,Excel', 'Handle accounting records, reports and compliance tasks'),
        (9, 'DESIGN', 'Product Designer', 'Figma,UI Design,UX Research,Communication', 'Design thoughtful user experiences for web and mobile products'),
        (10, 'BUSINESS-DEVELOPMENT', 'Business Development Executive', 'Partnerships,Market Research,Business Strategy,Negotiation', 'Develop partnerships and new growth opportunities'),
        (11, 'ENGINEERING', 'Solutions Engineer', 'Python,AWS,Docker,SQL', 'Design practical technical solutions for customer and internal needs'),
        (12, 'OPERATIONS', 'Operations Coordinator', 'Excel,Communication,Customer Success,Business Strategy', 'Coordinate daily operations and improve team execution')
),
type_seed(idx, job_type) AS (
    VALUES
        (1, 'Full-time'),
        (2, 'Part-time'),
        (3, 'Remote'),
        (4, 'Hybrid'),
        (5, 'Contract'),
        (6, 'Internship')
),
experience_seed(idx, experience_level, salary_bonus) AS (
    VALUES
        (1, 'Entry', 0),
        (2, 'Junior', 2000000),
        (3, 'Mid', 6000000),
        (4, 'Senior', 12000000),
        (5, 'Lead', 18000000)
),
location_seed(idx, location) AS (
    VALUES
        (1, 'Ha Noi'),
        (2, 'Ho Chi Minh City'),
        (3, 'Da Nang'),
        (4, 'Remote'),
        (5, 'Hybrid')
),
job_seed AS (
    SELECT
        gs.n,
        cs.name AS company_name,
        cat.category,
        cat.title_prefix,
        cat.skill_csv,
        cat.base_description,
        typ.job_type,
        exp.experience_level,
        loc.location,
        (7000000 + exp.salary_bonus + (((gs.n - 1) % 7) * 1500000))::numeric AS salary_min
    FROM generate_series(1, 300) AS gs(n)
    JOIN company_seed cs ON cs.idx = (((gs.n - 1) % 30) + 1)
    JOIN category_seed cat ON cat.idx = (((gs.n - 1) % 12) + 1)
    JOIN type_seed typ ON typ.idx = (((gs.n - 1) % 6) + 1)
    JOIN experience_seed exp ON exp.idx = (((gs.n - 1) % 5) + 1)
    JOIN location_seed loc ON loc.idx = (((gs.n - 1) % 5) + 1)
)
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
    company_id
)
SELECT
    js.title_prefix || ' ' || js.experience_level || ' #' || LPAD(js.n::text, 3, '0'),
    'Demo seed: ' || js.base_description || '. Key skills: ' || js.skill_csv || '. Job type: ' || js.job_type || '.',
    js.location,
    js.salary_min + 4000000,
    js.salary_min,
    js.salary_min + 8000000,
    'VND',
    js.job_type,
    js.experience_level,
    js.category,
    'https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs',
    'open',
    NOW() - (((js.n - 1) % 45) * INTERVAL '1 day'),
    NOW() + ((20 + ((js.n - 1) % 45)) * INTERVAL '1 day'),
    NOW(),
    c.id
FROM job_seed js
JOIN companies c ON LOWER(c.name) = LOWER(js.company_name)
WHERE NOT EXISTS (
    SELECT 1
    FROM jobs existing
    WHERE existing.company_id = c.id
      AND existing.title = js.title_prefix || ' ' || js.experience_level || ' #' || LPAD(js.n::text, 3, '0')
);

WITH category_seed(category, title_prefix, skill_csv) AS (
    VALUES
        ('INFORMATION-TECHNOLOGY', 'Software Engineer', 'Java,Spring Boot,React,SQL,Docker'),
        ('SALES', 'Sales Executive', 'Sales,CRM,Negotiation,Customer Success'),
        ('MARKETING', 'Digital Marketing Specialist', 'SEO,Content Marketing,Google Ads,Social Media'),
        ('HR', 'HR Operations Specialist', 'Recruitment,HR Operations,Payroll,Communication'),
        ('FINANCE', 'Financial Analyst', 'Financial Analysis,Risk Management,Excel,SQL'),
        ('CUSTOMER-SERVICE', 'Customer Support Specialist', 'Customer Support,Communication,CRM,Customer Success'),
        ('REAL-ESTATE', 'Property Consultant', 'Real Estate Sales,Property Management,Negotiation,CRM'),
        ('ACCOUNTING', 'Accountant', 'Accounting,Tax,Audit,Excel'),
        ('DESIGN', 'Product Designer', 'Figma,UI Design,UX Research,Communication'),
        ('BUSINESS-DEVELOPMENT', 'Business Development Executive', 'Partnerships,Market Research,Business Strategy,Negotiation'),
        ('ENGINEERING', 'Solutions Engineer', 'Python,AWS,Docker,SQL'),
        ('OPERATIONS', 'Operations Coordinator', 'Excel,Communication,Customer Success,Business Strategy')
),
demo_job_skills AS (
    SELECT
        j.id AS job_id,
        TRIM(skill_name) AS skill_name
    FROM jobs j
    JOIN category_seed cs ON j.category = cs.category
        AND j.title LIKE cs.title_prefix || '%'
    CROSS JOIN LATERAL regexp_split_to_table(cs.skill_csv, ',') AS skill_name
    WHERE j.description LIKE 'Demo seed:%'
)
INSERT INTO job_skills (job_id, skill_id)
SELECT djs.job_id, s.id
FROM demo_job_skills djs
JOIN skills s ON LOWER(s.name) = LOWER(djs.skill_name)
ON CONFLICT DO NOTHING;
