-- Reminders themselves stay derived from Application's own deadline/follow_up_date/
-- interview_date fields (no duplicated reminder rows to keep in sync) — this table only
-- persists which specific (user, application, kind, due_date) reminder instance the user
-- has dismissed or snoozed, so the notification bell survives a reload instead of the
-- previous purely client-computed, un-dismissable feed. The unique constraint means
-- re-dismissing/re-snoozing the same reminder instance updates one row rather than growing.

CREATE TABLE reminder_dismissal (
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    application_id uuid NOT NULL,
    kind character varying(20) NOT NULL,
    due_date date NOT NULL,
    snoozed_until date,
    created_at timestamp(6) without time zone,
    CONSTRAINT reminder_dismissal_pkey PRIMARY KEY (id),
    CONSTRAINT reminder_dismissal_kind_check CHECK (kind::text = ANY (ARRAY['DEADLINE'::character varying, 'FOLLOW_UP'::character varying, 'INTERVIEW'::character varying]::text[])),
    CONSTRAINT fk_reminder_dismissal_user FOREIGN KEY (user_id) REFERENCES japuser(id) ON DELETE CASCADE,
    CONSTRAINT fk_reminder_dismissal_application FOREIGN KEY (application_id) REFERENCES application(id) ON DELETE CASCADE,
    CONSTRAINT uk_reminder_dismissal UNIQUE (user_id, application_id, kind, due_date)
);

CREATE INDEX idx_reminder_dismissal_user ON reminder_dismissal(user_id);
