import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import {
  LucideArrowLeft,
  LucideBriefcase,
  LucideCircleAlert,
  LucideFileText,
  LucidePencil,
  LucideShieldCheck,
  LucideUser,
} from '@lucide/angular';
import { describeApiError } from '../../../core/http/describe-api-error';
import { UserService } from '../../../core/user/user.service';
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
    LucideArrowLeft,
    LucideBriefcase,
    LucideCircleAlert,
    LucideFileText,
    LucidePencil,
    LucideShieldCheck,
    LucideUser,
  ],
  templateUrl: './cover-letter-detail.html',
  styleUrl: './cover-letter-detail.scss',
})
export class CoverLetterDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly coverLetterService = inject(CoverLetterService);
  private readonly userService = inject(UserService);
  private readonly snackBar = inject(MatSnackBar);

  private readonly coverLetterId = this.route.snapshot.paramMap.get('id')!;

  private readonly state = signal<LoadState>('loading');
  private readonly _coverLetter = signal<CoverLetterResponse | null>(null);
  protected readonly editing = signal(false);
  protected readonly submitting = signal(false);
  protected readonly saveError = signal<string | null>(null);

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
}
