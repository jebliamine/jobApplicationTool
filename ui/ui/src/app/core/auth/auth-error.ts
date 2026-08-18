import { HttpErrorResponse } from '@angular/common/http';

/**
 * Maps a failed /auth/login or /auth/register response to user-facing text.
 * The backend has no global exception handler, so auth failures (bad
 * credentials, duplicate email, user not found) all surface as a bare 500
 * with no structured error body — status codes below 500 are handled in
 * case that changes, but today only the 0/500 branches are reachable.
 */
export function describeAuthError(error: HttpErrorResponse): string {
  if (error.status === 0) {
    return 'Unable to reach the server. Check your connection and try again.';
  }
  if (error.status === 401 || error.status === 403) {
    return 'Invalid email or password.';
  }
  if (error.status === 409) {
    return 'An account with this email already exists.';
  }
  if (error.status >= 500) {
    return 'Something went wrong on our end. Please try again.';
  }
  return 'We could not process your request. Please check your details and try again.';
}
