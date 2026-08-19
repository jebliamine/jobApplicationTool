import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { LucideCircleAlert } from '@lucide/angular';
import { finalize } from 'rxjs';
import { describeJobError } from '../../jobs/job-error';
import { CompanyRequest } from '../company.models';
import { CompanyService } from '../company.service';

interface CompanyFormControls {
  name: FormControl<string>;
  website: FormControl<string>;
  location: FormControl<string>;
  notes: FormControl<string>;
}

@Component({
  selector: 'app-company-form',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    LucideCircleAlert,
  ],
  templateUrl: './company-form.html',
  styleUrl: './company-form.scss',
})
export class CompanyForm {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly companyService = inject(CompanyService);
  private readonly snackBar = inject(MatSnackBar);

  private readonly companyId = this.route.snapshot.paramMap.get('id');
  protected readonly isEditMode = this.companyId !== null;

  protected readonly loading = signal(this.isEditMode);
  protected readonly submitting = signal(false);
  protected readonly serverError = signal<string | null>(null);

  protected readonly form = new FormGroup<CompanyFormControls>({
    name: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    website: new FormControl('', { nonNullable: true }),
    location: new FormControl('', { nonNullable: true }),
    notes: new FormControl('', { nonNullable: true }),
  });

  constructor() {
    if (this.isEditMode) {
      this.loadCompany();
    }
  }

  protected cancel(): void {
    this.router.navigateByUrl(this.isEditMode ? `/companies/${this.companyId}` : '/companies');
  }

  protected submit(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.serverError.set(null);

    const raw = this.form.getRawValue();
    const request: CompanyRequest = {
      name: raw.name,
      website: raw.website || null,
      location: raw.location || null,
      notes: raw.notes || null,
    };

    const action = this.isEditMode
      ? this.companyService.update(this.companyId!, request)
      : this.companyService.create(request);

    action.pipe(finalize(() => this.submitting.set(false))).subscribe({
      next: (company) => {
        this.snackBar.open(this.isEditMode ? 'Company updated.' : 'Company created.', 'Dismiss', {
          duration: 4000,
        });
        this.router.navigateByUrl(this.isEditMode ? `/companies/${company.id}` : '/companies');
      },
      error: (error: HttpErrorResponse) => this.serverError.set(describeJobError(error)),
    });
  }

  private loadCompany(): void {
    this.companyService.get(this.companyId!).subscribe({
      next: (company) => {
        this.form.setValue({
          name: company.name,
          website: company.website ?? '',
          location: company.location ?? '',
          notes: company.notes ?? '',
        });
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.serverError.set('We could not load this company. Please try again.');
      },
    });
  }
}
