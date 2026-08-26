/**
 * Mirrors the response body of GET /api/v1/users/me and PUT /api/v1/users/me — also embedded as
 * an owner reference on Job/Company/Application/etc, which is why emailVerified is optional here:
 * every real response includes it, but the many test fixtures across the app that construct a
 * bare owner literal for an unrelated resource shouldn't all need updating for a field they don't
 * care about.
 */
export interface UserProfile {
  fullName: string;
  email: string;
  role: 'USER' | 'ADMIN';
  emailVerified?: boolean;
}

/** Mirrors the request body of PUT /api/v1/users/me — only self-editable fields. */
export interface UpdateUserRequest {
  fullName: string;
  email: string;
}

/** Mirrors the request body of PUT /api/v1/users/me/password. */
export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}
