import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialogRef } from '@angular/material/dialog';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { AiProviderCreate } from './ai-provider-create';

describe('AiProviderCreate', () => {
  let fixture: ComponentFixture<AiProviderCreate>;
  let component: AiProviderCreate;
  let dialogRef: { close: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    dialogRef = { close: vi.fn() };

    TestBed.configureTestingModule({
      imports: [AiProviderCreate, NoopAnimationsModule],
      providers: [{ provide: MatDialogRef, useValue: dialogRef }],
    });

    fixture = TestBed.createComponent(AiProviderCreate);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('does not submit when adapter type or display name is missing', () => {
    component['save']();

    expect(dialogRef.close).not.toHaveBeenCalled();
    expect(component['form'].controls.adapterType.touched).toBe(true);
  });

  it('suggests a base URL when the adapter type is chosen and the field is untouched', () => {
    component['form'].controls.adapterType.setValue('OPENAI_COMPATIBLE');

    component['onAdapterTypeChange']();

    expect(component['form'].controls.baseUrl.value).toBe('https://api.openai.com/v1');
  });

  it('does not overwrite a manually-edited base URL when the adapter type changes', () => {
    component['form'].controls.baseUrl.setValue('http://localhost:11434/v1');
    component['form'].controls.baseUrl.markAsDirty();
    component['form'].controls.adapterType.setValue('GEMINI_GENERATE_CONTENT');

    component['onAdapterTypeChange']();

    expect(component['form'].controls.baseUrl.value).toBe('http://localhost:11434/v1');
  });

  it('submits a well-formed create request', () => {
    component['form'].setValue({
      adapterType: 'ANTHROPIC_MESSAGES',
      displayName: 'Company Anthropic',
      enabled: true,
      defaultModel: 'claude-sonnet-4',
      baseUrl: 'https://api.anthropic.com',
      apiKey: 'secret-key',
    });

    component['save']();

    expect(dialogRef.close).toHaveBeenCalledWith({
      adapterType: 'ANTHROPIC_MESSAGES',
      displayName: 'Company Anthropic',
      enabled: true,
      defaultModel: 'claude-sonnet-4',
      baseUrl: 'https://api.anthropic.com',
      apiKey: 'secret-key',
    });
  });

  it('omits apiKey when left blank, for an unauthenticated local server', () => {
    component['form'].setValue({
      adapterType: 'OPENAI_COMPATIBLE',
      displayName: 'My Ollama Server',
      enabled: true,
      defaultModel: 'llama3',
      baseUrl: 'http://localhost:11434/v1',
      apiKey: '',
    });

    component['save']();

    expect(dialogRef.close).toHaveBeenCalledWith(
      expect.not.objectContaining({ apiKey: expect.anything() }),
    );
  });

  it('cancel() closes with no result', () => {
    component['cancel']();

    expect(dialogRef.close).toHaveBeenCalledWith(undefined);
  });
});
