import { Injectable, computed, inject, signal } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { EMPTY, Observable, catchError, of, switchMap, tap, timer } from 'rxjs';
import { ApplicationService } from '../../features/applications/application.service';
import { ApplicationResponse } from '../../features/applications/application.models';
import { AuthService } from '../auth/auth.service';
import { UserService } from '../user/user.service';
import { Reminder, buildReminders } from './reminder.models';

// 5 minutes: reminders are deadline/follow-up/interview dates, not chat —
// no need for anything close to real-time, just not "only when the bell is
// clicked" (the previous behavior, which could go stale for a whole session).
const REFRESH_INTERVAL_MS = 5 * 60 * 1000;

/**
 * Singleton, app-wide reminder feed for the topbar notification bell.
 * Reuses ApplicationService.list() — no dedicated backend endpoint exists
 * or is needed, since classification happens entirely client-side (see
 * reminder.models.ts).
 */
@Injectable({ providedIn: 'root' })
export class ReminderService {
  private readonly applicationService = inject(ApplicationService);
  private readonly userService = inject(UserService);
  private readonly authService = inject(AuthService);

  private readonly _applications = signal<ApplicationResponse[]>([]);

  readonly reminders = computed(() =>
    buildReminders(this._applications(), this.userService.currentUser()?.email ?? ''),
  );

  constructor() {
    // Polling only runs while authenticated — this is a root-scoped
    // singleton that outlives login/logout, so without this it would keep
    // hitting /applications (and getting 401s) in the background forever
    // after the user logs out.
    toObservable(this.authService.isAuthenticated)
      .pipe(
        switchMap((authenticated) => {
          if (!authenticated) {
            this._applications.set([]);
            return EMPTY;
          }
          return timer(0, REFRESH_INTERVAL_MS).pipe(switchMap(() => this.fetch()));
        }),
      )
      .subscribe();
  }

  /** Re-fetches applications on demand — still called when the bell menu opens, for an immediate refresh on top of the periodic poll. */
  refresh(): void {
    this.fetch().subscribe();
  }

  // Never errors — a failed request must not kill the outer timer/switchMap
  // chain (an unhandled error there would silently stop all future polling).
  private fetch(): Observable<ApplicationResponse[]> {
    return this.applicationService.list().pipe(
      tap((applications) => this._applications.set(applications)),
      catchError(() => {
        this._applications.set([]);
        return of([]);
      }),
    );
  }
}
