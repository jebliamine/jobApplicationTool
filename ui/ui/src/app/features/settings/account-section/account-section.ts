import { HttpErrorResponse } from '@angular/common/http';
import { Component, ElementRef, inject, signal, viewChild } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ToastService } from '../../../core/ui/toast.service';
import {
  LucideCamera,
  LucideCircleAlert,
  LucideMail,
  LucidePencil,
  LucideShieldCheck,
  LucideTrash2,
  LucideUser,
} from '@lucide/angular';
import { TranslatePipe } from '@ngx-translate/core';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { describeProfileUpdateError } from '../../../core/user/user-error';
import { UserService } from '../../../core/user/user.service';

const ALLOWED_AVATAR_TYPES = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];
const MAX_AVATAR_SIZE_BYTES = 5 * 1024 * 1024;

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
    LucideCamera,
    LucideCircleAlert,
    LucideMail,
    LucidePencil,
    LucideShieldCheck,
    LucideTrash2,
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

  protected readonly avatarObjectUrl = this.userService.avatarObjectUrl;
  protected readonly uploadingAvatar = signal(false);
  protected readonly removingAvatar = signal(false);
  protected readonly avatarError = signal<string | null>(null);
  private readonly avatarFileInput = viewChild<ElementRef<HTMLInputElement>>('avatarFileInput');

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

  protected triggerAvatarUpload(): void {
    this.avatarFileInput()?.nativeElement.click();
  }

  protected onAvatarFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    input.value = '';
    if (!file || this.uploadingAvatar()) {
      return;
    }

    const validationError = this.validateAvatarFile(file);
    if (validationError) {
      this.avatarError.set(validationError);
      return;
    }

    this.avatarError.set(null);
    this.uploadingAvatar.set(true);
    this.userService
      .uploadAvatar(file)
      .pipe(finalize(() => this.uploadingAvatar.set(false)))
      .subscribe({
        next: () => this.toast.success('Profile photo updated.'),
        error: (error: HttpErrorResponse) => this.avatarError.set(describeProfileUpdateError(error)),
      });
  }

  protected removeAvatar(): void {
    if (this.removingAvatar()) {
      return;
    }
    this.removingAvatar.set(true);
    this.userService
      .deleteAvatar()
      .pipe(finalize(() => this.removingAvatar.set(false)))
      .subscribe({
        next: () => this.toast.success('Profile photo removed.'),
        error: (error: HttpErrorResponse) => this.avatarError.set(describeProfileUpdateError(error)),
      });
  }

  private validateAvatarFile(file: File): string | null {
    if (file.size === 0) {
      return 'The selected file is empty.';
    }
    if (file.size > MAX_AVATAR_SIZE_BYTES) {
      return 'Image exceeds the 5 MB limit.';
    }
    if (!ALLOWED_AVATAR_TYPES.includes(file.type)) {
      return 'Unsupported image type. Allowed formats: JPEG, PNG, WEBP, GIF.';
    }
    return null;
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
