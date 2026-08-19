import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
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

  return userService.currentUser()?.role === 'ADMIN' ? true : router.createUrlTree(['/dashboard']);
};
