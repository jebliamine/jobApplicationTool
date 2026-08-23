import { ApplicationResponse, ApplicationStatus } from '../../features/applications/application.models';
import { buildReminders } from './reminder.models';

const TODAY = new Date('2026-01-15T12:00:00Z');
const ME = 'me@example.com';
const OTHER = 'other@example.com';

function application(overrides: Partial<ApplicationResponse> & { status?: ApplicationStatus }): ApplicationResponse {
  return {
    id: 'app-1',
    job: { id: 'job-1', title: 'Backend Engineer', company: { id: 'co-1', name: 'Acme' } } as never,
    cv: null,
    coverLetter: null,
    status: 'APPLIED',
    appliedAt: '2026-01-01',
    deadline: null,
    followUpDate: null,
    interviewDate: null,
    contactPerson: null,
    notes: null,
    owner: { fullName: 'Me', email: ME, role: 'USER' },
    createdAt: '2026-01-01T00:00:00',
    updatedAt: '2026-01-01T00:00:00',
    ...overrides,
  };
}

describe('buildReminders', () => {
  it('marks a past date as overdue (error severity)', () => {
    const reminders = buildReminders([application({ deadline: '2026-01-10' })], ME, TODAY);
    expect(reminders).toEqual([
      expect.objectContaining({ kind: 'DEADLINE', dueDate: '2026-01-10', severity: 'error' }),
    ]);
  });

  it('marks today and tomorrow as due-soon (warning severity)', () => {
    const reminders = buildReminders(
      [application({ id: 'a', followUpDate: '2026-01-15' }), application({ id: 'b', followUpDate: '2026-01-16' })],
      ME,
      TODAY,
    );
    expect(reminders.map((r) => r.severity)).toEqual(['warning', 'warning']);
  });

  it('marks a date further out within the window as info severity', () => {
    const reminders = buildReminders([application({ interviewDate: '2026-01-20' })], ME, TODAY);
    expect(reminders[0].severity).toBe('info');
  });

  it('excludes dates beyond the 7-day window', () => {
    const reminders = buildReminders([application({ deadline: '2026-01-23' })], ME, TODAY);
    expect(reminders).toEqual([]);
  });

  it('excludes applications owned by another user', () => {
    const reminders = buildReminders(
      [application({ owner: { fullName: 'Other', email: OTHER, role: 'USER' }, deadline: '2026-01-15' })],
      ME,
      TODAY,
    );
    expect(reminders).toEqual([]);
  });

  it('excludes closed statuses (REJECTED, WITHDRAWN, ACCEPTED)', () => {
    const closed: ApplicationStatus[] = ['REJECTED', 'WITHDRAWN', 'ACCEPTED'];
    const reminders = buildReminders(
      closed.map((status) => application({ status, deadline: '2026-01-15' })),
      ME,
      TODAY,
    );
    expect(reminders).toEqual([]);
  });

  it('produces one reminder per set date field on the same application', () => {
    const reminders = buildReminders(
      [application({ deadline: '2026-01-16', followUpDate: '2026-01-17', interviewDate: '2026-01-18' })],
      ME,
      TODAY,
    );
    expect(reminders.map((r) => r.kind)).toEqual(['DEADLINE', 'FOLLOW_UP', 'INTERVIEW']);
  });

  it('sorts reminders ascending by due date across applications', () => {
    const reminders = buildReminders(
      [application({ id: 'later', deadline: '2026-01-19' }), application({ id: 'sooner', deadline: '2026-01-15' })],
      ME,
      TODAY,
    );
    expect(reminders.map((r) => r.applicationId)).toEqual(['sooner', 'later']);
  });
});
