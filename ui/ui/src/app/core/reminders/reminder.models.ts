import { ApplicationResponse, ApplicationStatus } from '../../features/applications/application.models';

export type ReminderKind = 'DEADLINE' | 'FOLLOW_UP' | 'INTERVIEW';
export type ReminderSeverity = 'error' | 'warning' | 'info';

export interface Reminder {
  applicationId: string;
  jobTitle: string;
  companyName: string;
  kind: ReminderKind;
  dueDate: string;
  severity: ReminderSeverity;
}

/** Closed applications have no actionable reminder. */
const ACTIVE_STATUSES: ApplicationStatus[] = ['APPLIED', 'PHONE_SCREEN', 'INTERVIEWING', 'OFFER'];

const REMINDER_WINDOW_DAYS = 7;

const DATE_FIELDS: { field: 'deadline' | 'followUpDate' | 'interviewDate'; kind: ReminderKind }[] = [
  { field: 'deadline', kind: 'DEADLINE' },
  { field: 'followUpDate', kind: 'FOLLOW_UP' },
  { field: 'interviewDate', kind: 'INTERVIEW' },
];

/**
 * Pure client-side classification over data the backend already returns —
 * same approach as application-status.ts's ApplicationStatus -> severity
 * mapping, no backend "reminder" concept exists or is needed.
 */
export function buildReminders(
  applications: ApplicationResponse[],
  currentUserEmail: string,
  today: Date = new Date(),
): Reminder[] {
  const todayIso = toIsoDate(today);
  const horizonIso = toIsoDate(addDays(today, REMINDER_WINDOW_DAYS));

  const reminders: Reminder[] = [];

  for (const application of applications) {
    if (application.owner.email !== currentUserEmail) {
      continue;
    }
    if (!ACTIVE_STATUSES.includes(application.status)) {
      continue;
    }

    for (const { field, kind } of DATE_FIELDS) {
      const dueDate = application[field];
      if (!dueDate || dueDate > horizonIso) {
        continue;
      }

      reminders.push({
        applicationId: application.id,
        jobTitle: application.job.title,
        companyName: application.job.company.name,
        kind,
        dueDate,
        severity: severityFor(dueDate, todayIso),
      });
    }
  }

  return reminders.sort((a, b) => a.dueDate.localeCompare(b.dueDate));
}

function severityFor(dueDate: string, todayIso: string): ReminderSeverity {
  if (dueDate < todayIso) {
    return 'error';
  }
  if (dueDate === todayIso || dueDate === toIsoDate(addDays(new Date(todayIso), 1))) {
    return 'warning';
  }
  return 'info';
}

function addDays(date: Date, days: number): Date {
  const result = new Date(date);
  result.setDate(result.getDate() + days);
  return result;
}

function toIsoDate(date: Date): string {
  return date.toISOString().slice(0, 10);
}
