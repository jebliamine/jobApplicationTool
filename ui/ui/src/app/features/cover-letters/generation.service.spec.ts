import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { CompanyResponse } from '../companies/company.models';
import { JobResponse } from '../jobs/job.models';
import { GenerationRequestCreateRequest, GenerationRequestResponse } from './generation.models';
import { GenerationService } from './generation.service';

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

const GENERATION_REQUEST: GenerationRequestResponse = {
  id: '66666666-6666-6666-6666-666666666666',
  job: JOB,
  cv: null,
  status: 'COMPLETED',
  provider: 'placeholder',
  model: 'deterministic-v1',
  errorMessage: null,
  coverLetter: null,
  owner: OWNER,
  createdAt: '2026-01-01T00:00:00',
  startedAt: '2026-01-01T00:00:00',
  completedAt: '2026-01-01T00:00:01',
};

const CREATE_REQUEST: GenerationRequestCreateRequest = {
  jobId: JOB.id,
  cvDocumentId: '44444444-4444-4444-4444-444444444444',
};

describe('GenerationService', () => {
  let service: GenerationService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(GenerationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('list() GETs /generation-requests', () => {
    service.list().subscribe((requests) => expect(requests).toEqual([GENERATION_REQUEST]));
    const req = httpMock.expectOne(`${environment.apiUrl}/generation-requests`);
    expect(req.request.method).toBe('GET');
    req.flush([GENERATION_REQUEST]);
  });

  it('get() GETs /generation-requests/{id}', () => {
    service.get(GENERATION_REQUEST.id).subscribe((request) => expect(request).toEqual(GENERATION_REQUEST));
    const req = httpMock.expectOne(`${environment.apiUrl}/generation-requests/${GENERATION_REQUEST.id}`);
    expect(req.request.method).toBe('GET');
    req.flush(GENERATION_REQUEST);
  });

  it('create() POSTs to /generation-requests', () => {
    service.create(CREATE_REQUEST).subscribe((request) => expect(request).toEqual(GENERATION_REQUEST));
    const req = httpMock.expectOne(`${environment.apiUrl}/generation-requests`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(CREATE_REQUEST);
    req.flush(GENERATION_REQUEST);
  });
});
