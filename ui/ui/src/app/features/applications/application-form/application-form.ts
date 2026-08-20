import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { ToastService } from '../../../core/ui/toast.service';
import { LucideCircleAlert } from '@lucide/angular';
import { finalize, forkJoin } from 'rxjs';
import { describeApiError } from '../../../core/http/describe-api-error';
import { CoverLetterResponse } from '../../cover-letters/cover-letter.models';
import { CoverLetterService } from '../../cover-letters/cover-letter.service';
import { CvResponse } from '../../cv/cv.models';
import { CvService } from '../../cv/cv.service';
import { JobResponse } from '../../jobs/job.models';
import { JobService } from '../../jobs/job.service';
import { APPLICATION_STATUSES, ApplicationRequest, ApplicationStatus } from '../application.models';
import { ApplicationService } from '../application.service';
import { APPLICATION_STATUS_LABELS } from '../application-status';

interface ApplicationFormControls {
  jobId: FormControl<string>;
  cvDocumentId: FormControl<string>;
  coverLetterId: FormControl<string>;
  status: FormControl<ApplicationStatus>;
  appliedAt: FormControl<string>;
  notes: FormControl<string>;
}

@Component({
  selector: 'app-application-form',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    DatePipe,
    LucideCircleAlert,
  ],
  templateUrl: './application-form.html',
  styleUrl: './application-form.scss',
})
export class ApplicationForm {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly applicationService = inject(ApplicationService);
  private readonly jobService = inject(JobService);
  private readonly cvService = inject(CvService);
  private readonly coverLetterService = inject(CoverLetterService);
  private readonly toast = inject(ToastService);

  protected readonly statuses = APPLICATION_STATUSES;

  private readonly applicationId = this.route.snapshot.paramMap.get('id');
  protected readonly isEditMode = this.applicationId !== null;

  protected readonly jobs = signal<JobResponse[]>([]);
  protected readonly cvs = signal<CvResponse[]>([]);
  protected readonly coverLetters = signal<CoverLetterResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly submitting = signal(false);
  protected readonly serverError = signal<string | null>(null);

  protected readonly form = new FormGroup<ApplicationFormControls>({
    jobId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    cvDocumentId: new FormControl('', { nonNullable: true }),
    coverLetterId: new FormControl('', { nonNullable: true }),
    status: new FormControl<ApplicationStatus>('APPLIED', { nonNullable: true, validators: [Validators.required] }),
    appliedAt: new FormControl(todayAsInputValue(), { nonNullable: true, validators: [Validators.required] }),
    notes: new FormControl('', { nonNullable: true }),
  });

  constructor() {
    this.loadOptions();
  }

  protected statusLabel(status: ApplicationStatus): string {
    return APPLICATION_STATUS_LABELS[status];
  }

  protected cancel(): void {
    this.router.navigateByUrl(this.isEditMode ? `/applications/${this.applicationId}` : '/applications');
  }

  protected submit(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.serverError.set(null);

    const raw = this.form.getRawValue();
    const request: ApplicationRequest = {
      jobId: raw.jobId,
      cvDocumentId: raw.cvDocumentId || null,
      coverLetterId: raw.coverLetterId || null,
      status: raw.status,
      appliedAt: raw.appliedAt,
      notes: raw.notes || null,
    };

    const action = this.isEditMode
      ? this.applicationService.update(this.applicationId!, request)
      : this.applicationService.create(request);

    action.pipe(finalize(() => this.submitting.set(false))).subscribe({
      next: (application) => {
        this.toast.success(this.isEditMode ? 'Application updated.' : 'Application created.');
        this.router.navigateByUrl(this.isEditMode ? `/applications/${application.id}` : '/applications');
      },
      error: (error: HttpErrorResponse) => this.serverError.set(describeApiError(error)),
    });
  }

  private loadOptions(): void {
    forkJoin({
      jobs: this.jobService.list(),
      cvs: this.cvService.list(),
      coverLetters: this.coverLetterService.list(),
    }).subscribe({
      next: ({ jobs, cvs, coverLetters }) => {
        this.jobs.set(jobs);
        this.cvs.set(cvs);
        this.coverLetters.set(coverLetters);
        if (this.isEditMode) {
          this.loadApplication();
        } else {
          this.loading.set(false);
        }
      },
      error: () => {
        this.loading.set(false);
        this.serverError.set('We could not load your jobs, CVs, and cover letters. Please try again.');
      },
    });
  }

  private loadApplication(): void {
    this.applicationService.get(this.applicationId!).subscribe({
      next: (application) => {
        // The dropdown only lists active cover letters; if this application
        // already references one that has since been archived, add it back
        // in so the current selection still shows up (and stays valid) —
        // archiving must never silently detach it from the application.
        const attached = application.coverLetter;
        if (attached && !this.coverLetters().some((letter) => letter.id === attached.id)) {
          this.coverLetters.update((current) => [...current, attached]);
        }

        this.form.setValue({
          jobId: application.job.id,
          cvDocumentId: application.cv?.id ?? '',
          coverLetterId: application.coverLetter?.id ?? '',
          status: application.status,
          appliedAt: application.appliedAt,
          notes: application.notes ?? '',
        });
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.serverError.set('We could not load this application. Please try again.');
      },
    });
  }
}

function todayAsInputValue(): string {
  const now = new Date();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${now.getFullYear()}-${month}-${day}`;
}
