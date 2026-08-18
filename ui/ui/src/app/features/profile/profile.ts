import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import {
  LucideCircleAlert,
  LucideMail,
  LucidePencil,
  LucideShieldCheck,
  LucideUser,
} from '@lucide/angular';
import { finalize } from 'rxjs';
import { describeProfileUpdateError } from '../../core/user/user-error';
import { UserService } from '../../core/user/user.service';

interface ProfileForm {
  fullName: FormControl<string>;
  email: FormControl<string>;
}

@Component({
  selector: 'app-profile',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    LucideCircleAlert,
    LucideMail,
    LucidePencil,
    LucideShieldCheck,
    LucideUser,
  ],
  templateUrl: './profile.html',
  styleUrl: './profile.scss',
})
export class Profile {
  protected readonly userService = inject(UserService);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly editing = signal(false);
  protected readonly submitting = signal(false);
  protected readonly saveError = signal<string | null>(null);

  protected readonly form = new FormGroup<ProfileForm>({
    fullName: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
  });

  protected retry(): void {
    this.userService.refresh();
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
          this.snackBar.open('Profile updated.', 'Dismiss', { duration: 4000 });
        },
        error: (error: HttpErrorResponse) => this.saveError.set(describeProfileUpdateError(error)),
      });
  }
}
