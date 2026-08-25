import { ComponentFixture, TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideTranslateService } from '@ngx-translate/core';
import { Subject, of } from 'rxjs';
import { Reminder } from '../../core/reminders/reminder.models';
import { ReminderService } from '../../core/reminders/reminder.service';
import { NotificationBell } from './notification-bell';

const REMINDER: Reminder = {
  applicationId: 'app-1',
  jobTitle: 'Backend Engineer',
  companyName: 'Acme',
  kind: 'DEADLINE',
  dueDate: '2026-01-01',
  severity: 'WARNING',
};

describe('NotificationBell', () => {
  let fixture: ComponentFixture<NotificationBell>;
  let component: NotificationBell;
  let dismissSpy: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    dismissSpy = vi.fn().mockReturnValue(of(undefined));

    await TestBed.configureTestingModule({
      imports: [NotificationBell],
      providers: [
        provideRouter([]),
        provideTranslateService({ lang: 'en', fallbackLang: 'en' }),
        {
          provide: ReminderService,
          useValue: {
            reminders: signal([REMINDER]),
            refresh: vi.fn(),
            dismiss: dismissSpy,
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(NotificationBell);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('maps backend severity to the status-badge severity scale', () => {
    expect(component['severityBadge']('ERROR')).toBe('error');
    expect(component['severityBadge']('WARNING')).toBe('warning');
    expect(component['severityBadge']('INFO')).toBe('info');
  });

  it('dismiss() calls the service with a null snoozedUntil and stops the click from navigating', () => {
    const event = new MouseEvent('click');
    const stopSpy = vi.spyOn(event, 'stopPropagation');
    const preventSpy = vi.spyOn(event, 'preventDefault');

    component['dismiss'](REMINDER, event);

    expect(stopSpy).toHaveBeenCalled();
    expect(preventSpy).toHaveBeenCalled();
    expect(dismissSpy).toHaveBeenCalledWith({
      applicationId: REMINDER.applicationId,
      kind: REMINDER.kind,
      dueDate: REMINDER.dueDate,
      snoozedUntil: null,
    });
  });

  it('snooze() calls the service with a snoozedUntil date in the future', () => {
    component['snooze'](REMINDER, new MouseEvent('click'));

    expect(dismissSpy).toHaveBeenCalledTimes(1);
    const request = dismissSpy.mock.calls[0][0];
    expect(request.snoozedUntil).not.toBeNull();
    expect(new Date(request.snoozedUntil).getTime()).toBeGreaterThan(Date.now());
  });

  it('ignores a second dismiss/snooze while one is already pending', () => {
    dismissSpy.mockReturnValue(new Subject());

    component['dismiss'](REMINDER, new MouseEvent('click'));
    component['snooze'](REMINDER, new MouseEvent('click'));

    expect(dismissSpy).toHaveBeenCalledTimes(1);
  });
});
