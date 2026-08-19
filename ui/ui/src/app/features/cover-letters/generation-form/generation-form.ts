import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { LucideCircleAlert, LucideSparkles } from '@lucide/angular';
import { finalize, forkJoin, timer } from 'rxjs';
import { describeApiError } from '../../../core/http/describe-api-error';
import { CvResponse } from '../../cv/cv.models';
import { CvService } from '../../cv/cv.service';
import { JobResponse } from '../../jobs/job.models';
import { JobService } from '../../jobs/job.service';
import { GenerationRequestResponse } from '../generation.models';
import { GenerationService } from '../generation.service';

const POLL_INTERVAL_MS = 1000;
const MAX_POLL_ATTEMPTS = 15;

interface GenerationFormControls {
  jobId: FormControl<string>;
  cvDocumentId: FormControl<string>;
}

@Component({
  selector: 'app-generation-form',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    LucideCircleAlert,
    LucideSparkles,
  ],
  templateUrl: './generation-form.html',
  styleUrl: './generation-form.scss',
})
export class GenerationForm {
  private readonly router = inject(Router);
  private readonly generationService = inject(GenerationService);
  private readonly jobService = inject(JobService);
  private readonly cvService = inject(CvService);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly jobs = signal<JobResponse[]>([]);
  protected readonly cvs = signal<CvResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly generating = signal(false);
  protected readonly generatingStatus = signal<string | null>(null);
  protected readonly serverError = signal<string | null>(null);

  protected readonly form = new FormGroup<GenerationFormControls>({
    jobId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    cvDocumentId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  constructor() {
    forkJoin({ jobs: this.jobService.list(), cvs: this.cvService.list() })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: ({ jobs, cvs }) => {
          this.jobs.set(jobs);
          this.cvs.set(cvs);
        },
        error: () => this.serverError.set('We could not load your jobs and CVs. Please try again.'),
      });
  }

  protected cancel(): void {
    this.router.navigateByUrl('/cover-letters');
  }

  protected submit(): void {
    if (this.form.invalid || this.generating()) {
      this.form.markAllAsTouched();
      return;
    }

    this.generating.set(true);
    this.serverError.set(null);
    this.generatingStatus.set('Starting generation…');

    const raw = this.form.getRawValue();
    this.generationService.create({ jobId: raw.jobId, cvDocumentId: raw.cvDocumentId }).subscribe({
      next: (request) => this.handleGenerationUpdate(request, 0),
      error: (error: HttpErrorResponse) => {
        this.generating.set(false);
        this.generatingStatus.set(null);
        this.serverError.set(describeApiError(error));
      },
    });
  }

  private handleGenerationUpdate(request: GenerationRequestResponse, attempt: number): void {
    if (request.status === 'COMPLETED' && request.coverLetter) {
      this.generating.set(false);
      this.generatingStatus.set(null);
      this.snackBar.open('Cover letter generated.', 'Dismiss', { duration: 4000 });
      this.router.navigateByUrl(`/cover-letters/${request.coverLetter.id}`);
      return;
    }

    if (request.status === 'FAILED') {
      this.generating.set(false);
      this.generatingStatus.set(null);
      this.serverError.set(request.errorMessage || 'Generation failed. Please try again.');
      return;
    }

    if (attempt >= MAX_POLL_ATTEMPTS) {
      this.generating.set(false);
      this.generatingStatus.set(null);
      this.serverError.set('Generation is taking longer than expected. Please try again shortly.');
      return;
    }

    this.generatingStatus.set(
      request.status === 'IN_PROGRESS' ? 'Generating your cover letter…' : 'Waiting to start…',
    );

    timer(POLL_INTERVAL_MS).subscribe(() => {
      this.generationService.get(request.id).subscribe({
        next: (updated) => this.handleGenerationUpdate(updated, attempt + 1),
        error: (error: HttpErrorResponse) => {
          this.generating.set(false);
          this.generatingStatus.set(null);
          this.serverError.set(describeApiError(error));
        },
      });
    });
  }
}
