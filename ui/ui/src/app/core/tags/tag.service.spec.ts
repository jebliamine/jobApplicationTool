import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { TagRequest, TagResponse } from './tag.models';
import { TagService } from './tag.service';

const TAG: TagResponse = { id: '11111111-1111-1111-1111-111111111111', name: 'Remote' };
const REQUEST: TagRequest = { name: 'Remote' };

describe('TagService', () => {
  let service: TagService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(TagService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('list() GETs /tags', () => {
    service.list().subscribe((tags) => expect(tags).toEqual([TAG]));

    const req = httpMock.expectOne(`${environment.apiUrl}/tags`);
    expect(req.request.method).toBe('GET');
    req.flush([TAG]);
  });

  it('create() POSTs to /tags', () => {
    service.create(REQUEST).subscribe((tag) => expect(tag).toEqual(TAG));

    const req = httpMock.expectOne(`${environment.apiUrl}/tags`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(REQUEST);
    req.flush(TAG);
  });

  it('rename() PUTs to /tags/{id}', () => {
    service.rename(TAG.id, REQUEST).subscribe((tag) => expect(tag).toEqual(TAG));

    const req = httpMock.expectOne(`${environment.apiUrl}/tags/${TAG.id}`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(REQUEST);
    req.flush(TAG);
  });

  it('delete() DELETEs /tags/{id}', () => {
    service.delete(TAG.id).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/tags/${TAG.id}`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
