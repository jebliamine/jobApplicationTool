import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatBadgeModule } from '@angular/material/badge';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { LucideBell, LucideCalendarClock, LucideMessageCircle, LucideTarget } from '@lucide/angular';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { ReminderService } from '../../core/reminders/reminder.service';
import { Reminder, ReminderKind } from '../../core/reminders/reminder.models';
import { StatusBadge } from '../../shared/components/status-badge/status-badge';

@Component({
  selector: 'app-notification-bell',
  imports: [
    RouterLink,
    MatBadgeModule,
    MatButtonModule,
    MatMenuModule,
    MatTooltipModule,
    TranslatePipe,
    LucideBell,
    LucideCalendarClock,
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

  protected onMenuOpened(): void {
    this.reminderService.refresh();
  }

  protected kindLabel(kind: ReminderKind): string {
    return this.translate.instant(`notifications.kinds.${kind}`);
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
}

function daysBetween(isoDate: string, from: Date): number {
  const fromIso = from.toISOString().slice(0, 10);
  const msPerDay = 24 * 60 * 60 * 1000;
  return Math.round((Date.parse(isoDate) - Date.parse(fromIso)) / msPerDay);
}
