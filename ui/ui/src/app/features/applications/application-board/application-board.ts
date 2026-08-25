import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CdkDrag, CdkDragDrop, CdkDropList, CdkDropListGroup } from '@angular/cdk/drag-drop';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import {
  LucideCalendar,
  LucideCheck,
  LucideCircleAlert,
  LucideList,
} from '@lucide/angular';
import { finalize } from 'rxjs';
import { describeApiError } from '../../../core/http/describe-api-error';
import { ToastService } from '../../../core/ui/toast.service';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';
import {
  APPLICATION_STATUSES,
  ApplicationResponse,
  ApplicationStatus,
  InterviewStageResponse,
  buildStatusChangeRequest,
  nextInterviewStage,
} from '../application.models';
import { ApplicationService } from '../application.service';
import {
  APPLICATION_STATUS_LABELS,
  APPLICATION_STATUS_SEVERITY,
  ApplicationStatusSeverity,
} from '../application-status';

type LoadState = 'loading' | 'loaded' | 'error';

@Component({
  selector: 'app-application-board',
  imports: [
    DatePipe,
    RouterLink,
    CdkDrag,
    CdkDropList,
    CdkDropListGroup,
    MatButtonModule,
    MatMenuModule,
    MatProgressSpinnerModule,
    LucideCalendar,
    LucideCheck,
    LucideCircleAlert,
    LucideList,
    StatusBadge,
  ],
  templateUrl: './application-board.html',
  styleUrl: './application-board.scss',
})
export class ApplicationBoard {
  private readonly applicationService = inject(ApplicationService);
  private readonly toast = inject(ToastService);

  protected readonly statuses = APPLICATION_STATUSES;

  private readonly state = signal<LoadState>('loading');
  private readonly _applications = signal<ApplicationResponse[]>([]);
  protected readonly changingStatusId = signal<string | null>(null);

  protected readonly loading = computed(() => this.state() === 'loading');
  protected readonly error = computed(() => this.state() === 'error');
  protected readonly empty = computed(
    () => this.state() === 'loaded' && this._applications().length === 0,
  );

  private readonly columns = computed(() => {
    const grouped = new Map<ApplicationStatus, ApplicationResponse[]>();
    for (const status of APPLICATION_STATUSES) {
      grouped.set(status, []);
    }
    for (const application of this._applications()) {
      grouped.get(application.status)?.push(application);
    }
    return grouped;
  });

  constructor() {
    this.load();
  }

  protected statusLabel(status: ApplicationStatus): string {
    return APPLICATION_STATUS_LABELS[status];
  }

  protected statusSeverity(status: ApplicationStatus): ApplicationStatusSeverity {
    return APPLICATION_STATUS_SEVERITY[status];
  }

  protected columnApplications(status: ApplicationStatus): ApplicationResponse[] {
    return this.columns().get(status) ?? [];
  }

  protected nextInterviewStage(application: ApplicationResponse): InterviewStageResponse | null {
    return nextInterviewStage(application);
  }

  protected load(): void {
    this.state.set('loading');
    this.applicationService.list().subscribe({
      next: (applications) => {
        this._applications.set(applications);
        this.state.set('loaded');
      },
      error: () => this.state.set('error'),
    });
  }

  protected onDrop(event: CdkDragDrop<ApplicationStatus>): void {
    if (event.previousContainer === event.container) {
      return;
    }
    this.changeStatus(event.item.data as ApplicationResponse, event.container.data);
  }

  protected changeStatus(application: ApplicationResponse, status: ApplicationStatus): void {
    if (status === application.status || this.changingStatusId()) {
      return;
    }

    const previousStatus = application.status;
    this.changingStatusId.set(application.id);
    // Optimistic move so drag-and-drop doesn't visibly snap back while the
    // request is in flight; rolled back on error.
    this._applications.update((current) =>
      current.map((a) => (a.id === application.id ? { ...a, status } : a)),
    );

    this.applicationService
      .update(application.id, buildStatusChangeRequest(application, status))
      .pipe(finalize(() => this.changingStatusId.set(null)))
      .subscribe({
        next: (updated) => {
          this._applications.update((current) =>
            current.map((a) => (a.id === updated.id ? updated : a)),
          );
          this.toast.success('Status updated.');
        },
        error: (error: HttpErrorResponse) => {
          this._applications.update((current) =>
            current.map((a) => (a.id === application.id ? { ...a, status: previousStatus } : a)),
          );
          this.toast.error(describeApiError(error));
        },
      });
  }
}
