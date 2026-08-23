-- Adds CV text extraction caching to cvdocument, and repurposes the dead
-- generationrequest.cv_text column (unused since job_description_snapshot
-- replaced the older job_description_text/cv_text pair) as a proper
-- reproducibility snapshot, matching job_description_snapshot's pattern.

ALTER TABLE cvdocument
    ADD COLUMN extracted_text text,
    ADD COLUMN extraction_status character varying(20) NOT NULL DEFAULT 'NOT_ATTEMPTED',
    ADD COLUMN extraction_quality character varying(20),
    ADD COLUMN extracted_at timestamp(6) without time zone;

ALTER TABLE cvdocument
    ADD CONSTRAINT cvdocument_extraction_status_check
    CHECK (extraction_status IN ('NOT_ATTEMPTED', 'COMPLETED', 'FAILED'));

ALTER TABLE generationrequest
    RENAME COLUMN cv_text TO cv_text_snapshot;

ALTER TABLE generationrequest
    ALTER COLUMN cv_text_snapshot TYPE character varying(8000);
