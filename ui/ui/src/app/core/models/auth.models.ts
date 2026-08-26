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

/** Mirrors POST /api/v1/auth/forgot-password request body. */
export interface ForgotPasswordRequest {
  email: string;
}

/** Mirrors POST /api/v1/auth/reset-password request body. */
export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}

/** Mirrors POST /api/v1/auth/verify-email request body. */
export interface VerifyEmailRequest {
  token: string;
}

/** Mirrors POST /api/v1/auth/resend-verification request body. */
export interface ResendVerificationRequest {
  email: string;
}
