import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { LucideCircleAlert, LucidePlus } from '@lucide/angular';
import { finalize } from 'rxjs';
import { CompanyCreateDialog } from '../../companies/company-create-dialog/company-create-dialog';
import { CompanyResponse } from '../../companies/company.models';
import { CompanyService } from '../../companies/company.service';
import { describeApiError } from '../../../core/http/describe-api-error';
import {
  EMPLOYMENT_TYPES,
  EmploymentType,
  JobRequest,
  WORK_MODES,
  WorkMode,
} from '../job.models';
import { JobService } from '../job.service';

interface JobFormControls {
  companyId: FormControl<string>;
  title: FormControl<string>;
  description: FormControl<string>;
  location: FormControl<string>;
  employmentType: FormControl<EmploymentType | null>;
  workMode: FormControl<WorkMode | null>;
  url: FormControl<string>;
  source: FormControl<string>;
}

const EMPLOYMENT_TYPE_LABELS: Record<EmploymentType, string> = {
  FULL_TIME: 'Full-time',
  PART_TIME: 'Part-time',
  CONTRACT: 'Contract',
  INTERNSHIP: 'Internship',
  FREELANCE: 'Freelance',
};

const WORK_MODE_LABELS: Record<WorkMode, string> = {
  REMOTE: 'Remote',
  HYBRID: 'Hybrid',
  ONSITE: 'On-site',
};

@Component({
  selector: 'app-job-form',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    LucideCircleAlert,
    LucidePlus,
  ],
  templateUrl: './job-form.html',
  styleUrl: './job-form.scss',
})
export class JobForm {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly jobService = inject(JobService);
  private readonly companyService = inject(CompanyService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly employmentTypes = EMPLOYMENT_TYPES;
  protected readonly workModes = WORK_MODES;

  private readonly jobId = this.route.snapshot.paramMap.get('id');
  protected readonly isEditMode = this.jobId !== null;

  protected readonly companies = signal<CompanyResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly submitting = signal(false);
  protected readonly serverError = signal<string | null>(null);

  protected readonly form = new FormGroup<JobFormControls>({
    companyId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    title: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    description: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    location: new FormControl('', { nonNullable: true }),
    employmentType: new FormControl<EmploymentType | null>(null),
    workMode: new FormControl<WorkMode | null>(null),
    url: new FormControl('', { nonNullable: true }),
    source: new FormControl('', { nonNullable: true }),
  });

  constructor() {
    this.loadCompanies();
  }

  protected formatEmploymentType(type: EmploymentType): string {
    return EMPLOYMENT_TYPE_LABELS[type];
  }

  protected formatWorkMode(mode: WorkMode): string {
    return WORK_MODE_LABELS[mode];
  }

  protected openCreateCompanyDialog(): void {
    const ref = this.dialog.open<CompanyCreateDialog, void, CompanyResponse | null>(CompanyCreateDialog, {
      width: '480px',
    });

    ref.afterClosed().subscribe((company) => {
      if (company) {
        this.companies.update((current) => [...current, company]);
        this.form.controls.companyId.setValue(company.id);
      }
    });
  }

  protected cancel(): void {
    this.router.navigateByUrl('/jobs');
  }

  protected submit(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.serverError.set(null);

    const raw = this.form.getRawValue();
    const request: JobRequest = {
      companyId: raw.companyId,
      title: raw.title,
      description: raw.description,
      location: raw.location || null,
      employmentType: raw.employmentType,
      workMode: raw.workMode,
      url: raw.url || null,
      source: raw.source || null,
    };

    const action = this.isEditMode
      ? this.jobService.update(this.jobId!, request)
      : this.jobService.create(request);

    action.pipe(finalize(() => this.submitting.set(false))).subscribe({
      next: () => {
        this.snackBar.open(this.isEditMode ? 'Job updated.' : 'Job created.', 'Dismiss', {
          duration: 4000,
        });
        this.router.navigateByUrl('/jobs');
      },
      error: (error: HttpErrorResponse) => this.serverError.set(describeApiError(error)),
    });
  }

  private loadCompanies(): void {
    this.companyService.list().subscribe({
      next: (companies) => {
        this.companies.set(companies);
        if (this.isEditMode) {
          this.loadJob();
        } else {
          this.loading.set(false);
        }
      },
      error: () => {
        this.loading.set(false);
        this.serverError.set('We could not load your companies. Please try again.');
      },
    });
  }

  private loadJob(): void {
    this.jobService.get(this.jobId!).subscribe({
      next: (job) => {
        this.form.setValue({
          companyId: job.company.id,
          title: job.title,
          description: job.description,
          location: job.location ?? '',
          employmentType: job.employmentType,
          workMode: job.workMode,
          url: job.url ?? '',
          source: job.source ?? '',
        });
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.serverError.set('We could not load this job. Please try again.');
      },
    });
  }
}
