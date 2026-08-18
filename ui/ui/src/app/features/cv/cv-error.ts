import { HttpErrorResponse } from '@angular/common/http';

/** Maps a failed POST /cv response to user-facing text, reading the backend's {message} body for 400s. */
export function describeCvError(error: HttpErrorResponse): string {
  if (error.status === 400) {
    const message = error.error?.message;
    if (typeof message === 'string' && message.trim()) {
      return message;
    }
  }
  if (error.status === 0) {
    return 'Unable to reach the server. Check your connection and try again.';
  }
  return 'We could not upload your CV. Please try again.';
}
