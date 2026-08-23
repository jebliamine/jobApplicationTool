import { Component, inject } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { AdapterType, AiProviderCreateRequest, CREATABLE_ADAPTER_TYPES } from '../ai-provider.models';

interface AiProviderCreateControls {
  adapterType: FormControl<AdapterType | ''>;
  displayName: FormControl<string>;
  enabled: FormControl<boolean>;
  defaultModel: FormControl<string>;
  baseUrl: FormControl<string>;
  apiKey: FormControl<string>;
}

/** Suggested base URLs per adapter type, purely to save typing — the field stays fully editable. */
const SUGGESTED_BASE_URLS: Partial<Record<AdapterType, string>> = {
  OPENAI_COMPATIBLE: 'https://api.openai.com/v1',
  ANTHROPIC_MESSAGES: 'https://api.anthropic.com',
  GEMINI_GENERATE_CONTENT: 'https://generativelanguage.googleapis.com',
};

@Component({
  selector: 'app-ai-provider-create',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSlideToggleModule,
  ],
  templateUrl: './ai-provider-create.html',
  styleUrl: './ai-provider-create.scss',
})
export class AiProviderCreate {
  private readonly dialogRef = inject(MatDialogRef<AiProviderCreate, AiProviderCreateRequest | undefined>);

  protected readonly adapterTypes = CREATABLE_ADAPTER_TYPES;

  protected readonly form = new FormGroup<AiProviderCreateControls>({
    adapterType: new FormControl<AdapterType | ''>('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    displayName: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    enabled: new FormControl(false, { nonNullable: true }),
    defaultModel: new FormControl('', { nonNullable: true }),
    baseUrl: new FormControl('', { nonNullable: true }),
    apiKey: new FormControl('', { nonNullable: true }),
  });

  protected onAdapterTypeChange(): void {
    const adapterType = this.form.controls.adapterType.value;
    if (!adapterType) {
      return;
    }
    if (!this.form.controls.baseUrl.dirty) {
      this.form.controls.baseUrl.setValue(SUGGESTED_BASE_URLS[adapterType] ?? '');
    }
  }

  protected cancel(): void {
    this.dialogRef.close(undefined);
  }

  protected save(): void {
    const raw = this.form.getRawValue();
    if (this.form.invalid || !this.form.controls.adapterType.value || !raw.displayName.trim()) {
      this.form.markAllAsTouched();
      return;
    }

    const request: AiProviderCreateRequest = {
      adapterType: raw.adapterType as AdapterType,
      displayName: raw.displayName.trim(),
      enabled: raw.enabled,
      defaultModel: raw.defaultModel.trim() || null,
      baseUrl: raw.baseUrl.trim() || null,
    };
    if (raw.apiKey.trim()) {
      request.apiKey = raw.apiKey.trim();
    }
    this.dialogRef.close(request);
  }
}
