import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, input, output, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ToastService } from '../../../core/ui/toast.service';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import {
  LucideCircleAlert,
  LucideDownload,
  LucideEye,
  LucideFileText,
  LucideInbox,
  LucideSparkles,
  LucideTrash2,
} from '@lucide/angular';
import { finalize } from 'rxjs';
import {
  ConfirmDialog,
  ConfirmDialogData,
} from '../../../shared/components/confirm-dialog/confirm-dialog';
import { CvProfileDialog, CvProfileDialogData } from '../cv-profile-dialog/cv-profile-dialog';
import { CvResponse } from '../cv.models';
import { CvService } from '../cv.service';

const CONTENT_TYPE_LABELS: Record<string, string> = {
  'application/pdf': 'PDF',
  'application/msword': 'DOC',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document': 'DOCX',
};

@Component({
  selector: 'app-cv-list',
  imports: [
    DatePipe,
    MatButtonModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatTableModule,
    MatTooltipModule,
    LucideCircleAlert,
    LucideDownload,
    LucideEye,
    LucideFileText,
    LucideInbox,
    LucideSparkles,
    LucideTrash2,
  ],
  templateUrl: './cv-list.html',
  styleUrl: './cv-list.scss',
})
export class CvList {
  private readonly cvService = inject(CvService);
  private readonly dialog = inject(MatDialog);
  private readonly toast = inject(ToastService);

  readonly cvs = input.required<CvResponse[]>();
  readonly loading = input(false);
  readonly error = input(false);
  readonly isAdmin = input(false);
  readonly retry = output<void>();
  readonly deleted = output<string>();

  protected readonly viewingId = signal<string | null>(null);
  protected readonly downloadingId = signal<string | null>(null);
  protected readonly deletingId = signal<string | null>(null);

  protected readonly displayedColumns = computed(() => {
    const base = ['title', 'fileName', 'contentType', 'size', 'createdAt'];
    return [...base, ...(this.isAdmin() ? ['owner'] : []), 'actions'];
  });

  protected formatSize(bytes: number): string {
    if (bytes < 1024) {
      return `${bytes} B`;
    }
    if (bytes < 1024 * 1024) {
      return `${(bytes / 1024).toFixed(1)} KB`;
    }
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  }

  protected formatType(contentType: string): string {
    return CONTENT_TYPE_LABELS[contentType] ?? contentType;
  }

  protected openProfile(cv: CvResponse): void {
    this.dialog.open<CvProfileDialog, CvProfileDialogData>(CvProfileDialog, {
      data: { cv },
      width: '480px',
    });
  }

  protected view(cv: CvResponse): void {
    if (this.viewingId()) {
      return;
    }
    this.viewingId.set(cv.id);
    this.cvService
      .view(cv.id)
      .pipe(finalize(() => this.viewingId.set(null)))
      .subscribe({
        next: (blob) => {
          const url = URL.createObjectURL(blob);
          window.open(url, '_blank', 'noopener');
          setTimeout(() => URL.revokeObjectURL(url), 60_000);
        },
        error: () => this.notifyActionFailure('open'),
      });
  }

  protected download(cv: CvResponse): void {
    if (this.downloadingId()) {
      return;
    }
    this.downloadingId.set(cv.id);
    this.cvService
      .download(cv.id)
      .pipe(finalize(() => this.downloadingId.set(null)))
      .subscribe({
        next: ({ blob, filename }) => {
          const url = URL.createObjectURL(blob);
          const anchor = document.createElement('a');
          anchor.href = url;
          anchor.download = filename;
          anchor.click();
          URL.revokeObjectURL(url);
        },
        error: () => this.notifyActionFailure('download'),
      });
  }

  protected confirmDelete(cv: CvResponse): void {
    if (this.deletingId()) {
      return;
    }
    const ref = this.dialog.open<ConfirmDialog, ConfirmDialogData, boolean>(ConfirmDialog, {
      data: {
        title: 'Delete CV?',
        message: `Are you sure you want to delete "${cv.fileName}"? This action cannot be undone.`,
      },
      width: '420px',
    });

    ref.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.performDelete(cv);
      }
    });
  }

  private performDelete(cv: CvResponse): void {
    this.deletingId.set(cv.id);
    this.cvService
      .delete(cv.id)
      .pipe(finalize(() => this.deletingId.set(null)))
      .subscribe({
        next: () => {
          this.deleted.emit(cv.id);
          this.toast.success('CV deleted.');
        },
        error: (error: HttpErrorResponse) => {
          const message = typeof error.error?.message === 'string' ? error.error.message : null;
          this.toast.error(message ?? 'We could not delete this CV. Please try again.');
        },
      });
  }

  private notifyActionFailure(action: 'open' | 'download'): void {
    const message =
      action === 'open'
        ? 'We could not open this CV. Please try again.'
        : 'We could not download this CV. Please try again.';
    this.toast.error(message);
  }
}
