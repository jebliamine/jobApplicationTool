import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ToastService } from '../../../core/ui/toast.service';
import {
  LucideArrowLeft,
  LucideBriefcase,
  LucideCalendar,
  LucideCircleAlert,
  LucideFileText,
  LucideMail,
  LucideNotebookText,
  LucidePencil,
  LucideShieldCheck,
  LucideTrash2,
  LucideUser,
} from '@lucide/angular';
import { describeApiError } from '../../../core/http/describe-api-error';
import { UserService } from '../../../core/user/user.service';
import {
  ConfirmDialog,
  ConfirmDialogData,
} from '../../../shared/components/confirm-dialog/confirm-dialog';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';
import { ApplicationResponse, ApplicationStatus } from '../application.models';
import { ApplicationService } from '../application.service';
import {
  APPLICATION_STATUS_LABELS,
  APPLICATION_STATUS_SEVERITY,
  ApplicationStatusSeverity,
} from '../application-status';

type LoadState = 'loading' | 'loaded' | 'error';

@Component({
  selector: 'app-application-detail',
  imports: [
    DatePipe,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatProgressSpinnerModule,
    LucideArrowLeft,
    LucideBriefcase,
    LucideCalendar,
    LucideCircleAlert,
    LucideFileText,
    LucideMail,
    LucideNotebookText,
    LucidePencil,
    LucideShieldCheck,
    LucideTrash2,
    LucideUser,
    StatusBadge,
  ],
  templateUrl: './application-detail.html',
  styleUrl: './application-detail.scss',
})
export class ApplicationDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly applicationService = inject(ApplicationService);
  private readonly userService = inject(UserService);
  private readonly dialog = inject(MatDialog);
  private readonly toast = inject(ToastService);

  private readonly applicationId = this.route.snapshot.paramMap.get('id')!;

  private readonly state = signal<LoadState>('loading');
  private readonly _application = signal<ApplicationResponse | null>(null);
  protected readonly deleting = signal(false);

  protected readonly loading = computed(() => this.state() === 'loading');
  protected readonly error = computed(() => this.state() === 'error');
  protected readonly application = this._application.asReadonly();
  protected readonly isAdmin = computed(() => this.userService.currentUser()?.role === 'ADMIN');

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
    this.applicationService.get(this.applicationId).subscribe({
      next: (application) => {
        this._application.set(application);
        this.state.set('loaded');
      },
      error: () => this.state.set('error'),
    });
  }

  protected confirmDelete(): void {
    const application = this.application();
    if (!application || this.deleting()) {
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
    this.deleting.set(true);
    this.applicationService.delete(application.id).subscribe({
      next: () => {
        this.toast.success('Application deleted.');
        this.router.navigateByUrl('/applications');
      },
      error: (error: HttpErrorResponse) => {
        this.deleting.set(false);
        this.toast.error(describeApiError(error));
      },
    });
  }
}
