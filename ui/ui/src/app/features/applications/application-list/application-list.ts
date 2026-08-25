import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSortModule, Sort } from '@angular/material/sort';
import { ToastService } from '../../../core/ui/toast.service';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import {
  LucideCheck,
  LucideCircleAlert,
  LucideClipboardList,
  LucideEye,
  LucideInbox,
  LucideLayoutGrid,
  LucidePencil,
  LucideSearch,
  LucidePlus,
  LucideTrash2,
} from '@lucide/angular';
import { finalize } from 'rxjs';
import { describeApiError } from '../../../core/http/describe-api-error';
import { UserService } from '../../../core/user/user.service';
import {
  ConfirmDialog,
  ConfirmDialogData,
} from '../../../shared/components/confirm-dialog/confirm-dialog';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';
import {
  APPLICATION_STATUSES,
  ApplicationResponse,
  ApplicationStatus,
  buildStatusChangeRequest,
} from '../application.models';
import { ApplicationService } from '../application.service';
import {
  APPLICATION_STATUS_LABELS,
  APPLICATION_STATUS_SEVERITY,
  ApplicationStatusSeverity,
} from '../application-status';

type LoadState = 'loading' | 'loaded' | 'error';
type StatusFilter = ApplicationStatus | 'ALL';

@Component({
  selector: 'app-application-list',
  imports: [
    DatePipe,
    FormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatMenuModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatSortModule,
    MatTableModule,
    MatTooltipModule,
    LucideCheck,
    LucideCircleAlert,
    LucideClipboardList,
    LucideEye,
    LucideInbox,
    LucideLayoutGrid,
    LucidePencil,
    LucideSearch,
    LucidePlus,
    LucideTrash2,
    StatusBadge,
  ],
  templateUrl: './application-list.html',
  styleUrl: './application-list.scss',
})
export class ApplicationList {
  private readonly applicationService = inject(ApplicationService);
  private readonly userService = inject(UserService);
  private readonly dialog = inject(MatDialog);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  protected readonly statuses = APPLICATION_STATUSES;

  private readonly state = signal<LoadState>('loading');
  private readonly _applications = signal<ApplicationResponse[]>([]);
  protected readonly deletingId = signal<string | null>(null);
  protected readonly changingStatusId = signal<string | null>(null);

  protected readonly searchTerm = signal('');
  protected readonly statusFilter = signal<StatusFilter>('ALL');
  protected readonly sortState = signal<Sort>({ active: '', direction: '' });

  protected readonly loading = computed(() => this.state() === 'loading');
  protected readonly error = computed(() => this.state() === 'error');
  protected readonly applications = this._applications.asReadonly();
  protected readonly isAdmin = computed(() => this.userService.currentUser()?.role === 'ADMIN');

  protected readonly visibleApplications = computed(() => {
    const term = this.searchTerm().trim().toLowerCase();
    const status = this.statusFilter();

    let result = this._applications();

    if (status !== 'ALL') {
      result = result.filter((application) => application.status === status);
    }
    if (term) {
      result = result.filter(
        (application) =>
          application.job.title.toLowerCase().includes(term) ||
          application.job.company.name.toLowerCase().includes(term),
      );
    }

    const sort = this.sortState();
    if (sort.active && sort.direction) {
      const direction = sort.direction === 'asc' ? 1 : -1;
      result = [...result].sort((a, b) => direction * this.compare(a, b, sort.active));
    }

    return result;
  });

  protected readonly displayedColumns = computed(() => {
    const base = ['job', 'company', 'status', 'appliedAt', 'cv', 'tags'];
    return [...base, ...(this.isAdmin() ? ['owner'] : []), 'actions'];
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

  protected onSortChange(sort: Sort): void {
    this.sortState.set(sort);
  }

  protected clearFilters(): void {
    this.searchTerm.set('');
    this.statusFilter.set('ALL');
  }

  protected viewApplication(application: ApplicationResponse): void {
    this.router.navigateByUrl(`/applications/${application.id}`);
  }

  protected editApplication(application: ApplicationResponse): void {
    this.router.navigateByUrl(`/applications/${application.id}/edit`);
  }

  protected changeStatus(application: ApplicationResponse, status: ApplicationStatus): void {
    if (status === application.status || this.changingStatusId()) {
      return;
    }

    this.changingStatusId.set(application.id);

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
        error: (error: HttpErrorResponse) => this.toast.error(describeApiError(error)),
      });
  }

  protected confirmDelete(application: ApplicationResponse): void {
    if (this.deletingId()) {
      return;
    }
    const ref = this.dialog.open<ConfirmDialog, ConfirmDialogData, boolean>(ConfirmDialog, {
      data: {
        title: 'Delete application?',
        message: `Are you sure you want to delete your application for "${application.job.title}"? This action cannot be undone.`,
      },
      width: '420px',
    });

    ref.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.performDelete(application);
      }
    });
  }

  private performDelete(application: ApplicationResponse): void {
    this.deletingId.set(application.id);
    this.applicationService.delete(application.id).subscribe({
      next: () => {
        this._applications.update((current) => current.filter((a) => a.id !== application.id));
        this.deletingId.set(null);
        this.toast.success('Application deleted.');
      },
      error: (error: HttpErrorResponse) => {
        this.deletingId.set(null);
        this.toast.error(describeApiError(error));
      },
    });
  }

  private compare(a: ApplicationResponse, b: ApplicationResponse, active: string): number {
    switch (active) {
      case 'job':
        return a.job.title.localeCompare(b.job.title);
      case 'company':
        return a.job.company.name.localeCompare(b.job.company.name);
      case 'status':
        return APPLICATION_STATUSES.indexOf(a.status) - APPLICATION_STATUSES.indexOf(b.status);
      case 'appliedAt':
        return a.appliedAt.localeCompare(b.appliedAt);
      default:
        return 0;
    }
  }
}
