import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../../environments/environment';
import {
  AdminAiProviderResponse,
  AiProviderCreateRequest,
  AiProviderTestResult,
  AiProviderUpdateRequest,
} from './ai-provider.models';
import { AdminAiProviderService } from './ai-provider.service';

const GEMINI_ID = '22222222-2222-2222-2222-222222222222';

const GEMINI: AdminAiProviderResponse = {
  id: GEMINI_ID,
  adapterType: 'GEMINI_GENERATE_CONTENT',
  displayName: 'Google Gemini',
  enabled: true,
  hasApiKey: true,
  defaultModel: 'gemini-2.0-flash',
  baseUrl: null,
};

describe('AdminAiProviderService', () => {
  let service: AdminAiProviderService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AdminAiProviderService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('list() GETs /admin/ai/providers', () => {
    service.list().subscribe((providers) => expect(providers).toEqual([GEMINI]));
    const req = httpMock.expectOne(`${environment.apiUrl}/admin/ai/providers`);
    expect(req.request.method).toBe('GET');
    req.flush([GEMINI]);
  });

  it('create() POSTs to /admin/ai/providers', () => {
    const request: AiProviderCreateRequest = {
      adapterType: 'OPENAI_COMPATIBLE',
      displayName: 'My Ollama Server',
    };
    service.create(request).subscribe((provider) => expect(provider).toEqual(GEMINI));
    const req = httpMock.expectOne(`${environment.apiUrl}/admin/ai/providers`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(GEMINI);
  });

  it('update() PUTs to /admin/ai/providers/{id}', () => {
    const request: AiProviderUpdateRequest = { enabled: true, apiKey: 'new-key' };
    service.update(GEMINI_ID, request).subscribe((provider) => expect(provider).toEqual(GEMINI));
    const req = httpMock.expectOne(`${environment.apiUrl}/admin/ai/providers/${GEMINI_ID}`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(request);
    req.flush(GEMINI);
  });

  it('update() with clearApiKey sends clearApiKey and no apiKey field set', () => {
    const request: AiProviderUpdateRequest = { clearApiKey: true };
    service.update(GEMINI_ID, request).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/admin/ai/providers/${GEMINI_ID}`);
    expect(req.request.body).toEqual({ clearApiKey: true });
    req.flush({ ...GEMINI, hasApiKey: false });
  });

  it('delete() DELETEs /admin/ai/providers/{id}', () => {
    service.delete(GEMINI_ID).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/admin/ai/providers/${GEMINI_ID}`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('test() POSTs to /admin/ai/providers/{id}/test', () => {
    const result: AiProviderTestResult = { success: true, message: 'Connection successful.' };
    service.test(GEMINI_ID).subscribe((response) => expect(response).toEqual(result));
    const req = httpMock.expectOne(`${environment.apiUrl}/admin/ai/providers/${GEMINI_ID}/test`);
    expect(req.request.method).toBe('POST');
    req.flush(result);
  });
});
