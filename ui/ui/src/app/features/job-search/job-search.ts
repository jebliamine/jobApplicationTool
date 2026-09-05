import { SlicePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import {
  LucideBriefcase,
  LucideCircleAlert,
  LucideExternalLink,
  LucideMapPin,
  LucideSearch,
  LucideSearchX,
} from '@lucide/angular';
import { describeApiError } from '../../core/http/describe-api-error';
import { ToastService } from '../../core/ui/toast.service';
import { ExternalJobListing, ExternalJobSource, JobSearchSourceSummary } from './job-search.models';
import { JobSearchService } from './job-search.service';

type LoadState = 'idle' | 'loading' | 'loaded' | 'error';

const SOURCE_LABELS: Record<ExternalJobSource, string> = {
  ADZUNA: 'Adzuna',
  JOOBLE: 'Jooble',
  JSEARCH: 'JSearch',
};

interface JobSearchForm {
  keyword: FormControl<string>;
  location: FormControl<string>;
}

/**
 * Live external job search (GET /api/v1/job-search) — Adzuna/Jooble/JSearch combined. Distinct
 * from /jobs, which lists the jobs the user is already tracking. "Save to my jobs" hands a
 * listing to JobForm via router state, reusing the exact same prefill review flow as
 * paste-to-import (see JobForm.applyExtractionResult).
 */
@Component({
  selector: 'app-job-search',
  imports: [
    SlicePipe,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    LucideBriefcase,
    LucideCircleAlert,
    LucideExternalLink,
    LucideMapPin,
    LucideSearch,
    LucideSearchX,
  ],
  templateUrl: './job-search.html',
  styleUrl: './job-search.scss',
})
export class JobSearch {
  private readonly jobSearchService = inject(JobSearchService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  private readonly state = signal<LoadState>('idle');
  private readonly _results = signal<ExternalJobListing[]>([]);
  private readonly _sources = signal<JobSearchSourceSummary[]>([]);
  private readonly _page = signal(1);

  protected readonly loading = computed(() => this.state() === 'loading');
  protected readonly error = computed(() => this.state() === 'error');
  protected readonly searched = computed(() => this.state() === 'loaded' || this.state() === 'error');
  protected readonly results = this._results.asReadonly();
  protected readonly sources = this._sources.asReadonly();
  protected readonly page = this._page.asReadonly();
  protected readonly canGoPrevious = computed(() => this.page() > 1 && !this.loading());
  // No total-result count comes back from the API (sources are merged/deduped), so "next" just
  // stays enabled as long as the current page returned something — the true last page is only
  // discovered by paging one step past it.
  protected readonly canGoNext = computed(() => this.results().length > 0 && !this.loading());

  protected readonly form = new FormGroup<JobSearchForm>({
    keyword: new FormControl('', { nonNullable: true }),
    location: new FormControl('', { nonNullable: true }),
  });

  protected search(): void {
    if (this.loading()) {
      return;
    }
    this._page.set(1);
    this.runSearch();
  }

  protected retry(): void {
    if (this.loading()) {
      return;
    }
    this.runSearch();
  }

  protected nextPage(): void {
    if (!this.canGoNext()) {
      return;
    }
    this._page.update((current) => current + 1);
    this.runSearch();
  }

  protected previousPage(): void {
    if (!this.canGoPrevious()) {
      return;
    }
    this._page.update((current) => current - 1);
    this.runSearch();
  }

  private runSearch(): void {
    const { keyword, location } = this.form.getRawValue();
    this.state.set('loading');
    this.jobSearchService.search(keyword, location, this.page()).subscribe({
      next: (response) => {
        this._results.set(response.results);
        this._sources.set(response.sources);
        this.state.set('loaded');
      },
      error: (error: HttpErrorResponse) => {
        this.state.set('error');
        this.toast.error(describeApiError(error));
      },
    });
  }

  protected formatSource(source: ExternalJobSource): string {
    return SOURCE_LABELS[source];
  }

  protected sourceStatusLabel(summary: JobSearchSourceSummary): string {
    if (!summary.configured) {
      return 'not configured';
    }
    if (!summary.succeeded) {
      return 'temporarily unavailable';
    }
    return `${summary.resultCount} result${summary.resultCount === 1 ? '' : 's'}`;
  }

  protected trackListing(_index: number, listing: ExternalJobListing): string {
    return `${listing.source}:${listing.externalId ?? listing.url ?? listing.title}`;
  }

  /** Hands the listing to JobForm's create page via router state — reviewed and saved there, same as paste-to-import. */
  protected saveListing(listing: ExternalJobListing): void {
    this.router.navigateByUrl('/jobs/new', { state: { externalListing: listing } });
  }
}
