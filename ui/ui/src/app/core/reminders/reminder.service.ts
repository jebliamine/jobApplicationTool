import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { EMPTY, Observable, catchError, of, switchMap, tap, timer } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from '../auth/auth.service';
import { Reminder, ReminderDismissRequest } from './reminder.models';

// 5 minutes: reminders are deadline/follow-up/interview dates, not chat —
// no need for anything close to real-time, just not "only when the bell is
// clicked" (the previous behavior, which could go stale for a whole session).
const REFRESH_INTERVAL_MS = 5 * 60 * 1000;

/**
 * Singleton, app-wide reminder feed for the topbar notification bell. Backed by
 * GET /api/v1/reminders — the backend computes the reminders (from Application's own
 * deadline/followUpDate/interviewDate) and excludes anything already dismissed/still
 * snoozed; this service is just a polled cache plus the dismiss action.
 */
@Injectable({ providedIn: 'root' })
export class ReminderService {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);
  private readonly baseUrl = `${environment.apiUrl}/reminders`;

  private readonly _reminders = signal<Reminder[]>([]);
  readonly reminders = this._reminders.asReadonly();

  constructor() {
    // Polling only runs while authenticated — this is a root-scoped
    // singleton that outlives login/logout, so without this it would keep
    // hitting /reminders (and getting 401s) in the background forever
    // after the user logs out.
    toObservable(this.authService.isAuthenticated)
      .pipe(
        switchMap((authenticated) => {
          if (!authenticated) {
            this._reminders.set([]);
            return EMPTY;
          }
          return timer(0, REFRESH_INTERVAL_MS).pipe(switchMap(() => this.fetch()));
        }),
      )
      .subscribe();
  }

  /** Re-fetches reminders on demand — still called when the bell menu opens, for an immediate refresh on top of the periodic poll. */
  refresh(): void {
    this.fetch().subscribe();
  }

  /** POST /reminders/dismiss, then refreshes the feed so the dismissed/snoozed item disappears immediately. */
  dismiss(request: ReminderDismissRequest): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/dismiss`, request).pipe(tap(() => this.refresh()));
  }

  // Never errors — a failed request must not kill the outer timer/switchMap
  // chain (an unhandled error there would silently stop all future polling).
  private fetch(): Observable<Reminder[]> {
    return this.http.get<Reminder[]>(this.baseUrl).pipe(
      tap((reminders) => this._reminders.set(reminders)),
      catchError(() => {
        this._reminders.set([]);
        return of([]);
      }),
    );
  }
}
