import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Observable, of, throwError } from 'rxjs';
import { AiProviderResponse } from '../../cover-letters/ai-provider.models';
import { AiProviderService } from '../../cover-letters/ai-provider.service';
import { CompanyResponse } from '../../companies/company.models';
import { CompanyService } from '../../companies/company.service';
import { JobExtractionResponse } from '../job.models';
import { JobService } from '../job.service';
import { JobForm } from './job-form';

const ACME: CompanyResponse = {
  id: '11111111-1111-1111-1111-111111111111',
  name: 'Acme',
  website: null,
  location: null,
  notes: null,
  owner: { fullName: 'Jane Doe', email: 'jane@example.com', role: 'USER' },
  createdAt: '2026-01-01T00:00:00',
  updatedAt: '2026-01-01T00:00:00',
} as any;

const PLACEHOLDER_PROVIDER: AiProviderResponse = {
  id: '22222222-2222-2222-2222-222222222222',
  adapterType: 'PLACEHOLDER',
  displayName: 'Placeholder',
  available: true,
  model: 'deterministic-v1',
};

const EXTRACTED: JobExtractionResponse = {
  title: 'Backend Engineer',
  companyName: 'Acme',
  description: 'Build things.',
  location: 'Berlin',
  employmentType: 'FULL_TIME',
  workMode: 'REMOTE',
  salaryRange: '€60,000-€75,000',
  url: 'https://example.test/job',
};

describe('JobForm', () => {
  let fixture: ComponentFixture<JobForm>;
  let component: JobForm;
  let extractSpy: ReturnType<typeof vi.fn>;
  let dialogOpenSpy: ReturnType<typeof vi.fn>;

  function setup(
    options: {
      extractResult?: Observable<JobExtractionResponse>;
      companies?: CompanyResponse[];
      dialogResult?: CompanyResponse | null;
    } = {},
  ) {
    const { extractResult = of(EXTRACTED), companies = [ACME], dialogResult = null } = options;

    extractSpy = vi.fn().mockReturnValue(extractResult);
    dialogOpenSpy = vi.fn().mockReturnValue({ afterClosed: () => of(dialogResult) });

    TestBed.configureTestingModule({
      imports: [JobForm, NoopAnimationsModule],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => null } } },
        },
        { provide: Router, useValue: { navigateByUrl: vi.fn(), getCurrentNavigation: () => null } },
        { provide: JobService, useValue: { extract: extractSpy, create: vi.fn(), update: vi.fn(), get: vi.fn() } },
        { provide: CompanyService, useValue: { list: () => of(companies) } },
        { provide: AiProviderService, useValue: { list: () => of([PLACEHOLDER_PROVIDER]) } },
        { provide: MatDialog, useValue: { open: dialogOpenSpy } },
      ],
    });

    fixture = TestBed.createComponent(JobForm);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('pre-fills the form and matches an existing company by name', () => {
    setup();

    component['pasteText'].setValue('some job posting text');
    component['extractFromPaste']();

    expect(extractSpy).toHaveBeenCalledWith('some job posting text', PLACEHOLDER_PROVIDER.id);
    expect(component['form'].controls.title.value).toBe('Backend Engineer');
    expect(component['form'].controls.description.value).toBe('Build things.');
    expect(component['form'].controls.location.value).toBe('Berlin');
    expect(component['form'].controls.employmentType.value).toBe('FULL_TIME');
    expect(component['form'].controls.workMode.value).toBe('REMOTE');
    expect(component['form'].controls.salaryRange.value).toBe('€60,000-€75,000');
    expect(component['form'].controls.url.value).toBe('https://example.test/job');
    expect(component['form'].controls.companyId.value).toBe(ACME.id);
    expect(dialogOpenSpy).not.toHaveBeenCalled();
  });

  it('matches an existing company case-insensitively', () => {
    setup({ extractResult: of({ ...EXTRACTED, companyName: 'ACME' }) });

    component['pasteText'].setValue('some job posting text');
    component['extractFromPaste']();

    expect(component['form'].controls.companyId.value).toBe(ACME.id);
    expect(dialogOpenSpy).not.toHaveBeenCalled();
  });

  it('opens the create-company dialog pre-filled when the extracted company is unknown', () => {
    setup({ extractResult: of({ ...EXTRACTED, companyName: 'New Co' }) });

    component['pasteText'].setValue('some job posting text');
    component['extractFromPaste']();

    expect(dialogOpenSpy).toHaveBeenCalledWith(
      expect.anything(),
      expect.objectContaining({ data: { name: 'New Co' } }),
    );
  });

  it('sets the company id once the create-company dialog returns a company', () => {
    const newCo: CompanyResponse = { ...ACME, id: '33333333-3333-3333-3333-333333333333', name: 'New Co' };
    setup({ extractResult: of({ ...EXTRACTED, companyName: 'New Co' }), dialogResult: newCo });

    component['pasteText'].setValue('some job posting text');
    component['extractFromPaste']();

    expect(component['form'].controls.companyId.value).toBe(newCo.id);
  });

  it('does not overwrite an already-typed field when extraction returns null for it', () => {
    setup({ extractResult: of({ ...EXTRACTED, location: null, salaryRange: null }) });
    component['form'].controls.location.setValue('Existing location');
    component['form'].controls.salaryRange.setValue('Existing range');

    component['pasteText'].setValue('some job posting text');
    component['extractFromPaste']();

    expect(component['form'].controls.location.value).toBe('Existing location');
    expect(component['form'].controls.salaryRange.value).toBe('Existing range');
  });

  it('shows a retry notice for a transient provider failure instead of a hard error', () => {
    setup({
      extractResult: throwError(
        () => new HttpErrorResponse({ status: 502, error: { message: 'The provider is temporarily unavailable (HTTP 503).' } }),
      ),
    });

    component['pasteText'].setValue('some job posting text');
    component['extractFromPaste']();

    expect(component['extractRetryNotice']()).not.toBeNull();
    expect(component['extractError']()).toBeNull();
  });

  it('shows a hard error for a non-transient extraction failure', () => {
    setup({
      extractResult: throwError(
        () => new HttpErrorResponse({ status: 502, error: { message: 'This provider instance is not configured.' } }),
      ),
    });

    component['pasteText'].setValue('some job posting text');
    component['extractFromPaste']();

    expect(component['extractError']()).toBe('This provider instance is not configured.');
    expect(component['extractRetryNotice']()).toBeNull();
  });

  it('does nothing when the paste text is blank', () => {
    setup();

    component['pasteText'].setValue('   ');
    component['extractFromPaste']();

    expect(extractSpy).not.toHaveBeenCalled();
  });
});
