import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { ToastService } from '../../../core/ui/toast.service';
import { LucideCircleAlert, LucideClock, LucideSparkles } from '@lucide/angular';
import { finalize, forkJoin, timer } from 'rxjs';
import { describeApiError } from '../../../core/http/describe-api-error';
import { AiProviderResponse } from '../ai-provider.models';
import { AiProviderService } from '../ai-provider.service';
import { CvProfileResponse, CvResponse } from '../../cv/cv.models';
import { CvService } from '../../cv/cv.service';
import { JobResponse } from '../../jobs/job.models';
import { JobService } from '../../jobs/job.service';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';
import { GenerationRequestResponse } from '../generation.models';
import { GenerationService } from '../generation.service';

const POLL_INTERVAL_MS = 1000;
const MAX_POLL_ATTEMPTS = 15;

/**
 * Matches the wording adapters use for a transient, capacity-related failure (rate limit hit,
 * provider temporarily unavailable, connection timeout) — deliberately provider-agnostic so it
 * covers Gemini/OpenAI-compatible/Anthropic without depending on their exact phrasing. Anything
 * that doesn't match (misconfiguration, auth, validation) is treated as a real error.
 */
const RETRYABLE_FAILURE_PATTERN = /rate limit|currently unavailable|temporarily unavailable|timeout or connection/i;

function isProviderBusy(message: string): boolean {
  return RETRYABLE_FAILURE_PATTERN.test(message);
}

interface GenerationFormControls {
  jobId: FormControl<string>;
  cvDocumentId: FormControl<string>;
  provider: FormControl<string>;
  useStructuredCv: FormControl<boolean>;
}

@Component({
  selector: 'app-generation-form',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    StatusBadge,
    LucideCircleAlert,
    LucideClock,
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
  private readonly aiProviderService = inject(AiProviderService);
  private readonly toast = inject(ToastService);

  protected readonly jobs = signal<JobResponse[]>([]);
  protected readonly cvs = signal<CvResponse[]>([]);
  protected readonly providers = signal<AiProviderResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly generating = signal(false);
  protected readonly generatingStatus = signal<string | null>(null);
  protected readonly serverError = signal<string | null>(null);
  protected readonly retryNotice = signal<string | null>(null);
  protected readonly selectedCvProfile = signal<CvProfileResponse | null>(null);

  protected readonly form = new FormGroup<GenerationFormControls>({
    jobId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    cvDocumentId: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    provider: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    useStructuredCv: new FormControl({ value: false, disabled: true }, { nonNullable: true }),
  });

  private readonly selectedCvId = toSignal(this.form.controls.cvDocumentId.valueChanges, { initialValue: '' });

  /**
   * Explains why the "use structured CV profile" checkbox is enabled/disabled — the profile can
   * be missing, still generating, or have failed, and each of those is still a normal state (not
   * an error) since generating a cover letter from the raw CV text always remains available.
   */
  protected readonly structuredCvHint = computed(() => {
    switch (this.selectedCvProfile()?.status) {
      case 'COMPLETED':
        return 'Uses the structured profile extracted from this CV instead of the raw document text.';
      case 'IN_PROGRESS':
        return 'The structured profile for this CV is still generating.';
      case 'FAILED':
        return 'Structured profile generation failed for this CV — using the raw CV text instead.';
      default:
        return 'Generate a structured profile from the CV page to enable this option.';
    }
  });

  constructor() {
    forkJoin({
      jobs: this.jobService.list(),
      cvs: this.cvService.list(),
      providers: this.aiProviderService.list(),
    })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: ({ jobs, cvs, providers }) => {
          this.jobs.set(jobs);
          this.cvs.set(cvs);

          // The dropdown only ever offers providers the backend reports as available —
          // a disabled/unconfigured provider is never selectable.
          const available = providers.filter((provider) => provider.available);
          this.providers.set(available);

          const defaultProvider =
            available.find((provider) => provider.adapterType === 'PLACEHOLDER') ?? available[0];
          if (defaultProvider) {
            this.form.controls.provider.setValue(defaultProvider.id);
          }
        },
        error: () => this.serverError.set('We could not load your jobs and CVs. Please try again.'),
      });

    // Re-checks the selected CV's structured-profile status whenever the CV selection changes,
    // so the "use structured profile" option only ever offers what's actually available for it.
    effect(() => {
      const cvId = this.selectedCvId();
      if (!cvId) {
        this.selectedCvProfile.set(null);
        return;
      }
      this.cvService.getProfile(cvId).subscribe({
        next: (profile) => this.selectedCvProfile.set(profile),
        error: () => this.selectedCvProfile.set(null),
      });
    });

    effect(() => {
      const control = this.form.controls.useStructuredCv;
      if (this.selectedCvProfile()?.status === 'COMPLETED') {
        control.enable({ emitEvent: false });
      } else {
        control.setValue(false, { emitEvent: false });
        control.disable({ emitEvent: false });
      }
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
    this.retryNotice.set(null);
    this.generatingStatus.set('Starting generation…');

    const raw = this.form.getRawValue();
    this.generationService
      .create({
        jobId: raw.jobId,
        cvDocumentId: raw.cvDocumentId,
        providerId: raw.provider,
        useStructuredCv: raw.useStructuredCv,
      })
      .subscribe({
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
      this.toast.success('Cover letter generated.');
      this.router.navigateByUrl(`/cover-letters/${request.coverLetter.id}`);
      return;
    }

    if (request.status === 'FAILED') {
      this.generating.set(false);
      this.generatingStatus.set(null);
      const message = request.errorMessage || 'Generation failed. Please try again.';
      if (isProviderBusy(message)) {
        this.retryNotice.set('This AI provider is getting a lot of requests right now. Wait a moment, then try again.');
      } else {
        this.serverError.set(message);
      }
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
