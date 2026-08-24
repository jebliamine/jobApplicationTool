-- Supports admin user management (list users, change role, enable/disable).

ALTER TABLE japuser
    ADD COLUMN enabled boolean NOT NULL DEFAULT true,
    ADD COLUMN created_at timestamp(6) without time zone;

-- Backfill existing rows (created_at has no data to derive from, so "now" is
-- the best available value) before making it NOT NULL for future rows.
UPDATE japuser SET created_at = now() WHERE created_at IS NULL;

ALTER TABLE japuser
    ALTER COLUMN created_at SET NOT NULL;
