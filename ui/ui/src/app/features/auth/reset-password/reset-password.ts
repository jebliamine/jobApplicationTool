import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { AbstractControl, FormControl, FormGroup, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { LucideCircleAlert, LucideCircleCheck } from '@lucide/angular';
import { finalize } from 'rxjs';
import { describeApiError } from '../../../core/http/describe-api-error';
import { AuthService } from '../../../core/auth/auth.service';

interface ResetPasswordForm {
  newPassword: FormControl<string>;
  confirmPassword: FormControl<string>;
}

function passwordsMatchValidator(control: AbstractControl): ValidationErrors | null {
  const group = control as FormGroup<ResetPasswordForm>;
  const { newPassword, confirmPassword } = group.controls;
  return newPassword.value === confirmPassword.value ? null : { passwordMismatch: true };
}

@Component({
  selector: 'app-reset-password',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    LucideCircleAlert,
    LucideCircleCheck,
  ],
  templateUrl: './reset-password.html',
  styleUrl: './reset-password.scss',
})
export class ResetPassword {
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  private readonly token = this.route.snapshot.queryParamMap.get('token');
  protected readonly hasToken = this.token !== null && this.token.trim().length > 0;

  protected readonly form = new FormGroup<ResetPasswordForm>(
    {
      newPassword: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(8)] }),
      confirmPassword: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    },
    { validators: passwordsMatchValidator },
  );

  protected readonly submitting = signal(false);
  protected readonly serverError = signal<string | null>(null);
  protected readonly done = signal(false);

  protected submit(): void {
    if (!this.hasToken || this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.serverError.set(null);

    this.authService
      .resetPassword({ token: this.token!, newPassword: this.form.controls.newPassword.value })
      .pipe(finalize(() => this.submitting.set(false)))
      .subscribe({
        next: () => this.done.set(true),
        error: (error: HttpErrorResponse) => this.serverError.set(describeApiError(error)),
      });
  }

  protected goToLogin(): void {
    this.router.navigateByUrl('/login');
  }
}
