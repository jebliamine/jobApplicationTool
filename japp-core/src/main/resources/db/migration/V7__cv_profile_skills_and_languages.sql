-- Adds skills and languages to the structured CV profile, mirroring how
-- experience is already modeled (a child table with a plain FK named cv_id
-- back to cvprofile, deletion handled by CVProfile's JPA cascade rather than
-- an ON DELETE clause here).

CREATE TABLE skill (
    id uuid NOT NULL,
    name character varying(255) NOT NULL,
    cv_id uuid,
    CONSTRAINT skill_pkey PRIMARY KEY (id),
    CONSTRAINT fk_skill_cv_profile FOREIGN KEY (cv_id) REFERENCES cvprofile(id)
);

CREATE TABLE language (
    id uuid NOT NULL,
    name character varying(255) NOT NULL,
    level character varying(255),
    cv_id uuid,
    CONSTRAINT language_pkey PRIMARY KEY (id),
    CONSTRAINT fk_language_cv_profile FOREIGN KEY (cv_id) REFERENCES cvprofile(id)
);
