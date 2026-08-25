import { inject } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { CanActivateFn, Router } from '@angular/router';
import { filter, map, take } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { UserService } from '../user/user.service';

/**
 * UX/navigation protection only — hides admin pages from non-admins and
 * keeps them from landing on a page they can't use. It is NOT a security
 * boundary: every admin endpoint enforces its own ADMIN check server-side
 * (see AdminAiProviderService and friends), which remains the sole
 * authoritative authorization mechanism, matching every other admin action
 * in this app.
 */
export const adminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const userService = inject(UserService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    return router.createUrlTree(['/login']);
  }

  // authGuard already triggered UserService.ensureLoaded(), but that fetch is
  // async — on a fresh load of an admin URL (bookmark, reload), currentUser()
  // can still be null here even for a real admin, incorrectly bouncing them
  // to /dashboard. Wait for the profile fetch to settle before deciding,
  // instead of reading a signal that might still be mid-request. Resolves
  // immediately (no extra wait) once the profile is already loaded.
  return toObservable(userService.loading).pipe(
    filter((loading) => !loading),
    take(1),
    map(() => (userService.currentUser()?.role === 'ADMIN' ? true : router.createUrlTree(['/dashboard']))),
  );
};
