import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { LucideCircleAlert, LucideMailCheck } from '@lucide/angular';
import { finalize } from 'rxjs';
import { describeApiError } from '../../../core/http/describe-api-error';
import { AuthService } from '../../../core/auth/auth.service';

interface ForgotPasswordForm {
  email: FormControl<string>;
}

@Component({
  selector: 'app-forgot-password',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    LucideCircleAlert,
    LucideMailCheck,
  ],
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.scss',
})
export class ForgotPassword {
  private readonly authService = inject(AuthService);

  protected readonly form = new FormGroup<ForgotPasswordForm>({
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
  });

  protected readonly submitting = signal(false);
  protected readonly serverError = signal<string | null>(null);
  protected readonly sent = signal(false);

  protected submit(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.serverError.set(null);

    this.authService
      .forgotPassword(this.form.getRawValue())
      .pipe(finalize(() => this.submitting.set(false)))
      .subscribe({
        // The backend always responds the same way regardless of whether the email exists, to
        // avoid letting this endpoint reveal which addresses have an account.
        next: () => this.sent.set(true),
        error: (error: HttpErrorResponse) => this.serverError.set(describeApiError(error)),
      });
  }
}
