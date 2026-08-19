import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Observable, of, throwError } from 'rxjs';
import { AdminAiProviderResponse, AiProviderTestResult, AiProviderUpdateRequest } from '../ai-provider.models';
import { AdminAiProviderService } from '../ai-provider.service';
import { AiProviderList } from './ai-provider-list';

const PLACEHOLDER: AdminAiProviderResponse = {
  provider: 'PLACEHOLDER',
  displayName: 'Placeholder',
  enabled: true,
  hasApiKey: false,
  defaultModel: 'deterministic-v1',
  baseUrl: null,
};

const DISABLED_GEMINI: AdminAiProviderResponse = {
  provider: 'GEMINI',
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
  let dialogOpenSpy: ReturnType<typeof vi.fn>;

  function setup(
    options: {
      listResult?: Observable<AdminAiProviderResponse[]>;
      testResult?: Observable<AiProviderTestResult>;
      dialogResult?: AiProviderUpdateRequest | undefined;
    } = {},
  ) {
    const { listResult = of([PLACEHOLDER, DISABLED_GEMINI]), testResult = of({ success: true, message: 'OK' }) } =
      options;

    testSpy = vi.fn().mockReturnValue(testResult);
    updateSpy = vi.fn().mockReturnValue(of(PLACEHOLDER));
    dialogOpenSpy = vi.fn().mockReturnValue({
      afterClosed: () => of(options.dialogResult),
    });

    TestBed.configureTestingModule({
      imports: [AiProviderList, NoopAnimationsModule],
      providers: [
        {
          provide: AdminAiProviderService,
          useValue: { list: () => listResult, test: testSpy, update: updateSpy },
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

  it('testConnection() calls the service and shows the result', () => {
    setup();

    component['testConnection'](PLACEHOLDER);

    expect(testSpy).toHaveBeenCalledWith('PLACEHOLDER');
    expect(component['testingProvider']()).toBeNull();
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

    expect(updateSpy).toHaveBeenCalledWith('GEMINI', { enabled: true });
  });

  it('configure() does nothing when the dialog is cancelled', () => {
    setup({ dialogResult: undefined });

    component['configure'](DISABLED_GEMINI);

    expect(updateSpy).not.toHaveBeenCalled();
  });
});
