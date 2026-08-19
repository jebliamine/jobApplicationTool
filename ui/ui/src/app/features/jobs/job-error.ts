import { HttpErrorResponse } from '@angular/common/http';

/** Maps a failed Company/Job request to user-facing text, reading the backend's {message} body. */
export function describeJobError(error: HttpErrorResponse): string {
  if (error.status === 400 || error.status === 403 || error.status === 404) {
    const message = error.error?.message;
    if (typeof message === 'string' && message.trim()) {
      return message;
    }
  }
  if (error.status === 0) {
    return 'Unable to reach the server. Check your connection and try again.';
  }
  return 'Something went wrong. Please try again.';
}
