-- Replaces the single application.interview_date column with a real multi-round pipeline:
-- one interview_stage row per round (phone screen, onsite, offer, ...), each independently
-- dated, noted, and markable as completed. Any existing interview_date value is carried
-- forward as a first "Interview" stage before the column is dropped, so no data is lost.

CREATE TABLE interview_stage (
    id uuid NOT NULL,
    application_id uuid NOT NULL,
    title character varying(255) NOT NULL,
    scheduled_date date,
    notes character varying(2000),
    completed boolean NOT NULL DEFAULT false,
    created_at timestamp(6) without time zone,
    CONSTRAINT interview_stage_pkey PRIMARY KEY (id),
    CONSTRAINT fk_interview_stage_application FOREIGN KEY (application_id) REFERENCES application(id) ON DELETE CASCADE
);

CREATE INDEX idx_interview_stage_application ON interview_stage(application_id);

INSERT INTO interview_stage (id, application_id, title, scheduled_date, completed, created_at)
SELECT gen_random_uuid(), id, 'Interview', interview_date, false, now()
FROM application
WHERE interview_date IS NOT NULL;

ALTER TABLE application DROP COLUMN interview_date;
