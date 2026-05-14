UPDATE companies
SET verification_status = CASE
    WHEN UPPER(TRIM(verification_status)) IN ('VERIFIED', 'VERIFY', 'APPROVED') THEN 'VERIFIED'
    WHEN UPPER(TRIM(verification_status)) IN ('PENDING', 'WAITING', 'WAITING_VERIFY') THEN 'PENDING'
    WHEN UPPER(TRIM(verification_status)) IN ('REJECTED', 'REJECT') THEN 'REJECTED'
    WHEN UPPER(TRIM(verification_status)) IN ('SUSPENDED', 'SUSPEND') THEN 'SUSPENDED'
    ELSE 'PENDING'
END
WHERE verification_status IS NULL
   OR verification_status <> UPPER(TRIM(verification_status))
   OR UPPER(TRIM(verification_status)) NOT IN ('PENDING', 'VERIFIED', 'REJECTED', 'SUSPENDED');

UPDATE company_verifications
SET status = CASE
    WHEN UPPER(TRIM(status)) IN ('VERIFIED', 'VERIFY', 'APPROVED') THEN 'VERIFIED'
    WHEN UPPER(TRIM(status)) IN ('PENDING', 'WAITING', 'WAITING_VERIFY') THEN 'PENDING'
    WHEN UPPER(TRIM(status)) IN ('REJECTED', 'REJECT') THEN 'REJECTED'
    WHEN UPPER(TRIM(status)) IN ('SUSPENDED', 'SUSPEND') THEN 'SUSPENDED'
    ELSE 'PENDING'
END
WHERE status IS NULL
   OR status <> UPPER(TRIM(status))
   OR UPPER(TRIM(status)) NOT IN ('PENDING', 'VERIFIED', 'REJECTED', 'SUSPENDED');

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_companies_verification_status'
    ) THEN
        ALTER TABLE companies
            ADD CONSTRAINT ck_companies_verification_status
            CHECK (verification_status IN ('PENDING', 'VERIFIED', 'REJECTED', 'SUSPENDED'));
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_company_verifications_status'
    ) THEN
        ALTER TABLE company_verifications
            ADD CONSTRAINT ck_company_verifications_status
            CHECK (status IN ('PENDING', 'VERIFIED', 'REJECTED', 'SUSPENDED'));
    END IF;
END $$;
