/** Mirrors POST /api/v1/auth/login request body. */
export interface LoginRequest {
  email: string;
  password: string;
}

/** Mirrors POST /api/v1/auth/register request body. */
export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
}

/** Mirrors the response body shared by /auth/login and /auth/register. */
export interface AuthResponse {
  token: string;
}
