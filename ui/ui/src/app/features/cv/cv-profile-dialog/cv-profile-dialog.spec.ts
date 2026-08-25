import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { AiProviderResponse } from '../../cover-letters/ai-provider.models';
import { AiProviderService } from '../../cover-letters/ai-provider.service';
import { CvProfileResponse, CvResponse } from '../cv.models';
import { CvService } from '../cv.service';
import { CvProfileDialog } from './cv-profile-dialog';

const CV: CvResponse = {
  id: '11111111-1111-1111-1111-111111111111',
  title: 'My Resume',
  fileName: 'resume.pdf',
  contentType: 'application/pdf',
  size: 1024,
  createdAt: '2026-01-01T00:00:00',
  updatedAt: '2026-01-01T00:00:00',
  owner: { fullName: 'Jane Doe', email: 'jane@example.com', role: 'USER' },
};

const NOT_ATTEMPTED_PROFILE: CvProfileResponse = {
  id: null,
  fullName: null,
  summary: null,
  experiences: [],
  skills: [],
  languages: [],
  status: 'NOT_ATTEMPTED',
  errorMessage: null,
  generatedAt: null,
};

const PLACEHOLDER_PROVIDER: AiProviderResponse = {
  id: '22222222-2222-2222-2222-222222222222',
  adapterType: 'PLACEHOLDER',
  displayName: 'Placeholder',
  available: true,
  model: 'deterministic-v1',
};

const GEMINI_PROVIDER: AiProviderResponse = {
  id: '33333333-3333-3333-3333-333333333333',
  adapterType: 'GEMINI_GENERATE_CONTENT',
  displayName: 'Google Gemini',
  available: true,
  model: 'gemini-2.0-flash',
};

const UNAVAILABLE_ANTHROPIC_PROVIDER: AiProviderResponse = {
  id: '44444444-4444-4444-4444-444444444444',
  adapterType: 'ANTHROPIC_MESSAGES',
  displayName: 'Company Anthropic',
  available: false,
  model: null,
};

describe('CvProfileDialog', () => {
  let fixture: ComponentFixture<CvProfileDialog>;
  let component: CvProfileDialog;
  let generateProfileSpy: ReturnType<typeof vi.fn>;

  function setup(
    options: {
      profile?: CvProfileResponse;
      providers?: AiProviderResponse[];
    } = {},
  ) {
    const { profile = NOT_ATTEMPTED_PROFILE, providers = [PLACEHOLDER_PROVIDER, GEMINI_PROVIDER] } = options;
    generateProfileSpy = vi.fn().mockReturnValue(of(profile));

    TestBed.configureTestingModule({
      imports: [CvProfileDialog, NoopAnimationsModule],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: { cv: CV } },
        { provide: MatDialogRef, useValue: { close: vi.fn() } },
        {
          provide: CvService,
          useValue: { getProfile: () => of(profile), generateProfile: generateProfileSpy },
        },
        { provide: AiProviderService, useValue: { list: () => of(providers) } },
      ],
    });

    fixture = TestBed.createComponent(CvProfileDialog);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('defaults the provider selection to the built-in Placeholder instance', () => {
    setup();

    expect(component['selectedProviderId']()).toBe(PLACEHOLDER_PROVIDER.id);
  });

  it('excludes unavailable providers from the selectable list', () => {
    setup({ providers: [PLACEHOLDER_PROVIDER, UNAVAILABLE_ANTHROPIC_PROVIDER] });

    expect(component['providers']()).toEqual([PLACEHOLDER_PROVIDER]);
  });

  it('falls back to the first available provider when Placeholder is unavailable', () => {
    setup({ providers: [{ ...PLACEHOLDER_PROVIDER, available: false }, GEMINI_PROVIDER] });

    expect(component['selectedProviderId']()).toBe(GEMINI_PROVIDER.id);
  });

  it('sends the default Placeholder provider id when generating without changing the selection', () => {
    setup();

    component['generate']();

    expect(generateProfileSpy).toHaveBeenCalledWith(CV.id, PLACEHOLDER_PROVIDER.id);
  });

  it('sends the selected real provider id when the user switches away from Placeholder', () => {
    setup();
    component['selectedProviderId'].set(GEMINI_PROVIDER.id);

    component['generate']();

    expect(generateProfileSpy).toHaveBeenCalledWith(CV.id, GEMINI_PROVIDER.id);
  });
});
