import { HttpErrorResponse, provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Observable, of, throwError } from 'rxjs';
import { AiProviderResponse } from '../ai-provider.models';
import { AiProviderService } from '../ai-provider.service';
import { CvService } from '../../cv/cv.service';
import { JobService } from '../../jobs/job.service';
import { GenerationRequestResponse } from '../generation.models';
import { GenerationService } from '../generation.service';
import { GenerationForm } from './generation-form';

const JOB = {
  id: '33333333-3333-3333-3333-333333333333',
  title: 'Backend Engineer',
  company: { name: 'Acme' },
} as any;

const CV = { id: '44444444-4444-4444-4444-444444444444', title: 'My Resume' } as any;

const PLACEHOLDER_ID = '11111111-1111-1111-1111-111111111111';
const GEMINI_ID = '22222222-2222-2222-2222-222222222222';

const PLACEHOLDER_PROVIDER: AiProviderResponse = {
  id: PLACEHOLDER_ID,
  adapterType: 'PLACEHOLDER',
  displayName: 'Placeholder',
  available: true,
  model: 'deterministic-v1',
};

const GEMINI_PROVIDER: AiProviderResponse = {
  id: GEMINI_ID,
  adapterType: 'GEMINI_GENERATE_CONTENT',
  displayName: 'Google Gemini',
  available: true,
  model: 'gemini-2.0-flash',
};

const DISABLED_GEMINI_PROVIDER: AiProviderResponse = {
  ...GEMINI_PROVIDER,
  available: false,
  model: null,
};

const COMPLETED_RESPONSE: GenerationRequestResponse = {
  id: '66666666-6666-6666-6666-666666666666',
  job: JOB,
  cv: CV,
  status: 'COMPLETED',
  provider: 'Placeholder',
  model: 'deterministic-v1',
  errorMessage: null,
  coverLetter: { id: '77777777-7777-7777-7777-777777777777' } as any,
  owner: { fullName: 'Jane Doe', email: 'jane@example.com', role: 'USER' },
  createdAt: '2026-01-01T00:00:00',
  startedAt: '2026-01-01T00:00:00',
  completedAt: '2026-01-01T00:00:01',
};

@Component({ template: '' })
class DummyCoverLetterDetail {}

describe('GenerationForm', () => {
  let fixture: ComponentFixture<GenerationForm>;
  let component: GenerationForm;
  let createSpy: ReturnType<typeof vi.fn>;

  function setup(
    options: {
      createResult?: Observable<GenerationRequestResponse>;
      providers?: AiProviderResponse[];
    } = {},
  ) {
    const { createResult = of(COMPLETED_RESPONSE), providers = [PLACEHOLDER_PROVIDER, GEMINI_PROVIDER] } = options;
    createSpy = vi.fn().mockReturnValue(createResult);

    TestBed.configureTestingModule({
      imports: [GenerationForm],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([{ path: 'cover-letters/:id', component: DummyCoverLetterDetail }]),
        { provide: JobService, useValue: { list: () => of([JOB]) } },
        { provide: CvService, useValue: { list: () => of([CV]) } },
        { provide: AiProviderService, useValue: { list: () => of(providers) } },
        { provide: GenerationService, useValue: { create: createSpy, get: vi.fn() } },
      ],
    });

    fixture = TestBed.createComponent(GenerationForm);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  function fillJobAndCv(): void {
    component['form'].controls.jobId.setValue(JOB.id);
    component['form'].controls.cvDocumentId.setValue(CV.id);
  }

  it('loads providers dynamically from AiProviderService and defaults to the built-in Placeholder instance', () => {
    setup();
    expect(component['form'].controls.provider.value).toBe(PLACEHOLDER_ID);
  });

  it('renders a provider selector with the providers returned by the backend', () => {
    setup();
    const labels = Array.from(fixture.nativeElement.querySelectorAll('mat-label')).map((el: any) =>
      el.textContent.trim(),
    );
    expect(labels).toContain('Provider');
    expect(component['providers']()).toEqual([PLACEHOLDER_PROVIDER, GEMINI_PROVIDER]);
  });

  it('excludes an unavailable provider from the selectable list', () => {
    setup({ providers: [PLACEHOLDER_PROVIDER, DISABLED_GEMINI_PROVIDER] });

    expect(component['providers']()).toEqual([PLACEHOLDER_PROVIDER]);
    expect(component['form'].controls.provider.value).toBe(PLACEHOLDER_ID);
  });

  it('falls back to the first available provider when Placeholder itself is unavailable', () => {
    setup({ providers: [{ ...PLACEHOLDER_PROVIDER, available: false }, GEMINI_PROVIDER] });

    expect(component['providers']()).toEqual([GEMINI_PROVIDER]);
    expect(component['form'].controls.provider.value).toBe(GEMINI_ID);
  });

  it('sends the Placeholder instance id by default when the user does not change the provider', () => {
    setup();
    fillJobAndCv();

    component['submit']();

    expect(createSpy).toHaveBeenCalledWith({ jobId: JOB.id, cvDocumentId: CV.id, providerId: PLACEHOLDER_ID });
  });

  it('sends the selected provider instance id', () => {
    setup();
    fillJobAndCv();
    component['form'].controls.provider.setValue(GEMINI_ID);

    component['submit']();

    expect(createSpy).toHaveBeenCalledWith({ jobId: JOB.id, cvDocumentId: CV.id, providerId: GEMINI_ID });
  });

  it('can explicitly select the Placeholder instance again after selecting another provider', () => {
    setup();
    fillJobAndCv();
    component['form'].controls.provider.setValue(GEMINI_ID);
    component['form'].controls.provider.setValue(PLACEHOLDER_ID);

    component['submit']();

    expect(createSpy).toHaveBeenCalledWith({ jobId: JOB.id, cvDocumentId: CV.id, providerId: PLACEHOLDER_ID });
  });

  it('displays a generation error returned by the backend', () => {
    setup({
      createResult: throwError(
        () => new HttpErrorResponse({ status: 400, error: { message: 'Gemini is not configured.' } }),
      ),
    });
    fillJobAndCv();

    component['submit']();

    expect(component['serverError']()).toBe('Gemini is not configured.');
    expect(component['generating']()).toBe(false);
  });
});
