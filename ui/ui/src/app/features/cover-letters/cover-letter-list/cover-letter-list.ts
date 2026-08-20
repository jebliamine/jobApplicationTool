import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleChange, MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ToastService } from '../../../core/ui/toast.service';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import {
  LucideArchive,
  LucideArchiveRestore,
  LucideCircleAlert,
  LucideEye,
  LucideInbox,
  LucideMail,
  LucidePlus,
  LucideTrash2,
} from '@lucide/angular';
import { describeApiError } from '../../../core/http/describe-api-error';
import { UserService } from '../../../core/user/user.service';
import {
  ConfirmDialog,
  ConfirmDialogData,
} from '../../../shared/components/confirm-dialog/confirm-dialog';
import { CoverLetterResponse } from '../cover-letter.models';
import { CoverLetterService } from '../cover-letter.service';

type LoadState = 'loading' | 'loaded' | 'error';

@Component({
  selector: 'app-cover-letter-list',
  imports: [
    DatePipe,
    RouterLink,
    MatButtonModule,
    MatButtonToggleModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatTableModule,
    MatTooltipModule,
    LucideArchive,
    LucideArchiveRestore,
    LucideCircleAlert,
    LucideEye,
    LucideInbox,
    LucideMail,
    LucidePlus,
    LucideTrash2,
  ],
  templateUrl: './cover-letter-list.html',
  styleUrl: './cover-letter-list.scss',
})
export class CoverLetterList {
  private readonly coverLetterService = inject(CoverLetterService);
  private readonly userService = inject(UserService);
  private readonly dialog = inject(MatDialog);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  private readonly state = signal<LoadState>('loading');
  private readonly _coverLetters = signal<CoverLetterResponse[]>([]);
  protected readonly showArchived = signal(false);
  protected readonly togglingId = signal<string | null>(null);
  protected readonly deletingId = signal<string | null>(null);

  protected readonly loading = computed(() => this.state() === 'loading');
  protected readonly error = computed(() => this.state() === 'error');
  protected readonly coverLetters = this._coverLetters.asReadonly();
  protected readonly isAdmin = computed(() => this.userService.currentUser()?.role === 'ADMIN');

  protected readonly displayedColumns = computed(() => {
    const base = ['job', 'company', 'updatedAt'];
    return [...base, ...(this.isAdmin() ? ['owner'] : []), 'actions'];
  });

  constructor() {
    this.load();
  }

  protected onViewChange(change: MatButtonToggleChange): void {
    this.showArchived.set(change.value === 'archived');
    this.load();
  }

  protected load(): void {
    this.state.set('loading');
    this.coverLetterService.list(this.showArchived()).subscribe({
      next: (coverLetters) => {
        this._coverLetters.set(coverLetters);
        this.state.set('loaded');
      },
      error: () => this.state.set('error'),
    });
  }

  protected viewCoverLetter(coverLetter: CoverLetterResponse): void {
    this.router.navigateByUrl(`/cover-letters/${coverLetter.id}`);
  }

  protected archiveCoverLetter(coverLetter: CoverLetterResponse): void {
    if (this.togglingId()) {
      return;
    }
    this.togglingId.set(coverLetter.id);
    this.coverLetterService.archive(coverLetter.id).subscribe({
      next: () => {
        this._coverLetters.update((current) => current.filter((c) => c.id !== coverLetter.id));
        this.togglingId.set(null);
        this.toast.success('Cover letter archived.');
      },
      error: (error: HttpErrorResponse) => {
        this.togglingId.set(null);
        this.toast.error(describeApiError(error));
      },
    });
  }

  protected unarchiveCoverLetter(coverLetter: CoverLetterResponse): void {
    if (this.togglingId()) {
      return;
    }
    this.togglingId.set(coverLetter.id);
    this.coverLetterService.unarchive(coverLetter.id).subscribe({
      next: () => {
        this._coverLetters.update((current) => current.filter((c) => c.id !== coverLetter.id));
        this.togglingId.set(null);
        this.toast.success('Cover letter restored.');
      },
      error: (error: HttpErrorResponse) => {
        this.togglingId.set(null);
        this.toast.error(describeApiError(error));
      },
    });
  }

  protected confirmDelete(coverLetter: CoverLetterResponse): void {
    if (this.deletingId()) {
      return;
    }
    const ref = this.dialog.open<ConfirmDialog, ConfirmDialogData, boolean>(ConfirmDialog, {
      data: {
        title: 'Delete this cover letter permanently?',
        message: `This will permanently delete the cover letter for "${coverLetter.job.title}". This action cannot be undone.`,
        confirmLabel: 'Delete permanently',
      },
      width: '420px',
    });

    ref.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.performDelete(coverLetter);
      }
    });
  }

  private performDelete(coverLetter: CoverLetterResponse): void {
    this.deletingId.set(coverLetter.id);
    this.coverLetterService.delete(coverLetter.id).subscribe({
      next: () => {
        this._coverLetters.update((current) => current.filter((c) => c.id !== coverLetter.id));
        this.deletingId.set(null);
        this.toast.success('Cover letter permanently deleted.');
      },
      error: (error: HttpErrorResponse) => {
        this.deletingId.set(null);
        this.toast.error(describeApiError(error));
      },
    });
  }
}
