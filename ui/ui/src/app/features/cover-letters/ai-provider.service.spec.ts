import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { AiProviderResponse } from './ai-provider.models';
import { AiProviderService } from './ai-provider.service';

const PROVIDERS: AiProviderResponse[] = [
  {
    id: '11111111-1111-1111-1111-111111111111',
    adapterType: 'PLACEHOLDER',
    displayName: 'Placeholder',
    available: true,
    model: 'deterministic-v1',
  },
  {
    id: '22222222-2222-2222-2222-222222222222',
    adapterType: 'GEMINI_GENERATE_CONTENT',
    displayName: 'Google Gemini',
    available: false,
    model: null,
  },
];

describe('AiProviderService', () => {
  let service: AiProviderService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AiProviderService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('list() GETs /ai/providers', () => {
    service.list().subscribe((providers) => expect(providers).toEqual(PROVIDERS));
    const req = httpMock.expectOne(`${environment.apiUrl}/ai/providers`);
    expect(req.request.method).toBe('GET');
    req.flush(PROVIDERS);
  });
});
