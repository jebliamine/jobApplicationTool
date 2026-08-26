-- Both columns are nullable: most existing users have no avatar, and the app falls back to an
-- initials-based avatar in that case (see User.getAvatarStorageKey()). content_type is stored
-- alongside the storage key since the file on disk carries no metadata of its own — needed to set
-- the correct Content-Type header when serving GET /api/v1/users/{id}/avatar.
ALTER TABLE japuser ADD COLUMN avatar_storage_key character varying(255);
ALTER TABLE japuser ADD COLUMN avatar_content_type character varying(100);
