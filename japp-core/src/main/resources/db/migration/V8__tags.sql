-- Tags are a per-user taxonomy (each owner manages their own tag list, same
-- ownership model as Job/Company/Application) applicable to both jobs and
-- applications via two separate join tables. ON DELETE CASCADE on every FK
-- here means deleting a Job, Application, or Tag never leaves orphaned
-- join rows and never blocks the delete.

CREATE TABLE tag (
    id uuid NOT NULL,
    owner_id uuid NOT NULL,
    name character varying(100) NOT NULL,
    created_at timestamp(6) without time zone,
    CONSTRAINT tag_pkey PRIMARY KEY (id),
    CONSTRAINT fk_tag_owner FOREIGN KEY (owner_id) REFERENCES japuser(id),
    CONSTRAINT uk_tag_owner_name UNIQUE (owner_id, name)
);

CREATE TABLE job_tag (
    job_id uuid NOT NULL,
    tag_id uuid NOT NULL,
    CONSTRAINT job_tag_pkey PRIMARY KEY (job_id, tag_id),
    CONSTRAINT fk_job_tag_job FOREIGN KEY (job_id) REFERENCES job(id) ON DELETE CASCADE,
    CONSTRAINT fk_job_tag_tag FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE
);

CREATE TABLE application_tag (
    application_id uuid NOT NULL,
    tag_id uuid NOT NULL,
    CONSTRAINT application_tag_pkey PRIMARY KEY (application_id, tag_id),
    CONSTRAINT fk_application_tag_application FOREIGN KEY (application_id) REFERENCES application(id) ON DELETE CASCADE,
    CONSTRAINT fk_application_tag_tag FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE
);
