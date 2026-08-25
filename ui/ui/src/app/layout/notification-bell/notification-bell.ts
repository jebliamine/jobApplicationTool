import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatBadgeModule } from '@angular/material/badge';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { LucideBell, LucideCalendarClock, LucideCheck, LucideClock, LucideMessageCircle, LucideTarget } from '@lucide/angular';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { ReminderService } from '../../core/reminders/reminder.service';
import { Reminder, ReminderKind, ReminderSeverity } from '../../core/reminders/reminder.models';
import { StatusBadge, StatusBadgeSeverity } from '../../shared/components/status-badge/status-badge';

const SEVERITY_BADGE: Record<ReminderSeverity, StatusBadgeSeverity> = {
  ERROR: 'error',
  WARNING: 'warning',
  INFO: 'info',
};

const SNOOZE_DAYS = 1;

@Component({
  selector: 'app-notification-bell',
  imports: [
    RouterLink,
    MatBadgeModule,
    MatButtonModule,
    MatMenuModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    TranslatePipe,
    LucideBell,
    LucideCalendarClock,
    LucideCheck,
    LucideClock,
    LucideMessageCircle,
    LucideTarget,
    StatusBadge,
  ],
  templateUrl: './notification-bell.html',
  styleUrl: './notification-bell.scss',
})
export class NotificationBell {
  private readonly reminderService = inject(ReminderService);
  private readonly translate = inject(TranslateService);

  protected readonly reminders = this.reminderService.reminders;
  protected readonly count = computed(() => this.reminders().length);
  protected readonly pendingKey = signal<string | null>(null);

  protected onMenuOpened(): void {
    this.reminderService.refresh();
  }

  protected kindLabel(kind: ReminderKind): string {
    return this.translate.instant(`notifications.kinds.${kind}`);
  }

  protected severityBadge(severity: ReminderSeverity): StatusBadgeSeverity {
    return SEVERITY_BADGE[severity];
  }

  protected reminderKey(reminder: Reminder): string {
    return `${reminder.applicationId}:${reminder.kind}:${reminder.dueDate}`;
  }

  protected dueLabel(reminder: Reminder): string {
    const days = daysBetween(reminder.dueDate, new Date());
    if (days < 0) {
      return this.translate.instant('notifications.overdue', { count: Math.abs(days) });
    }
    if (days === 0) {
      return this.translate.instant('notifications.dueToday');
    }
    if (days === 1) {
      return this.translate.instant('notifications.dueTomorrow');
    }
    return this.translate.instant('notifications.dueInDays', { count: days });
  }

  /** Permanently dismisses this exact reminder instance (see ReminderService#dismiss on the backend). */
  protected dismiss(reminder: Reminder, event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    this.runDismiss(reminder, null);
  }

  /** Hides this reminder until it resurfaces on its own tomorrow. */
  protected snooze(reminder: Reminder, event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    const snoozedUntil = addDays(new Date(), SNOOZE_DAYS).toISOString().slice(0, 10);
    this.runDismiss(reminder, snoozedUntil);
  }

  private runDismiss(reminder: Reminder, snoozedUntil: string | null): void {
    const key = this.reminderKey(reminder);
    if (this.pendingKey()) {
      return;
    }
    this.pendingKey.set(key);
    this.reminderService
      .dismiss({
        applicationId: reminder.applicationId,
        kind: reminder.kind,
        dueDate: reminder.dueDate,
        snoozedUntil,
      })
      .subscribe({
        next: () => this.pendingKey.set(null),
        error: () => this.pendingKey.set(null),
      });
  }
}

function daysBetween(isoDate: string, from: Date): number {
  const fromIso = from.toISOString().slice(0, 10);
  const msPerDay = 24 * 60 * 60 * 1000;
  return Math.round((Date.parse(isoDate) - Date.parse(fromIso)) / msPerDay);
}

function addDays(date: Date, days: number): Date {
  const result = new Date(date);
  result.setDate(result.getDate() + days);
  return result;
}
