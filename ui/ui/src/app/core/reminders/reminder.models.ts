export type ReminderKind = 'DEADLINE' | 'FOLLOW_UP' | 'INTERVIEW';
export type ReminderSeverity = 'ERROR' | 'WARNING' | 'INFO';

/** Mirrors the response body of GET /api/v1/reminders — always scoped to the caller's own applications. */
export interface Reminder {
  applicationId: string;
  jobTitle: string;
  companyName: string;
  kind: ReminderKind;
  dueDate: string;
  severity: ReminderSeverity;
}

/**
 * Request body for POST /api/v1/reminders/dismiss. Omit/null snoozedUntil to dismiss
 * permanently for this exact due date; set it to resurface the reminder once that date arrives.
 */
export interface ReminderDismissRequest {
  applicationId: string;
  kind: ReminderKind;
  dueDate: string;
  snoozedUntil?: string | null;
}
