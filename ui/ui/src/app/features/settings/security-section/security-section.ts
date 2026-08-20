import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { LucideCircleAlert } from '@lucide/angular';
import { finalize } from 'rxjs';
import { describeChangePasswordError } from '../../../core/user/user-error';
import { UserService } from '../../../core/user/user.service';
import { ToastService } from '../../../core/ui/toast.service';

interface SecurityForm {
  currentPassword: FormControl<string>;
  newPassword: FormControl<string>;
  confirmNewPassword: FormControl<string>;
}

function passwordsMatchValidator(group: AbstractControl): ValidationErrors | null {
  const newPassword = group.get('newPassword')?.value;
  const confirmNewPassword = group.get('confirmNewPassword')?.value;
  return newPassword === confirmNewPassword ? null : { passwordMismatch: true };
}

@Component({
  selector: 'app-security-section',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    LucideCircleAlert,
  ],
  templateUrl: './security-section.html',
  styleUrl: './security-section.scss',
})
export class SecuritySection {
  private readonly userService = inject(UserService);
  private readonly toast = inject(ToastService);

  protected readonly submitting = signal(false);
  protected readonly saveError = signal<string | null>(null);

  protected readonly form = new FormGroup<SecurityForm>(
    {
      currentPassword: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
      newPassword: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.minLength(8)],
      }),
      confirmNewPassword: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    },
    { validators: passwordsMatchValidator },
  );

  protected save(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.saveError.set(null);

    const { currentPassword, newPassword } = this.form.getRawValue();

    this.userService
      .changePassword({ currentPassword, newPassword })
      .pipe(finalize(() => this.submitting.set(false)))
      .subscribe({
        next: () => {
          this.form.reset({ currentPassword: '', newPassword: '', confirmNewPassword: '' });
          this.toast.success('Password changed.');
        },
        error: (error: HttpErrorResponse) => this.saveError.set(describeChangePasswordError(error)),
      });
  }
}
