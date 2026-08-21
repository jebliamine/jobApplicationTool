import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { DashboardResponse } from './dashboard.models';
import { DashboardService } from './dashboard.service';

const DASHBOARD: DashboardResponse = {
  cvCount: 2,
  jobCount: 5,
  applicationCount: 3,
  applicationStatusCounts: {
    APPLIED: 2,
    PHONE_SCREEN: 0,
    INTERVIEWING: 1,
    OFFER: 0,
    REJECTED: 0,
    WITHDRAWN: 0,
    ACCEPTED: 0,
  },
  applicationsByDay: { '2026-01-01': 2, '2026-01-05': 1 },
  activeCoverLetterCount: 1,
  archivedCoverLetterCount: 0,
  generationRequestCount: 4,
  generationStatusCounts: { PENDING: 0, IN_PROGRESS: 0, COMPLETED: 3, FAILED: 1 },
  totalUsers: null,
};

describe('DashboardService', () => {
  let service: DashboardService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(DashboardService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('get() GETs /dashboard', () => {
    service.get().subscribe((dashboard) => expect(dashboard).toEqual(DASHBOARD));
    const req = httpMock.expectOne(`${environment.apiUrl}/dashboard`);
    expect(req.request.method).toBe('GET');
    req.flush(DASHBOARD);
  });
});
