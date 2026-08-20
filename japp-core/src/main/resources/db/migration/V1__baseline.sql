-- Baseline migration for adopting Flyway on a database whose schema was
-- previously managed entirely by Hibernate `ddl-auto: update`.
--
-- This is the exact schema captured from the live dev database at the time
-- Flyway was introduced (table/column/constraint names included, verbatim,
-- as Hibernate generated them) — not a hand-cleaned version. Against an
-- existing database it never actually runs (spring.flyway.baseline-on-migrate
-- records it as already-applied); against a fresh database (a new clone, CI)
-- it recreates the schema from scratch. Statements are ordered by foreign-key
-- dependency so a fresh run succeeds top to bottom.

CREATE TABLE japuser (
    id uuid NOT NULL,
    email character varying(255) NOT NULL,
    full_name character varying(255),
    password_hash character varying(255) NOT NULL,
    role character varying(255),
    CONSTRAINT japuser_role_check CHECK (role::text = ANY (ARRAY['USER'::character varying, 'ADMIN'::character varying]::text[])),
    CONSTRAINT japuser_pkey PRIMARY KEY (id),
    CONSTRAINT ukgcvce9vol6lund04eq37ipfps UNIQUE (email)
);

CREATE TABLE cvprofile (
    id uuid NOT NULL,
    full_name character varying(255),
    summary character varying(255),
    CONSTRAINT cvprofile_pkey PRIMARY KEY (id)
);

CREATE TABLE company (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone,
    location character varying(255),
    name character varying(255) NOT NULL,
    notes character varying(2000),
    updated_at timestamp(6) without time zone,
    website character varying(255),
    owner_id uuid NOT NULL,
    CONSTRAINT company_pkey PRIMARY KEY (id),
    CONSTRAINT fk1r95royqfg6rp8po611ugmvbf FOREIGN KEY (owner_id) REFERENCES japuser(id)
);

CREATE TABLE cvdocument (
    id uuid NOT NULL,
    type smallint,
    url character varying(255),
    content_type character varying(255),
    created_at timestamp(6) without time zone,
    file_name character varying(255),
    file_path character varying(255),
    size bigint,
    title character varying(255),
    updated_at timestamp(6) without time zone,
    owner_id uuid,
    storage_key character varying(255),
    CONSTRAINT cvdocument_type_check CHECK (type >= 0 AND type <= 1),
    CONSTRAINT cvdocument_pkey PRIMARY KEY (id),
    CONSTRAINT fkk43nb30g4ryb6wfbxokcalh7b FOREIGN KEY (owner_id) REFERENCES japuser(id)
);

CREATE TABLE experience (
    id uuid NOT NULL,
    company character varying(255),
    description character varying(3000),
    end_date date,
    start_date date,
    title character varying(255),
    cv_id uuid,
    CONSTRAINT experience_pkey PRIMARY KEY (id),
    CONSTRAINT fk22h9npccll4uuo67vj7sc8ou9 FOREIGN KEY (cv_id) REFERENCES cvprofile(id)
);

CREATE TABLE job (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone,
    description character varying(8000) NOT NULL,
    employment_type character varying(255),
    location character varying(255),
    source character varying(255),
    title character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone,
    url character varying(255),
    work_mode character varying(255),
    company_id uuid NOT NULL,
    owner_id uuid NOT NULL,
    CONSTRAINT job_employment_type_check CHECK (employment_type::text = ANY (ARRAY['FULL_TIME'::character varying, 'PART_TIME'::character varying, 'CONTRACT'::character varying, 'INTERNSHIP'::character varying, 'FREELANCE'::character varying]::text[])),
    CONSTRAINT job_work_mode_check CHECK (work_mode::text = ANY (ARRAY['REMOTE'::character varying, 'HYBRID'::character varying, 'ONSITE'::character varying]::text[])),
    CONSTRAINT job_pkey PRIMARY KEY (id),
    CONSTRAINT fk5q04favsasq8y70bsei7wv8fc FOREIGN KEY (company_id) REFERENCES company(id),
    CONSTRAINT fkq6vv0kkpruljyft47rhnmv2lb FOREIGN KEY (owner_id) REFERENCES japuser(id)
);

CREATE TABLE cvprofile_experiences (
    cvprofile_id uuid NOT NULL,
    experiences_id uuid NOT NULL,
    CONSTRAINT ukt4obung49djk7jg7g3qlgx6s3 UNIQUE (experiences_id),
    CONSTRAINT fk4ni2u5u2cjck0h4jlncsrwnon FOREIGN KEY (experiences_id) REFERENCES experience(id),
    CONSTRAINT fkq77f3lx8gq901smr4csyk2ok3 FOREIGN KEY (cvprofile_id) REFERENCES cvprofile(id)
);

CREATE TABLE generationrequest (
    id uuid NOT NULL,
    cv_text character varying(255),
    job_description_text character varying(255),
    completed_at timestamp(6) without time zone,
    created_at timestamp(6) without time zone,
    error_message character varying(2000),
    job_description_snapshot character varying(8000),
    model character varying(255),
    provider character varying(255),
    started_at timestamp(6) without time zone,
    status character varying(255) NOT NULL,
    cv_document_id uuid,
    job_id uuid NOT NULL,
    user_id uuid NOT NULL,
    CONSTRAINT generationrequest_status_check CHECK (status::text = ANY (ARRAY['PENDING'::character varying, 'IN_PROGRESS'::character varying, 'COMPLETED'::character varying, 'FAILED'::character varying]::text[])),
    CONSTRAINT generationrequest_pkey PRIMARY KEY (id),
    CONSTRAINT fkcbp4m92de8u2s3rib5m0cgmwm FOREIGN KEY (job_id) REFERENCES job(id),
    CONSTRAINT fkg0c3qo174awm0y5koqg25clbt FOREIGN KEY (user_id) REFERENCES japuser(id),
    CONSTRAINT fkkryrc4j56x7gabky8wmpgsres FOREIGN KEY (cv_document_id) REFERENCES cvdocument(id) ON DELETE SET NULL
);

CREATE TABLE coverletter (
    id uuid NOT NULL,
    request_id uuid,
    result_text character varying(8000),
    created_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    generation_request_id uuid NOT NULL,
    owner_id uuid NOT NULL,
    archived boolean DEFAULT false NOT NULL,
    CONSTRAINT coverletter_pkey PRIMARY KEY (id),
    CONSTRAINT ukj8ogfy08nwdmxq3g4xos6ku3o UNIQUE (generation_request_id),
    CONSTRAINT fkj9hof1eu886fxm7258j61p83c FOREIGN KEY (owner_id) REFERENCES japuser(id),
    CONSTRAINT fkljmt6a0ocears1wit26nn7wy0 FOREIGN KEY (generation_request_id) REFERENCES generationrequest(id)
);

CREATE TABLE application (
    id uuid NOT NULL,
    applied_at date,
    created_at timestamp(6) without time zone,
    notes character varying(4000),
    status character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone,
    cv_document_id uuid,
    job_id uuid NOT NULL,
    user_id uuid NOT NULL,
    cover_letter_id uuid,
    CONSTRAINT application_status_check CHECK (status::text = ANY (ARRAY['APPLIED'::character varying, 'PHONE_SCREEN'::character varying, 'INTERVIEWING'::character varying, 'OFFER'::character varying, 'REJECTED'::character varying, 'WITHDRAWN'::character varying, 'ACCEPTED'::character varying]::text[])),
    CONSTRAINT application_pkey PRIMARY KEY (id),
    CONSTRAINT fkb911ni0telv5itvyaca39ekej FOREIGN KEY (cv_document_id) REFERENCES cvdocument(id) ON DELETE SET NULL,
    CONSTRAINT fkjpp0craqt982760uvb97rlpx7 FOREIGN KEY (user_id) REFERENCES japuser(id),
    CONSTRAINT fkls6sryk64ga8o5t4bym8qu3vm FOREIGN KEY (job_id) REFERENCES job(id),
    CONSTRAINT fkm6kp0jikrmk45e14gapbm48ec FOREIGN KEY (cover_letter_id) REFERENCES coverletter(id) ON DELETE SET NULL
);

CREATE TABLE ai_provider_configuration (
    id uuid NOT NULL,
    base_url character varying(255),
    created_at timestamp(6) without time zone,
    default_model character varying(255),
    enabled boolean NOT NULL,
    encrypted_api_key character varying(2000),
    provider character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone,
    updated_by uuid,
    CONSTRAINT ai_provider_configuration_pkey PRIMARY KEY (id),
    CONSTRAINT uk67jbcjopbn4hgjuuoh9yuynlm UNIQUE (provider),
    CONSTRAINT fk9vtjhddmruhyft61v5amxkabq FOREIGN KEY (updated_by) REFERENCES japuser(id) ON DELETE SET NULL
);

-- Explicit @Index-annotated indexes (primary-key and unique-constraint
-- backing indexes above are created automatically by Postgres and are not
-- repeated here).
CREATE INDEX idx_company_owner ON company (owner_id);
CREATE INDEX idx_job_owner ON job (owner_id);
CREATE INDEX idx_job_company ON job (company_id);
CREATE INDEX idx_application_user ON application (user_id);
CREATE INDEX idx_application_job ON application (job_id);
CREATE INDEX idx_application_cv_document ON application (cv_document_id);
CREATE INDEX idx_application_status ON application (status);
CREATE INDEX idx_application_cover_letter ON application (cover_letter_id);
CREATE INDEX idx_coverletter_owner ON coverletter (owner_id);
CREATE INDEX idx_coverletter_generation_request ON coverletter (generation_request_id);
CREATE INDEX idx_coverletter_archived ON coverletter (archived);
CREATE INDEX idx_generationrequest_user ON generationrequest (user_id);
CREATE INDEX idx_generationrequest_job ON generationrequest (job_id);
CREATE INDEX idx_generationrequest_cv_document ON generationrequest (cv_document_id);
CREATE INDEX idx_generationrequest_status ON generationrequest (status);
