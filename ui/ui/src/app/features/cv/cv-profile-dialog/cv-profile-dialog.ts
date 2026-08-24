import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { LucideCircleAlert, LucideSparkles } from '@lucide/angular';
import { finalize } from 'rxjs';
import { describeApiError } from '../../../core/http/describe-api-error';
import { CvProfileResponse, CvResponse, ProfileGenerationStatus } from '../cv.models';
import { CvService } from '../cv.service';
import { StatusBadge, StatusBadgeSeverity } from '../../../shared/components/status-badge/status-badge';

export interface CvProfileDialogData {
  cv: CvResponse;
}

const STATUS_LABELS: Record<ProfileGenerationStatus, string> = {
  NOT_ATTEMPTED: 'Not generated yet',
  IN_PROGRESS: 'Generating…',
  COMPLETED: 'Generated',
  FAILED: 'Generation failed',
};

const STATUS_SEVERITIES: Record<ProfileGenerationStatus, StatusBadgeSeverity> = {
  NOT_ATTEMPTED: 'neutral',
  IN_PROGRESS: 'info',
  COMPLETED: 'success',
  FAILED: 'error',
};

@Component({
  selector: 'app-cv-profile-dialog',
  imports: [DatePipe, MatButtonModule, MatDialogModule, MatProgressSpinnerModule, LucideCircleAlert, LucideSparkles, StatusBadge],
  templateUrl: './cv-profile-dialog.html',
  styleUrl: './cv-profile-dialog.scss',
})
export class CvProfileDialog {
  protected readonly data = inject<CvProfileDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<CvProfileDialog>);
  private readonly cvService = inject(CvService);

  protected readonly loading = signal(true);
  protected readonly generating = signal(false);
  protected readonly profile = signal<CvProfileResponse | null>(null);
  protected readonly loadError = signal<string | null>(null);
  protected readonly generateError = signal<string | null>(null);

  constructor() {
    this.cvService
      .getProfile(this.data.cv.id)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (profile) => this.profile.set(profile),
        error: () => this.loadError.set('We could not load this CV profile.'),
      });
  }

  protected statusLabel(status: ProfileGenerationStatus): string {
    return STATUS_LABELS[status];
  }

  protected statusSeverity(status: ProfileGenerationStatus): StatusBadgeSeverity {
    return STATUS_SEVERITIES[status];
  }

  protected generate(): void {
    if (this.generating()) {
      return;
    }
    this.generating.set(true);
    this.generateError.set(null);
    this.cvService
      .generateProfile(this.data.cv.id)
      .pipe(finalize(() => this.generating.set(false)))
      .subscribe({
        next: (profile) => this.profile.set(profile),
        error: (error: HttpErrorResponse) => this.generateError.set(describeApiError(error)),
      });
  }

  protected close(): void {
    this.dialogRef.close();
  }
}
