import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { AuthService } from '../auth/auth.service';
import { Reminder, ReminderDismissRequest } from './reminder.models';
import { ReminderService } from './reminder.service';

describe('ReminderService', () => {
  let service: ReminderService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        // Not authenticated: keeps the constructor's polling switchMap on EMPTY, so no
        // background GET interferes with each test's own explicit expectations below.
        { provide: AuthService, useValue: { isAuthenticated: signal(false) } },
      ],
    });
    service = TestBed.inject(ReminderService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('dismiss() POSTs to /reminders/dismiss and then refreshes the feed', () => {
    const request: ReminderDismissRequest = {
      applicationId: 'app-1',
      kind: 'DEADLINE',
      dueDate: '2026-01-01',
    };

    service.dismiss(request).subscribe();

    const dismissReq = httpMock.expectOne(`${environment.apiUrl}/reminders/dismiss`);
    expect(dismissReq.request.method).toBe('POST');
    expect(dismissReq.request.body).toEqual(request);
    dismissReq.flush(null);

    const refreshReq = httpMock.expectOne(`${environment.apiUrl}/reminders`);
    expect(refreshReq.request.method).toBe('GET');
    refreshReq.flush([]);
  });

  it('refresh() GETs /reminders and updates the reminders signal', () => {
    const reminder: Reminder = {
      applicationId: 'app-1',
      jobTitle: 'Backend Engineer',
      companyName: 'Acme',
      kind: 'DEADLINE',
      dueDate: '2026-01-01',
      severity: 'WARNING',
    };

    service.refresh();

    const req = httpMock.expectOne(`${environment.apiUrl}/reminders`);
    req.flush([reminder]);

    expect(service.reminders()).toEqual([reminder]);
  });

  it('refresh() clears reminders on error instead of throwing', () => {
    service.refresh();

    const req = httpMock.expectOne(`${environment.apiUrl}/reminders`);
    req.flush('boom', { status: 500, statusText: 'Server Error' });

    expect(service.reminders()).toEqual([]);
  });
});
