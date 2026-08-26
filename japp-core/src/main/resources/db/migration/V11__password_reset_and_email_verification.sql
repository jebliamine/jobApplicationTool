-- email_verified defaults true so every already-existing user (seeded admin, anyone registered
-- before this migration) keeps unrestricted access — only new self-registrations are explicitly
-- flagged unverified going forward (see EmailVerificationService). Nothing currently gates login
-- or feature access on this flag; it only drives the "verify your email" UI affordance.
ALTER TABLE japuser ADD COLUMN email_verified boolean NOT NULL DEFAULT true;

-- One table for both password-reset and email-verification tokens (discriminated by `type`)
-- since the shape and lifecycle — issued, optionally consumed once, expires — are identical.
CREATE TABLE user_token (
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    token character varying(255) NOT NULL,
    type character varying(30) NOT NULL,
    expires_at timestamp(6) without time zone NOT NULL,
    used_at timestamp(6) without time zone,
    created_at timestamp(6) without time zone NOT NULL,
    CONSTRAINT user_token_pkey PRIMARY KEY (id),
    CONSTRAINT user_token_type_check CHECK (type::text = ANY (ARRAY['PASSWORD_RESET'::character varying, 'EMAIL_VERIFICATION'::character varying]::text[])),
    CONSTRAINT fk_user_token_user FOREIGN KEY (user_id) REFERENCES japuser(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_token_token UNIQUE (token)
);

CREATE INDEX idx_user_token_user ON user_token(user_id);
