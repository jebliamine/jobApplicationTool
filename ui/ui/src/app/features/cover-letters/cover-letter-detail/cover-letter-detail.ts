import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import {
  LucideArchive,
  LucideArchiveRestore,
  LucideArrowLeft,
  LucideBriefcase,
  LucideCircleAlert,
  LucideFileText,
  LucidePencil,
  LucideShieldCheck,
  LucideTrash2,
  LucideUser,
} from '@lucide/angular';
import { describeApiError } from '../../../core/http/describe-api-error';
import { UserService } from '../../../core/user/user.service';
import {
  CoverLetterDeleteDialog,
  CoverLetterDeleteDialogData,
} from '../cover-letter-delete-dialog/cover-letter-delete-dialog';
import { CoverLetterResponse } from '../cover-letter.models';
import { CoverLetterService } from '../cover-letter.service';

type LoadState = 'loading' | 'loaded' | 'error';

interface CoverLetterFormControls {
  resultText: FormControl<string>;
}

@Component({
  selector: 'app-cover-letter-detail',
  imports: [
    DatePipe,
    RouterLink,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    LucideArchive,
    LucideArchiveRestore,
    LucideArrowLeft,
    LucideBriefcase,
    LucideCircleAlert,
    LucideFileText,
    LucidePencil,
    LucideShieldCheck,
    LucideTrash2,
    LucideUser,
  ],
  templateUrl: './cover-letter-detail.html',
  styleUrl: './cover-letter-detail.scss',
})
export class CoverLetterDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly coverLetterService = inject(CoverLetterService);
  private readonly userService = inject(UserService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  private readonly coverLetterId = this.route.snapshot.paramMap.get('id')!;

  private readonly state = signal<LoadState>('loading');
  private readonly _coverLetter = signal<CoverLetterResponse | null>(null);
  protected readonly editing = signal(false);
  protected readonly submitting = signal(false);
  protected readonly saveError = signal<string | null>(null);
  protected readonly toggling = signal(false);
  protected readonly deleting = signal(false);

  protected readonly loading = computed(() => this.state() === 'loading');
  protected readonly error = computed(() => this.state() === 'error');
  protected readonly coverLetter = this._coverLetter.asReadonly();
  protected readonly isAdmin = computed(() => this.userService.currentUser()?.role === 'ADMIN');

  protected readonly form = new FormGroup<CoverLetterFormControls>({
    resultText: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  constructor() {
    this.load();
  }

  protected load(): void {
    this.state.set('loading');
    this.coverLetterService.get(this.coverLetterId).subscribe({
      next: (coverLetter) => {
        this._coverLetter.set(coverLetter);
        this.state.set('loaded');
      },
      error: () => this.state.set('error'),
    });
  }

  protected startEdit(): void {
    const coverLetter = this.coverLetter();
    if (!coverLetter) {
      return;
    }
    this.form.setValue({ resultText: coverLetter.resultText });
    this.saveError.set(null);
    this.editing.set(true);
  }

  protected cancelEdit(): void {
    this.editing.set(false);
    this.saveError.set(null);
  }

  protected save(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.saveError.set(null);

    this.coverLetterService
      .update(this.coverLetterId, { resultText: this.form.getRawValue().resultText })
      .subscribe({
        next: (coverLetter) => {
          this._coverLetter.set(coverLetter);
          this.submitting.set(false);
          this.editing.set(false);
          this.snackBar.open('Cover letter updated.', 'Dismiss', { duration: 4000 });
        },
        error: (error: HttpErrorResponse) => {
          this.submitting.set(false);
          this.saveError.set(describeApiError(error));
        },
      });
  }

  protected archive(): void {
    if (this.toggling()) {
      return;
    }
    this.toggling.set(true);
    this.coverLetterService.archive(this.coverLetterId).subscribe({
      next: (coverLetter) => {
        this._coverLetter.set(coverLetter);
        this.toggling.set(false);
        this.snackBar.open('Cover letter archived.', 'Dismiss', { duration: 4000 });
      },
      error: (error: HttpErrorResponse) => {
        this.toggling.set(false);
        this.snackBar.open(describeApiError(error), 'Dismiss', { duration: 5000 });
      },
    });
  }

  protected unarchive(): void {
    if (this.toggling()) {
      return;
    }
    this.toggling.set(true);
    this.coverLetterService.unarchive(this.coverLetterId).subscribe({
      next: (coverLetter) => {
        this._coverLetter.set(coverLetter);
        this.toggling.set(false);
        this.snackBar.open('Cover letter restored.', 'Dismiss', { duration: 4000 });
      },
      error: (error: HttpErrorResponse) => {
        this.toggling.set(false);
        this.snackBar.open(describeApiError(error), 'Dismiss', { duration: 5000 });
      },
    });
  }

  protected confirmDelete(): void {
    const coverLetter = this.coverLetter();
    if (!coverLetter || this.deleting()) {
      return;
    }
    const ref = this.dialog.open<CoverLetterDeleteDialog, CoverLetterDeleteDialogData, boolean>(
      CoverLetterDeleteDialog,
      {
        data: { jobTitle: coverLetter.job.title },
        width: '420px',
      },
    );

    ref.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.performDelete(coverLetter);
      }
    });
  }

  private performDelete(coverLetter: CoverLetterResponse): void {
    this.deleting.set(true);
    this.coverLetterService.delete(coverLetter.id).subscribe({
      next: () => {
        this.snackBar.open('Cover letter permanently deleted.', 'Dismiss', { duration: 4000 });
        this.router.navigateByUrl('/cover-letters');
      },
      error: (error: HttpErrorResponse) => {
        this.deleting.set(false);
        this.snackBar.open(describeApiError(error), 'Dismiss', { duration: 5000 });
      },
    });
  }
}
