-- Wires up CVProfile/Experience, which existed as scaffolded tables since V1 but were
-- never actually populated by any code path: no FK back to cvdocument, and the
-- unmapped @OneToMany on CVProfile.experiences caused Hibernate to generate the
-- cvprofile_experiences join table below instead of using experience.cv_id (which
-- already existed and was always the intended owning-side FK).

ALTER TABLE cvprofile
    ADD COLUMN cv_document_id uuid,
    ADD COLUMN status character varying(20) NOT NULL DEFAULT 'NOT_ATTEMPTED',
    ADD COLUMN error_message character varying(2000),
    ADD COLUMN generated_at timestamp(6) without time zone;

-- Safe as NOT NULL with no backfill: nothing has ever inserted a cvprofile row.
ALTER TABLE cvprofile
    ALTER COLUMN cv_document_id SET NOT NULL;

ALTER TABLE cvprofile
    ALTER COLUMN summary TYPE character varying(3000);

ALTER TABLE cvprofile
    ADD CONSTRAINT uk_cvprofile_cv_document UNIQUE (cv_document_id),
    ADD CONSTRAINT fk_cvprofile_cv_document FOREIGN KEY (cv_document_id) REFERENCES cvdocument(id);

DROP TABLE cvprofile_experiences;
