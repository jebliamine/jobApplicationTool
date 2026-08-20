-- Application-tracking maturity fields (deadline/follow-up/interview date,
-- contact person) and a free-text salary range on Job. All nullable and
-- additive — existing rows simply get NULL in the new columns.

ALTER TABLE application
    ADD COLUMN deadline date,
    ADD COLUMN follow_up_date date,
    ADD COLUMN interview_date date,
    ADD COLUMN contact_person character varying(255);

ALTER TABLE job
    ADD COLUMN salary_range character varying(255);
