import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { CompanyResponse } from '../companies/company.models';
import { JobResponse } from '../jobs/job.models';
import { CoverLetterResponse, CoverLetterUpdateRequest } from './cover-letter.models';
import { CoverLetterService } from './cover-letter.service';

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
  company: COMPANY,
  owner: OWNER,
  createdAt: '2026-01-01T00:00:00',
  updatedAt: '2026-01-01T00:00:00',
};

const COVER_LETTER: CoverLetterResponse = {
  id: '55555555-5555-5555-5555-555555555555',
  resultText: 'Dear Hiring Team, ...',
  generationRequestId: '66666666-6666-6666-6666-666666666666',
  job: JOB,
  cv: null,
  owner: OWNER,
  createdAt: '2026-01-01T00:00:00',
  updatedAt: '2026-01-01T00:00:00',
};

const UPDATE_REQUEST: CoverLetterUpdateRequest = { resultText: 'Updated text.' };

describe('CoverLetterService', () => {
  let service: CoverLetterService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(CoverLetterService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('list() GETs /cover-letters', () => {
    service.list().subscribe((letters) => expect(letters).toEqual([COVER_LETTER]));
    const req = httpMock.expectOne(`${environment.apiUrl}/cover-letters`);
    expect(req.request.method).toBe('GET');
    req.flush([COVER_LETTER]);
  });

  it('get() GETs /cover-letters/{id}', () => {
    service.get(COVER_LETTER.id).subscribe((letter) => expect(letter).toEqual(COVER_LETTER));
    const req = httpMock.expectOne(`${environment.apiUrl}/cover-letters/${COVER_LETTER.id}`);
    expect(req.request.method).toBe('GET');
    req.flush(COVER_LETTER);
  });

  it('update() PUTs to /cover-letters/{id}', () => {
    service.update(COVER_LETTER.id, UPDATE_REQUEST).subscribe((letter) => expect(letter).toEqual(COVER_LETTER));
    const req = httpMock.expectOne(`${environment.apiUrl}/cover-letters/${COVER_LETTER.id}`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(UPDATE_REQUEST);
    req.flush(COVER_LETTER);
  });
});
