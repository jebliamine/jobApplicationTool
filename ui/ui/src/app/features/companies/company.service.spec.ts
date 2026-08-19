import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { CompanyRequest, CompanyResponse } from './company.models';
import { CompanyService } from './company.service';

const COMPANY: CompanyResponse = {
  id: '11111111-1111-1111-1111-111111111111',
  name: 'Acme',
  website: null,
  location: null,
  notes: null,
  owner: { fullName: 'Jane Doe', email: 'jane@example.com', role: 'USER' },
  createdAt: '2026-01-01T00:00:00',
  updatedAt: '2026-01-01T00:00:00',
};

const REQUEST: CompanyRequest = { name: 'Acme', website: null, location: null, notes: null };

describe('CompanyService', () => {
  let service: CompanyService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(CompanyService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('list() GETs /companies', () => {
    service.list().subscribe((companies) => {
      expect(companies).toEqual([COMPANY]);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/companies`);
    expect(req.request.method).toBe('GET');
    req.flush([COMPANY]);
  });

  it('get() GETs /companies/{id}', () => {
    service.get(COMPANY.id).subscribe((company) => expect(company).toEqual(COMPANY));
    const req = httpMock.expectOne(`${environment.apiUrl}/companies/${COMPANY.id}`);
    expect(req.request.method).toBe('GET');
    req.flush(COMPANY);
  });

  it('create() POSTs to /companies', () => {
    service.create(REQUEST).subscribe((company) => {
      expect(company).toEqual(COMPANY);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/companies`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(REQUEST);
    req.flush(COMPANY);
  });

  it('update() PUTs to /companies/{id}', () => {
    service.update(COMPANY.id, REQUEST).subscribe((company) => expect(company).toEqual(COMPANY));
    const req = httpMock.expectOne(`${environment.apiUrl}/companies/${COMPANY.id}`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(REQUEST);
    req.flush(COMPANY);
  });

  it('delete() DELETEs /companies/{id}', () => {
    service.delete(COMPANY.id).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/companies/${COMPANY.id}`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
