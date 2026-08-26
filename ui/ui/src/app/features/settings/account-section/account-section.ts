import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ToastService } from '../../../core/ui/toast.service';
import {
  LucideCircleAlert,
  LucideMail,
  LucidePencil,
  LucideShieldCheck,
  LucideUser,
} from '@lucide/angular';
import { TranslatePipe } from '@ngx-translate/core';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { describeProfileUpdateError } from '../../../core/user/user-error';
import { UserService } from '../../../core/user/user.service';

interface AccountForm {
  fullName: FormControl<string>;
  email: FormControl<string>;
}

@Component({
  selector: 'app-account-section',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    TranslatePipe,
    LucideCircleAlert,
    LucideMail,
    LucidePencil,
    LucideShieldCheck,
    LucideUser,
  ],
  templateUrl: './account-section.html',
  styleUrl: './account-section.scss',
})
export class AccountSection {
  protected readonly userService = inject(UserService);
  private readonly authService = inject(AuthService);
  private readonly toast = inject(ToastService);

  protected readonly editing = signal(false);
  protected readonly submitting = signal(false);
  protected readonly saveError = signal<string | null>(null);
  protected readonly resendingVerification = signal(false);

  protected readonly form = new FormGroup<AccountForm>({
    fullName: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
  });

  protected retry(): void {
    this.userService.refresh();
  }

  protected resendVerification(): void {
    const user = this.userService.currentUser();
    if (!user || this.resendingVerification()) {
      return;
    }

    this.resendingVerification.set(true);
    this.authService
      .resendVerification({ email: user.email })
      .pipe(finalize(() => this.resendingVerification.set(false)))
      .subscribe({
        // Always resolves regardless of whether the email exists/is already verified.
        next: () => this.toast.success('Verification email sent.'),
        error: () => this.toast.error('Something went wrong. Please try again.'),
      });
  }

  protected startEdit(): void {
    const user = this.userService.currentUser();
    if (!user) {
      return;
    }
    this.form.setValue({ fullName: user.fullName, email: user.email });
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

    this.userService
      .updateProfile(this.form.getRawValue())
      .pipe(finalize(() => this.submitting.set(false)))
      .subscribe({
        next: () => {
          this.editing.set(false);
          this.toast.success('Profile updated.');
        },
        error: (error: HttpErrorResponse) => this.saveError.set(describeProfileUpdateError(error)),
      });
  }
}
