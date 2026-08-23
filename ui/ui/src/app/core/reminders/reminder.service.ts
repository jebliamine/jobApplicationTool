import { Injectable, computed, inject, signal } from '@angular/core';
import { ApplicationService } from '../../features/applications/application.service';
import { ApplicationResponse } from '../../features/applications/application.models';
import { UserService } from '../user/user.service';
import { Reminder, buildReminders } from './reminder.models';

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

  private readonly _applications = signal<ApplicationResponse[]>([]);

  readonly reminders = computed(() =>
    buildReminders(this._applications(), this.userService.currentUser()?.email ?? ''),
  );

  constructor() {
    this.refresh();
  }

  /** Re-fetches applications — called when the bell menu opens so the list doesn't go stale for a long session. */
  refresh(): void {
    this.applicationService.list().subscribe({
      next: (applications) => this._applications.set(applications),
      error: () => this._applications.set([]),
    });
  }
}
