/** Mirrors the response body of GET /api/v1/admin/users and the update endpoints. */
export interface AdminUserResponse {
  id: string;
  fullName: string | null;
  email: string;
  role: 'USER' | 'ADMIN';
  enabled: boolean;
  createdAt: string;
}

/** Mirrors the request body of POST /api/v1/admin/users. */
export interface AdminCreateUserRequest {
  fullName: string;
  email: string;
  password: string;
  role: 'USER' | 'ADMIN';
}
