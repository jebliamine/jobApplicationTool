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
  salaryRange: null,
  company: COMPANY,
  owner: OWNER,
  createdAt: '2026-01-01T00:00:00',
  updatedAt: '2026-01-01T00:00:00',
  tags: [],
};

const COVER_LETTER: CoverLetterResponse = {
  id: '55555555-5555-5555-5555-555555555555',
  resultText: 'Dear Hiring Team, ...',
  generationRequestId: '66666666-6666-6666-6666-666666666666',
  job: JOB,
  cv: null,
  owner: OWNER,
  archived: false,
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

  it('list() GETs /cover-letters?archived=false by default', () => {
    service.list().subscribe((letters) => expect(letters).toEqual([COVER_LETTER]));
    const req = httpMock.expectOne(`${environment.apiUrl}/cover-letters?archived=false`);
    expect(req.request.method).toBe('GET');
    req.flush([COVER_LETTER]);
  });

  it('list(true) GETs /cover-letters?archived=true', () => {
    service.list(true).subscribe((letters) => expect(letters).toEqual([COVER_LETTER]));
    const req = httpMock.expectOne(`${environment.apiUrl}/cover-letters?archived=true`);
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

  it('archive() PATCHes /cover-letters/{id}/archive', () => {
    const archived = { ...COVER_LETTER, archived: true };
    service.archive(COVER_LETTER.id).subscribe((letter) => expect(letter).toEqual(archived));
    const req = httpMock.expectOne(`${environment.apiUrl}/cover-letters/${COVER_LETTER.id}/archive`);
    expect(req.request.method).toBe('PATCH');
    req.flush(archived);
  });

  it('unarchive() PATCHes /cover-letters/{id}/unarchive', () => {
    service.unarchive(COVER_LETTER.id).subscribe((letter) => expect(letter).toEqual(COVER_LETTER));
    const req = httpMock.expectOne(`${environment.apiUrl}/cover-letters/${COVER_LETTER.id}/unarchive`);
    expect(req.request.method).toBe('PATCH');
    req.flush(COVER_LETTER);
  });

  it('delete() DELETEs /cover-letters/{id}', () => {
    service.delete(COVER_LETTER.id).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/cover-letters/${COVER_LETTER.id}`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
