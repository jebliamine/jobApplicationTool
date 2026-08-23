import { Component, inject } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { AdminAiProviderResponse, AiProviderUpdateRequest } from '../ai-provider.models';

export interface AiProviderFormData {
  provider: AdminAiProviderResponse;
}

interface AiProviderFormControls {
  displayName: FormControl<string>;
  enabled: FormControl<boolean>;
  defaultModel: FormControl<string>;
  baseUrl: FormControl<string>;
  apiKey: FormControl<string>;
  clearApiKey: FormControl<boolean>;
}

/**
 * The API key input always starts blank — it is never pre-filled with the
 * existing key (which this dialog never even receives; only `hasApiKey` is
 * known). Leaving it blank on save means "keep the existing key". The
 * adapter type is fixed at creation time and shown read-only here — changing
 * an instance's wire protocol after creation doesn't make sense.
 */
@Component({
  selector: 'app-ai-provider-form',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCheckboxModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSlideToggleModule,
  ],
  templateUrl: './ai-provider-form.html',
  styleUrl: './ai-provider-form.scss',
})
export class AiProviderForm {
  protected readonly data = inject<AiProviderFormData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<AiProviderForm, AiProviderUpdateRequest | undefined>);

  protected readonly form = new FormGroup<AiProviderFormControls>({
    displayName: new FormControl(this.data.provider.displayName, {
      nonNullable: true,
      validators: [Validators.required],
    }),
    enabled: new FormControl(this.data.provider.enabled, { nonNullable: true }),
    defaultModel: new FormControl(this.data.provider.defaultModel ?? '', { nonNullable: true }),
    baseUrl: new FormControl(this.data.provider.baseUrl ?? '', { nonNullable: true }),
    apiKey: new FormControl('', { nonNullable: true }),
    clearApiKey: new FormControl(false, { nonNullable: true }),
  });

  protected cancel(): void {
    this.dialogRef.close(undefined);
  }

  protected save(): void {
    const raw = this.form.getRawValue();
    if (this.form.invalid || !raw.displayName.trim()) {
      this.form.markAllAsTouched();
      return;
    }

    const request: AiProviderUpdateRequest = {
      displayName: raw.displayName.trim(),
      enabled: raw.enabled,
      defaultModel: raw.defaultModel.trim() || null,
      baseUrl: raw.baseUrl.trim() || null,
      clearApiKey: raw.clearApiKey,
    };
    if (!raw.clearApiKey && raw.apiKey.trim()) {
      request.apiKey = raw.apiKey.trim();
    }
    this.dialogRef.close(request);
  }
}
