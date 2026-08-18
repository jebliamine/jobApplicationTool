/** Mirrors the response body of GET /api/v1/users/me and PUT /api/v1/users/me. */
export interface UserProfile {
  fullName: string;
  email: string;
  role: 'USER' | 'ADMIN';
}

/** Mirrors the request body of PUT /api/v1/users/me — only self-editable fields. */
export interface UpdateUserRequest {
  fullName: string;
  email: string;
}
