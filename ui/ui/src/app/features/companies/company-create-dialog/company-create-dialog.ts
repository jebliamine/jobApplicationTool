import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { LucideCircleAlert } from '@lucide/angular';
import { finalize } from 'rxjs';
import { CompanyResponse } from '../company.models';
import { CompanyService } from '../company.service';
import { describeApiError } from '../../../core/http/describe-api-error';

interface CompanyForm {
  name: FormControl<string>;
  website: FormControl<string>;
  location: FormControl<string>;
  notes: FormControl<string>;
}

@Component({
  selector: 'app-company-create-dialog',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    LucideCircleAlert,
  ],
  templateUrl: './company-create-dialog.html',
  styleUrl: './company-create-dialog.scss',
})
export class CompanyCreateDialog {
  private readonly companyService = inject(CompanyService);
  private readonly dialogRef = inject(MatDialogRef<CompanyCreateDialog, CompanyResponse | null>);

  protected readonly form = new FormGroup<CompanyForm>({
    name: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    website: new FormControl('', { nonNullable: true }),
    location: new FormControl('', { nonNullable: true }),
    notes: new FormControl('', { nonNullable: true }),
  });

  protected readonly submitting = signal(false);
  protected readonly serverError = signal<string | null>(null);

  protected cancel(): void {
    this.dialogRef.close(null);
  }

  protected submit(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.serverError.set(null);

    const raw = this.form.getRawValue();
    this.companyService
      .create({
        name: raw.name,
        website: raw.website || null,
        location: raw.location || null,
        notes: raw.notes || null,
      })
      .pipe(finalize(() => this.submitting.set(false)))
      .subscribe({
        next: (company) => this.dialogRef.close(company),
        error: (error: HttpErrorResponse) => this.serverError.set(describeApiError(error)),
      });
  }
}
