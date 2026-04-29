CREATE TABLE IF NOT EXISTS company_follows (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    followed_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_company_follows_user_company UNIQUE (user_id, company_id)
);

CREATE INDEX IF NOT EXISTS idx_company_follows_company_id ON company_follows(company_id);
CREATE INDEX IF NOT EXISTS idx_company_follows_user_followed_at ON company_follows(user_id, followed_at DESC);
