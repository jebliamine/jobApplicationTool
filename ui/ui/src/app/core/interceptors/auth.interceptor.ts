import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../auth/auth.service';

/**
 * Attaches `Authorization: Bearer <token>` to outgoing requests when a token
 * is present, and logs out + redirects to /login on a 401/403 response so an
 * expired/invalid token doesn't leave the user stuck on a broken page.
 *
 * This backend's SecurityConfig has no fine-grained authorization (no roles
 * or per-resource ownership checks) — `anyRequest().authenticated()` rejects
 * an anonymous/invalid-token request with 403, not 401 (confirmed against
 * the running backend: GET /users/me with no/garbage token → 403). Since
 * there's nothing else a 403 could mean here, both statuses are treated the
 * same.
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
      if ((error.status === 401 || error.status === 403) && authService.isAuthenticated()) {
        authService.logout();
        router.navigate(['/login']);
      }
      return throwError(() => error);
    }),
  );
};
