import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { AdminAiProviderResponse } from '../ai-provider.models';
import { AiProviderForm, AiProviderFormData } from './ai-provider-form';

const CONFIGURED_GEMINI: AdminAiProviderResponse = {
  id: '11111111-1111-1111-1111-111111111111',
  adapterType: 'GEMINI_GENERATE_CONTENT',
  displayName: 'Google Gemini',
  enabled: true,
  hasApiKey: true,
  defaultModel: 'gemini-2.0-flash',
  baseUrl: 'https://generativelanguage.googleapis.com',
};

describe('AiProviderForm', () => {
  let fixture: ComponentFixture<AiProviderForm>;
  let component: AiProviderForm;
  let dialogRef: { close: ReturnType<typeof vi.fn> };

  function setup(provider: AdminAiProviderResponse) {
    dialogRef = { close: vi.fn() };

    TestBed.configureTestingModule({
      imports: [AiProviderForm, NoopAnimationsModule],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: { provider } satisfies AiProviderFormData },
        { provide: MatDialogRef, useValue: dialogRef },
      ],
    });

    fixture = TestBed.createComponent(AiProviderForm);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('pre-fills display name, enabled, model, and base URL from the provider data', () => {
    setup(CONFIGURED_GEMINI);

    expect(component['form'].controls.displayName.value).toBe('Google Gemini');
    expect(component['form'].controls.enabled.value).toBe(true);
    expect(component['form'].controls.defaultModel.value).toBe('gemini-2.0-flash');
    expect(component['form'].controls.baseUrl.value).toBe('https://generativelanguage.googleapis.com');
  });

  it('never pre-fills the API key field, even when a key is already configured', () => {
    setup(CONFIGURED_GEMINI);

    expect(component['form'].controls.apiKey.value).toBe('');
    const input = fixture.nativeElement.querySelector('input[formcontrolname="apiKey"]');
    expect(input.value).toBe('');
  });

  it('shows the key status as text, never the key itself', () => {
    setup(CONFIGURED_GEMINI);

    expect(fixture.nativeElement.textContent).toContain('Configured');
    expect(fixture.nativeElement.textContent).not.toContain('actual-secret-value');
  });

  it('does not offer a clear-key option when no key is configured', () => {
    setup({ ...CONFIGURED_GEMINI, hasApiKey: false });

    expect(fixture.nativeElement.querySelector('mat-checkbox')).toBeNull();
  });

  it('save() rejects a blank display name', () => {
    setup(CONFIGURED_GEMINI);
    component['form'].controls.displayName.setValue('   ');

    component['save']();

    expect(dialogRef.close).not.toHaveBeenCalled();
  });

  it('save() omits apiKey when the field is left blank', () => {
    setup(CONFIGURED_GEMINI);

    component['save']();

    expect(dialogRef.close).toHaveBeenCalledWith(
      expect.not.objectContaining({ apiKey: expect.anything() }),
    );
  });

  it('save() includes the new apiKey when provided', () => {
    setup(CONFIGURED_GEMINI);
    component['form'].controls.apiKey.setValue('new-secret-key');

    component['save']();

    expect(dialogRef.close).toHaveBeenCalledWith(expect.objectContaining({ apiKey: 'new-secret-key' }));
  });

  it('save() with clearApiKey checked sends clearApiKey=true and no apiKey', () => {
    setup(CONFIGURED_GEMINI);
    component['form'].controls.apiKey.setValue('typed-but-should-be-ignored');
    component['form'].controls.clearApiKey.setValue(true);

    component['save']();

    expect(dialogRef.close).toHaveBeenCalledWith(
      expect.objectContaining({ clearApiKey: true }),
    );
    const [request] = dialogRef.close.mock.calls[0];
    expect(request.apiKey).toBeUndefined();
  });

  it('cancel() closes with no result', () => {
    setup(CONFIGURED_GEMINI);

    component['cancel']();

    expect(dialogRef.close).toHaveBeenCalledWith(undefined);
  });
});
