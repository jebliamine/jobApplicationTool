import { HttpErrorResponse } from '@angular/common/http';

/**
 * Maps a failed PUT /users/me response to user-facing text. Unlike auth
 * errors, this endpoint's UserProfileExceptionHandler returns a structured
 * `{ message }` body for 400 (invalid input) and 409 (duplicate email), so
 * those are read directly; everything else falls back to a generic message.
 */
export function describeProfileUpdateError(error: HttpErrorResponse): string {
  if (error.status === 400 || error.status === 409) {
    const message = error.error?.message;
    if (typeof message === 'string' && message.trim()) {
      return message;
    }
  }
  if (error.status === 0) {
    return 'Unable to reach the server. Check your connection and try again.';
  }
  return 'We could not save your changes. Please try again.';
}

/**
 * Maps a failed PUT /users/me/password response to user-facing text. The
 * same UserProfileExceptionHandler returns a structured `{ message }` body
 * for 400 (incorrect current password / new password too short).
 */
export function describeChangePasswordError(error: HttpErrorResponse): string {
  if (error.status === 400) {
    const message = error.error?.message;
    if (typeof message === 'string' && message.trim()) {
      return message;
    }
  }
  if (error.status === 0) {
    return 'Unable to reach the server. Check your connection and try again.';
  }
  return 'We could not change your password. Please try again.';
}
