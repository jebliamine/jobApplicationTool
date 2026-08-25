import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { SearchResultResponse } from './search.models';
import { SearchService } from './search.service';

const RESULT: SearchResultResponse = {
  type: 'JOB',
  id: '11111111-1111-1111-1111-111111111111',
  title: 'Backend Engineer',
  subtitle: 'Acme Corp',
};

describe('SearchService', () => {
  let service: SearchService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(SearchService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('search() GETs /search with the query as a param', () => {
    service.search('backend').subscribe((results) => expect(results).toEqual([RESULT]));

    const req = httpMock.expectOne((request) => request.url === `${environment.apiUrl}/search`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('q')).toBe('backend');
    req.flush([RESULT]);
  });
});
