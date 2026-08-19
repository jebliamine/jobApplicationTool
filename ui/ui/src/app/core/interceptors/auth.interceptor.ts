import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../auth/auth.service';

/**
 * Attaches `Authorization: Bearer <token>` to outgoing requests when a token
 * is present, and logs out + redirects to /login on a 401, or on a 403 that
 * signals an invalid/anonymous session, so an expired/invalid token doesn't
 * leave the user stuck on a broken page.
 *
 * This backend's SecurityConfig has no `AuthenticationEntryPoint`, so
 * `anyRequest().authenticated()` rejects an anonymous/invalid-token request
 * with 403, not 401 (confirmed against the running backend: GET /users/me
 * with no/garbage token → 403 with Spring's default body, no `message`
 * field). Per-resource ownership checks (Job/Company/CV/Application
 * services) now also return 403, but from an authenticated, still-valid
 * session — those responses always carry a `{ message: string }` body from
 * this app's own exception handlers. Only the bodyless/message-less form is
 * treated as a session problem; the rest is left for the calling component
 * to display inline (see describeApiError).
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const token = authService.getToken();

  const authorizedReq = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(authorizedReq).pipe(
    catchError((error) => {
      if (isSessionError(error) && authService.isAuthenticated()) {
        authService.logout();
        router.navigate(['/login']);
      }
      return throwError(() => error);
    }),
  );
};

function isSessionError(error: unknown): boolean {
  if (!(error instanceof HttpErrorResponse)) {
    return false;
  }
  if (error.status === 401) {
    return true;
  }
  if (error.status === 403) {
    const message = error.error?.message;
    return !(typeof message === 'string' && message.trim());
  }
  return false;
}
