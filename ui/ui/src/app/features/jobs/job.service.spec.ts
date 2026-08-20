import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { CompanyResponse } from '../companies/company.models';
import { JobRequest, JobResponse } from './job.models';
import { JobService } from './job.service';

const COMPANY: CompanyResponse = {
  id: '22222222-2222-2222-2222-222222222222',
  name: 'Acme',
  website: null,
  location: null,
  notes: null,
  owner: { fullName: 'Jane Doe', email: 'jane@example.com', role: 'USER' },
  createdAt: '2026-01-01T00:00:00',
  updatedAt: '2026-01-01T00:00:00',
};

const JOB: JobResponse = {
  id: '11111111-1111-1111-1111-111111111111',
  title: 'Backend Engineer',
  description: 'Build things.',
  location: 'Berlin',
  employmentType: 'FULL_TIME',
  workMode: 'REMOTE',
  url: null,
  source: null,
  salaryRange: null,
  company: COMPANY,
  owner: { fullName: 'Jane Doe', email: 'jane@example.com', role: 'USER' },
  createdAt: '2026-01-01T00:00:00',
  updatedAt: '2026-01-01T00:00:00',
};

const REQUEST: JobRequest = {
  companyId: COMPANY.id,
  title: 'Backend Engineer',
  description: 'Build things.',
  location: 'Berlin',
  employmentType: 'FULL_TIME',
  workMode: 'REMOTE',
  url: null,
  source: null,
  salaryRange: null,
};

describe('JobService', () => {
  let service: JobService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(JobService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('list() GETs /jobs', () => {
    service.list().subscribe((jobs) => expect(jobs).toEqual([JOB]));
    const req = httpMock.expectOne(`${environment.apiUrl}/jobs`);
    expect(req.request.method).toBe('GET');
    req.flush([JOB]);
  });

  it('get() GETs /jobs/{id}', () => {
    service.get(JOB.id).subscribe((job) => expect(job).toEqual(JOB));
    const req = httpMock.expectOne(`${environment.apiUrl}/jobs/${JOB.id}`);
    expect(req.request.method).toBe('GET');
    req.flush(JOB);
  });

  it('create() POSTs to /jobs', () => {
    service.create(REQUEST).subscribe((job) => expect(job).toEqual(JOB));
    const req = httpMock.expectOne(`${environment.apiUrl}/jobs`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(REQUEST);
    req.flush(JOB);
  });

  it('update() PUTs to /jobs/{id}', () => {
    service.update(JOB.id, REQUEST).subscribe((job) => expect(job).toEqual(JOB));
    const req = httpMock.expectOne(`${environment.apiUrl}/jobs/${JOB.id}`);
    expect(req.request.method).toBe('PUT');
    req.flush(JOB);
  });

  it('delete() DELETEs /jobs/{id}', () => {
    service.delete(JOB.id).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/jobs/${JOB.id}`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
