import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { CompanyResponse } from '../companies/company.models';
import { CvResponse } from '../cv/cv.models';
import { JobResponse } from '../jobs/job.models';
import { ApplicationRequest, ApplicationResponse } from './application.models';
import { ApplicationService } from './application.service';

const OWNER = { fullName: 'Jane Doe', email: 'jane@example.com', role: 'USER' as const };

const COMPANY: CompanyResponse = {
  id: '22222222-2222-2222-2222-222222222222',
  name: 'Acme',
  website: null,
  location: null,
  notes: null,
  owner: OWNER,
  createdAt: '2026-01-01T00:00:00',
  updatedAt: '2026-01-01T00:00:00',
};

const JOB: JobResponse = {
  id: '33333333-3333-3333-3333-333333333333',
  title: 'Backend Engineer',
  description: 'Build things.',
  location: 'Berlin',
  employmentType: 'FULL_TIME',
  workMode: 'REMOTE',
  url: null,
  source: null,
  salaryRange: null,
  company: COMPANY,
  owner: OWNER,
  createdAt: '2026-01-01T00:00:00',
  updatedAt: '2026-01-01T00:00:00',
};

const CV: CvResponse = {
  id: '44444444-4444-4444-4444-444444444444',
  title: 'My Resume',
  fileName: 'resume.pdf',
  contentType: 'application/pdf',
  size: 1024,
  createdAt: '2026-01-01T00:00:00',
  updatedAt: '2026-01-01T00:00:00',
  owner: OWNER,
};

const APPLICATION: ApplicationResponse = {
  id: '11111111-1111-1111-1111-111111111111',
  job: JOB,
  cv: CV,
  coverLetter: null,
  status: 'APPLIED',
  appliedAt: '2026-01-01',
  deadline: null,
  followUpDate: null,
  interviewDate: null,
  contactPerson: null,
  notes: 'Applied via referral.',
  owner: OWNER,
  createdAt: '2026-01-01T00:00:00',
  updatedAt: '2026-01-01T00:00:00',
};

const REQUEST: ApplicationRequest = {
  jobId: JOB.id,
  cvDocumentId: CV.id,
  coverLetterId: null,
  status: 'APPLIED',
  appliedAt: '2026-01-01',
  deadline: null,
  followUpDate: null,
  interviewDate: null,
  contactPerson: null,
  notes: 'Applied via referral.',
};

describe('ApplicationService', () => {
  let service: ApplicationService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ApplicationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('list() GETs /applications', () => {
    service.list().subscribe((applications) => expect(applications).toEqual([APPLICATION]));
    const req = httpMock.expectOne(`${environment.apiUrl}/applications`);
    expect(req.request.method).toBe('GET');
    req.flush([APPLICATION]);
  });

  it('get() GETs /applications/{id}', () => {
    service.get(APPLICATION.id).subscribe((application) => expect(application).toEqual(APPLICATION));
    const req = httpMock.expectOne(`${environment.apiUrl}/applications/${APPLICATION.id}`);
    expect(req.request.method).toBe('GET');
    req.flush(APPLICATION);
  });

  it('create() POSTs to /applications', () => {
    service.create(REQUEST).subscribe((application) => expect(application).toEqual(APPLICATION));
    const req = httpMock.expectOne(`${environment.apiUrl}/applications`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(REQUEST);
    req.flush(APPLICATION);
  });

  it('update() PUTs to /applications/{id}', () => {
    service.update(APPLICATION.id, REQUEST).subscribe((application) => expect(application).toEqual(APPLICATION));
    const req = httpMock.expectOne(`${environment.apiUrl}/applications/${APPLICATION.id}`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(REQUEST);
    req.flush(APPLICATION);
  });

  it('delete() DELETEs /applications/{id}', () => {
    service.delete(APPLICATION.id).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/applications/${APPLICATION.id}`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
