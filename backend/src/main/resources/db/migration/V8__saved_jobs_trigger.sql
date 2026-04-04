-- Ensure saved_at is always populated at database level.
CREATE OR REPLACE FUNCTION set_saved_jobs_saved_at()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.saved_at IS NULL THEN
        NEW.saved_at := NOW();
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_saved_jobs_set_saved_at ON saved_jobs;

CREATE TRIGGER trg_saved_jobs_set_saved_at
BEFORE INSERT ON saved_jobs
FOR EACH ROW
EXECUTE FUNCTION set_saved_jobs_saved_at();
