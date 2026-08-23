import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Observable, of, throwError } from 'rxjs';
import {
  AdminAiProviderResponse,
  AiProviderCreateRequest,
  AiProviderTestResult,
  AiProviderUpdateRequest,
} from '../ai-provider.models';
import { AdminAiProviderService } from '../ai-provider.service';
import { AiProviderList } from './ai-provider-list';

const PLACEHOLDER: AdminAiProviderResponse = {
  id: '11111111-1111-1111-1111-111111111111',
  adapterType: 'PLACEHOLDER',
  displayName: 'Placeholder',
  enabled: true,
  hasApiKey: false,
  defaultModel: 'deterministic-v1',
  baseUrl: null,
};

const DISABLED_GEMINI: AdminAiProviderResponse = {
  id: '22222222-2222-2222-2222-222222222222',
  adapterType: 'GEMINI_GENERATE_CONTENT',
  displayName: 'Google Gemini',
  enabled: false,
  hasApiKey: false,
  defaultModel: null,
  baseUrl: null,
};

describe('AiProviderList', () => {
  let fixture: ComponentFixture<AiProviderList>;
  let component: AiProviderList;
  let testSpy: ReturnType<typeof vi.fn>;
  let updateSpy: ReturnType<typeof vi.fn>;
  let createSpy: ReturnType<typeof vi.fn>;
  let deleteSpy: ReturnType<typeof vi.fn>;
  let dialogOpenSpy: ReturnType<typeof vi.fn>;

  function setup(
    options: {
      listResult?: Observable<AdminAiProviderResponse[]>;
      testResult?: Observable<AiProviderTestResult>;
      dialogResult?: AiProviderUpdateRequest | AiProviderCreateRequest | boolean | undefined;
    } = {},
  ) {
    const { listResult = of([PLACEHOLDER, DISABLED_GEMINI]), testResult = of({ success: true, message: 'OK' }) } =
      options;

    testSpy = vi.fn().mockReturnValue(testResult);
    updateSpy = vi.fn().mockReturnValue(of(PLACEHOLDER));
    createSpy = vi.fn().mockReturnValue(of(DISABLED_GEMINI));
    deleteSpy = vi.fn().mockReturnValue(of(undefined));
    dialogOpenSpy = vi.fn().mockReturnValue({
      afterClosed: () => of(options.dialogResult),
    });

    TestBed.configureTestingModule({
      imports: [AiProviderList, NoopAnimationsModule],
      providers: [
        {
          provide: AdminAiProviderService,
          useValue: { list: () => listResult, test: testSpy, update: updateSpy, create: createSpy, delete: deleteSpy },
        },
        { provide: MatDialog, useValue: { open: dialogOpenSpy } },
        { provide: MatSnackBar, useValue: { open: vi.fn() } },
      ],
    });

    fixture = TestBed.createComponent(AiProviderList);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('loads and displays every provider', () => {
    setup();

    expect(component['providers']()).toEqual([PLACEHOLDER, DISABLED_GEMINI]);
    expect(component['loading']()).toBe(false);
  });

  it('shows an error state when loading fails', () => {
    setup({ listResult: throwError(() => new Error('boom')) });

    expect(component['serverError']()).not.toBeNull();
  });

  it('never renders an API key anywhere on the page', () => {
    setup();

    expect(fixture.nativeElement.textContent).not.toContain('sk-');
    expect(fixture.nativeElement.textContent).toContain('Not configured');
  });

  it('shows enabled/disabled status per provider', () => {
    setup();

    const statusText = fixture.nativeElement.textContent as string;
    expect(statusText).toContain('Enabled');
    expect(statusText).toContain('Disabled');
  });

  it('disables Test Connection for a disabled provider', () => {
    setup();

    const buttons = Array.from(fixture.nativeElement.querySelectorAll('button')) as HTMLButtonElement[];
    const testButtons = buttons.filter((b) => b.textContent?.includes('Test Connection'));
    expect(testButtons.some((b) => b.disabled)).toBe(true);
  });

  it('does not offer deletion of the built-in Placeholder instance', () => {
    setup();

    const deleteButtons = fixture.nativeElement.querySelectorAll('button[aria-label="Delete provider"]');
    expect(deleteButtons.length).toBe(1);
  });

  it('testConnection() calls the service and shows the result', () => {
    setup();

    component['testConnection'](PLACEHOLDER);

    expect(testSpy).toHaveBeenCalledWith(PLACEHOLDER.id);
    expect(component['testingProviderId']()).toBeNull();
  });

  it('testConnection() ignores a second call while one is already in flight', () => {
    setup({ testResult: new Observable<AiProviderTestResult>() });

    component['testConnection'](PLACEHOLDER);
    component['testConnection'](PLACEHOLDER);

    expect(testSpy).toHaveBeenCalledTimes(1);
  });

  it('testConnection() surfaces a failure result without throwing', () => {
    setup({ testResult: of({ success: false, message: 'Gemini is not configured.' }) });

    expect(() => component['testConnection'](DISABLED_GEMINI)).not.toThrow();
  });

  it('configure() applies the update returned by the dialog', () => {
    setup({ dialogResult: { enabled: true } });

    component['configure'](DISABLED_GEMINI);

    expect(updateSpy).toHaveBeenCalledWith(DISABLED_GEMINI.id, { enabled: true });
  });

  it('configure() does nothing when the dialog is cancelled', () => {
    setup({ dialogResult: undefined });

    component['configure'](DISABLED_GEMINI);

    expect(updateSpy).not.toHaveBeenCalled();
  });

  it('addProvider() creates the instance returned by the dialog and appends it', () => {
    const created: AiProviderCreateRequest = {
      adapterType: 'GEMINI_GENERATE_CONTENT',
      displayName: 'Google Gemini',
    };
    setup({ dialogResult: created });

    component['addProvider']();

    expect(createSpy).toHaveBeenCalledWith(created);
    expect(component['providers']()).toContainEqual(DISABLED_GEMINI);
  });

  it('addProvider() does nothing when the dialog is cancelled', () => {
    setup({ dialogResult: undefined });

    component['addProvider']();

    expect(createSpy).not.toHaveBeenCalled();
  });

  it('deleteProvider() removes the instance when confirmed', () => {
    setup({ dialogResult: true });

    component['deleteProvider'](DISABLED_GEMINI);

    expect(deleteSpy).toHaveBeenCalledWith(DISABLED_GEMINI.id);
    expect(component['providers']()).not.toContainEqual(DISABLED_GEMINI);
  });

  it('deleteProvider() does nothing when not confirmed', () => {
    setup({ dialogResult: false });

    component['deleteProvider'](DISABLED_GEMINI);

    expect(deleteSpy).not.toHaveBeenCalled();
  });
});
