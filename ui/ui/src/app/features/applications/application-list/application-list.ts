import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import {
  LucideCircleAlert,
  LucideClipboardList,
  LucideEye,
  LucideInbox,
  LucidePencil,
  LucidePlus,
  LucideTrash2,
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
  selector: 'app-application-list',
  imports: [
    DatePipe,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatTableModule,
    MatTooltipModule,
    LucideCircleAlert,
    LucideClipboardList,
    LucideEye,
    LucideInbox,
    LucidePencil,
    LucidePlus,
    LucideTrash2,
  ],
  templateUrl: './application-list.html',
  styleUrl: './application-list.scss',
})
export class ApplicationList {
  private readonly applicationService = inject(ApplicationService);
  private readonly userService = inject(UserService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly router = inject(Router);

  private readonly state = signal<LoadState>('loading');
  private readonly _applications = signal<ApplicationResponse[]>([]);
  protected readonly deletingId = signal<string | null>(null);

  protected readonly loading = computed(() => this.state() === 'loading');
  protected readonly error = computed(() => this.state() === 'error');
  protected readonly applications = this._applications.asReadonly();
  protected readonly isAdmin = computed(() => this.userService.currentUser()?.role === 'ADMIN');

  protected readonly displayedColumns = computed(() => {
    const base = ['job', 'company', 'status', 'appliedAt', 'cv'];
    return [...base, ...(this.isAdmin() ? ['owner'] : []), 'actions'];
  });

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
    this.applicationService.list().subscribe({
      next: (applications) => {
        this._applications.set(applications);
        this.state.set('loaded');
      },
      error: () => this.state.set('error'),
    });
  }

  protected viewApplication(application: ApplicationResponse): void {
    this.router.navigateByUrl(`/applications/${application.id}`);
  }

  protected editApplication(application: ApplicationResponse): void {
    this.router.navigateByUrl(`/applications/${application.id}/edit`);
  }

  protected confirmDelete(application: ApplicationResponse): void {
    if (this.deletingId()) {
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
    this.deletingId.set(application.id);
    this.applicationService.delete(application.id).subscribe({
      next: () => {
        this._applications.update((current) => current.filter((a) => a.id !== application.id));
        this.deletingId.set(null);
        this.snackBar.open('Application deleted.', 'Dismiss', { duration: 4000 });
      },
      error: (error: HttpErrorResponse) => {
        this.deletingId.set(null);
        this.snackBar.open(describeApiError(error), 'Dismiss', { duration: 5000 });
      },
    });
  }
}
