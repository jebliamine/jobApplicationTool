import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import {
  LucideArrowLeft,
  LucideBriefcase,
  LucideCalendar,
  LucideCircleAlert,
  LucideFileText,
  LucideNotebookText,
  LucidePencil,
  LucideShieldCheck,
  LucideTrash2,
  LucideUser,
} from '@lucide/angular';
import { describeApiError } from '../../../core/http/describe-api-error';
import { UserService } from '../../../core/user/user.service';
import {
  ApplicationDeleteDialog,
  ApplicationDeleteDialogData,
} from '../application-delete-dialog/application-delete-dialog';
import { ApplicationResponse, ApplicationStatus } from '../application.models';
import { ApplicationService } from '../application.service';
import { APPLICATION_STATUS_LABELS, APPLICATION_STATUS_SEVERITY } from '../application-status';

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
    LucideNotebookText,
    LucidePencil,
    LucideShieldCheck,
    LucideTrash2,
    LucideUser,
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
  private readonly snackBar = inject(MatSnackBar);

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

  protected statusSeverity(status: ApplicationStatus): string {
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
    const ref = this.dialog.open<ApplicationDeleteDialog, ApplicationDeleteDialogData, boolean>(
      ApplicationDeleteDialog,
      {
        data: { jobTitle: application.job.title },
        width: '420px',
      },
    );

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
        this.snackBar.open('Application deleted.', 'Dismiss', { duration: 4000 });
        this.router.navigateByUrl('/applications');
      },
      error: (error: HttpErrorResponse) => {
        this.deleting.set(false);
        this.snackBar.open(describeApiError(error), 'Dismiss', { duration: 5000 });
      },
    });
  }
}
