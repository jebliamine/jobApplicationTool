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
import { ToastService } from '../../../core/ui/toast.service';
import { LucideCircleAlert, LucideClipboardPaste, LucidePlus } from '@lucide/angular';
import { finalize } from 'rxjs';
import {
  CompanyCreateDialog,
  CompanyCreateDialogData,
} from '../../companies/company-create-dialog/company-create-dialog';
import { CompanyResponse } from '../../companies/company.models';
import { CompanyService } from '../../companies/company.service';
import { describeApiError } from '../../../core/http/describe-api-error';
import { isProviderBusy } from '../../../core/http/is-provider-busy';
import { AiProviderResponse } from '../../cover-letters/ai-provider.models';
import { AiProviderService } from '../../cover-letters/ai-provider.service';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';
import {
  EMPLOYMENT_TYPES,
  EmploymentType,
  JobExtractionResponse,
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
  salaryRange: FormControl<string>;
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
    LucideClipboardPaste,
    LucidePlus,
    StatusBadge,
  ],
  templateUrl: './job-form.html',
  styleUrl: './job-form.scss',
})
export class JobForm {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly jobService = inject(JobService);
  private readonly companyService = inject(CompanyService);
  private readonly aiProviderService = inject(AiProviderService);
  private readonly dialog = inject(MatDialog);
  private readonly toast = inject(ToastService);

  protected readonly employmentTypes = EMPLOYMENT_TYPES;
  protected readonly workModes = WORK_MODES;

  private readonly jobId = this.route.snapshot.paramMap.get('id');
  protected readonly isEditMode = this.jobId !== null;

  protected readonly companies = signal<CompanyResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly submitting = signal(false);
  protected readonly serverError = signal<string | null>(null);

  // Paste-to-import: pre-fills the form below from pasted job-posting text. Create mode only —
  // this speeds up capturing a new job, it has no bearing on editing one that already exists.
  protected readonly showPasteImport = signal(false);
  protected readonly pasteText = new FormControl('', { nonNullable: true });
  protected readonly extracting = signal(false);
  protected readonly extractError = signal<string | null>(null);
  protected readonly extractRetryNotice = signal<string | null>(null);
  protected readonly providers = signal<AiProviderResponse[]>([]);
  protected readonly selectedProviderId = signal<string>('');

  protected readonly form = new FormGroup<JobFormControls>({
    companyId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    title: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    description: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    location: new FormControl('', { nonNullable: true }),
    employmentType: new FormControl<EmploymentType | null>(null),
    workMode: new FormControl<WorkMode | null>(null),
    url: new FormControl('', { nonNullable: true }),
    source: new FormControl('', { nonNullable: true }),
    salaryRange: new FormControl('', { nonNullable: true }),
  });

  constructor() {
    this.loadCompanies();

    if (!this.isEditMode) {
      // Independent of everything else — if this fails, the provider selector simply doesn't
      // appear and extract() falls back to the backend's own default (Placeholder).
      this.aiProviderService.list().subscribe({
        next: (providers) => {
          const available = providers.filter((provider) => provider.available);
          this.providers.set(available);
          const defaultProvider = available.find((provider) => provider.adapterType === 'PLACEHOLDER') ?? available[0];
          if (defaultProvider) {
            this.selectedProviderId.set(defaultProvider.id);
          }
        },
      });
    }
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

  protected togglePasteImport(): void {
    this.showPasteImport.update((current) => !current);
  }

  protected extractFromPaste(): void {
    const rawText = this.pasteText.value.trim();
    if (!rawText || this.extracting()) {
      return;
    }

    this.extracting.set(true);
    this.extractError.set(null);
    this.extractRetryNotice.set(null);

    this.jobService
      .extract(rawText, this.selectedProviderId() || undefined)
      .pipe(finalize(() => this.extracting.set(false)))
      .subscribe({
        next: (result) => this.applyExtractionResult(result),
        error: (error: HttpErrorResponse) => {
          const message = describeApiError(error);
          if (isProviderBusy(message)) {
            this.extractRetryNotice.set('This AI provider is getting a lot of requests right now. Wait a moment, then try again.');
          } else {
            this.extractError.set(message);
          }
        },
      });
  }

  private applyExtractionResult(result: JobExtractionResponse): void {
    const current = this.form.getRawValue();
    this.form.patchValue({
      title: result.title ?? current.title,
      description: result.description ?? current.description,
      location: result.location ?? current.location,
      employmentType: result.employmentType ?? current.employmentType,
      workMode: result.workMode ?? current.workMode,
      url: result.url ?? current.url,
      salaryRange: result.salaryRange ?? current.salaryRange,
    });

    if (result.companyName) {
      this.matchOrCreateCompany(result.companyName);
    }

    this.showPasteImport.set(false);
    this.pasteText.setValue('');
    this.toast.success('Job details extracted. Review before saving.');
  }

  /** Extraction returns a plain company name — match it against the user's existing companies, or offer to create it. */
  private matchOrCreateCompany(companyName: string): void {
    const normalized = companyName.trim().toLowerCase();
    const match = this.companies().find((company) => company.name.trim().toLowerCase() === normalized);
    if (match) {
      this.form.controls.companyId.setValue(match.id);
      return;
    }

    const ref = this.dialog.open<CompanyCreateDialog, CompanyCreateDialogData, CompanyResponse | null>(
      CompanyCreateDialog,
      { width: '480px', data: { name: companyName } },
    );

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
      salaryRange: raw.salaryRange || null,
    };

    const action = this.isEditMode
      ? this.jobService.update(this.jobId!, request)
      : this.jobService.create(request);

    action.pipe(finalize(() => this.submitting.set(false))).subscribe({
      next: () => {
        this.toast.success(this.isEditMode ? 'Job updated.' : 'Job created.');
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
          salaryRange: job.salaryRange ?? '',
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
