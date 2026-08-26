import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTranslateService } from '@ngx-translate/core';
import { of } from 'rxjs';
import { UserService } from '../../core/user/user.service';
import { ApplicationResponse } from '../applications/application.models';
import { ApplicationService } from '../applications/application.service';
import { Dashboard } from './dashboard';
import { DashboardResponse } from './dashboard.models';
import { DashboardService } from './dashboard.service';

const EMPTY_DASHBOARD: DashboardResponse = {
  cvCount: 0,
  jobCount: 0,
  applicationCount: 0,
  applicationStatusCounts: {
    APPLIED: 0,
    PHONE_SCREEN: 0,
    INTERVIEWING: 0,
    OFFER: 0,
    REJECTED: 0,
    WITHDRAWN: 0,
    ACCEPTED: 0,
  },
  applicationsByDay: {},
  activeCoverLetterCount: 0,
  archivedCoverLetterCount: 0,
  generationRequestCount: 0,
  generationStatusCounts: { PENDING: 0, IN_PROGRESS: 0, COMPLETED: 0, FAILED: 0 },
  totalUsers: null,
  funnelMetrics: {
    totalApplications: 0,
    responseRate: 0,
    offerRate: 0,
    averageDaysInCurrentStatus: {},
    byCompany: [],
  },
};

const ACTIVE_DASHBOARD: DashboardResponse = {
  ...EMPTY_DASHBOARD,
  cvCount: 1,
  jobCount: 3,
  applicationCount: 2,
  funnelMetrics: {
    totalApplications: 2,
    responseRate: 0.5,
    offerRate: 0,
    averageDaysInCurrentStatus: { APPLIED: 3 },
    byCompany: [{ companyName: 'Acme', applications: 2, responseRate: 0.5, offerRate: 0 }],
  },
};

const APPLICATION: ApplicationResponse = {
  id: '11111111-1111-1111-1111-111111111111',
  job: { id: 'job-1', title: 'Backend Engineer', company: { id: 'c-1', name: 'Acme' } } as any,
  cv: null,
  coverLetter: null,
  status: 'APPLIED',
  appliedAt: '2026-01-01T00:00:00',
  deadline: null,
  followUpDate: null,
  contactPerson: null,
  notes: null,
  owner: { fullName: 'Jane Doe', email: 'jane@example.com', role: 'USER' },
  createdAt: '2026-01-01T00:00:00',
  updatedAt: '2026-01-01T00:00:00',
  tags: [],
  interviewStages: [],
};

describe('Dashboard', () => {
  let fixture: ComponentFixture<Dashboard>;

  function setup(dashboardResponse: DashboardResponse, applications: ApplicationResponse[] = []) {
    TestBed.configureTestingModule({
      imports: [Dashboard],
      providers: [
        provideRouter([]),
        provideTranslateService({ lang: 'en', fallbackLang: 'en' }),
        { provide: DashboardService, useValue: { get: () => of(dashboardResponse) } },
        { provide: ApplicationService, useValue: { list: () => of(applications) } },
        { provide: UserService, useValue: { currentUser: () => ({ fullName: 'Jane Doe', email: 'jane@example.com', role: 'USER' }) } },
      ],
    });

    fixture = TestBed.createComponent(Dashboard);
    fixture.detectChanges();
  }

  it('shows the getting-started guide when the caller has no data anywhere', () => {
    setup(EMPTY_DASHBOARD);

    expect(fixture.componentInstance['isNewUser']()).toBe(true);
    expect(fixture.nativeElement.querySelector('app-workflow-guide')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('app-stat-card')).toBeNull();
  });

  it('shows the stats/charts/recent-applications view once the caller has real data', () => {
    setup(ACTIVE_DASHBOARD, [APPLICATION]);

    expect(fixture.componentInstance['isNewUser']()).toBe(false);
    expect(fixture.nativeElement.querySelector('app-workflow-guide')).toBeNull();
    expect(fixture.nativeElement.querySelector('app-stat-card')).not.toBeNull();
    expect(fixture.componentInstance['recentApplications']()).toEqual([APPLICATION]);
  });

  it('greets the user by their first name only', () => {
    setup(ACTIVE_DASHBOARD);

    expect(fixture.componentInstance['firstName']()).toBe('Jane');
  });

  it('renders response rate and offer rate as rounded percentages', () => {
    setup(ACTIVE_DASHBOARD, [APPLICATION]);

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('50%');
    expect(text).toContain('0%');
  });

  it('shows the by-company breakdown table when there is data', () => {
    setup(ACTIVE_DASHBOARD, [APPLICATION]);

    expect(fixture.nativeElement.textContent).toContain('Acme');
    expect(fixture.nativeElement.querySelector('.dashboard__funnel-table')).not.toBeNull();
  });

  it('hides the by-company table when there is no breakdown data', () => {
    setup({ ...ACTIVE_DASHBOARD, funnelMetrics: { ...ACTIVE_DASHBOARD.funnelMetrics, byCompany: [] } }, [APPLICATION]);

    expect(fixture.nativeElement.querySelector('.dashboard__funnel-table')).toBeNull();
  });
});
