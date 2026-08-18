import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { CvResponse } from './cv.models';
import { CvService } from './cv.service';

const CV: CvResponse = {
  id: '11111111-1111-1111-1111-111111111111',
  title: 'My CV',
  fileName: 'resume.pdf',
  contentType: 'application/pdf',
  size: 1024,
  createdAt: '2026-01-01T00:00:00',
  updatedAt: '2026-01-01T00:00:00',
  owner: { fullName: 'Jane Doe', email: 'jane@example.com', role: 'USER' },
};

describe('CvService', () => {
  let service: CvService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(CvService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('list() GETs /cv', () => {
    service.list().subscribe((cvs) => {
      expect(cvs).toEqual([CV]);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/cv`);
    expect(req.request.method).toBe('GET');
    req.flush([CV]);
  });

  it('upload() POSTs multipart FormData to /cv without a manual Content-Type header', () => {
    const file = new File(['%PDF-1.4'], 'resume.pdf', { type: 'application/pdf' });

    service.upload(file, 'My CV').subscribe((cv) => {
      expect(cv).toEqual(CV);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/cv`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body instanceof FormData).toBe(true);
    expect(req.request.headers.has('Content-Type')).toBe(false);

    const body = req.request.body as FormData;
    expect(body.get('title')).toBe('My CV');
    expect((body.get('file') as File).name).toBe('resume.pdf');

    req.flush(CV);
  });

  it('view() GETs /cv/{id}/view as a blob', () => {
    const blob = new Blob(['%PDF-1.4'], { type: 'application/pdf' });

    service.view(CV.id).subscribe((result) => {
      expect(result).toBe(blob);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/cv/${CV.id}/view`);
    expect(req.request.method).toBe('GET');
    expect(req.request.responseType).toBe('blob');
    req.flush(blob);
  });

  it('download() GETs /cv/{id}/download and reads the filename from Content-Disposition', () => {
    const blob = new Blob(['%PDF-1.4'], { type: 'application/pdf' });

    service.download(CV.id).subscribe((result) => {
      expect(result.blob).toBe(blob);
      expect(result.filename).toBe('My Resume.pdf');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/cv/${CV.id}/download`);
    expect(req.request.method).toBe('GET');
    expect(req.request.responseType).toBe('blob');
    req.flush(blob, {
      headers: { 'Content-Disposition': 'attachment; filename="My Resume.pdf"' },
    });
  });

  it('download() falls back to a default filename when Content-Disposition is missing', () => {
    const blob = new Blob(['%PDF-1.4'], { type: 'application/pdf' });

    service.download(CV.id).subscribe((result) => {
      expect(result.filename).toBe('cv');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/cv/${CV.id}/download`);
    req.flush(blob);
  });

  it('delete() DELETEs /cv/{id}', () => {
    service.delete(CV.id).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/cv/${CV.id}`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
