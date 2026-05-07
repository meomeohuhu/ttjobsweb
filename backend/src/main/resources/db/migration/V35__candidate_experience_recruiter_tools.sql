ALTER TABLE application_ai_scores
    ADD COLUMN IF NOT EXISTS pros TEXT,
    ADD COLUMN IF NOT EXISTS cons TEXT;

CREATE TABLE IF NOT EXISTS company_reviews (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    rating INTEGER NOT NULL,
    pros TEXT,
    cons TEXT,
    salary NUMERIC(14, 2),
    anonymous BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_company_reviews_company_created
    ON company_reviews(company_id, created_at DESC);

