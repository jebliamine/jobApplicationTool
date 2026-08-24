import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { LucideCircleAlert } from '@lucide/angular';
import { finalize } from 'rxjs';
import { describeApiError } from '../../../../core/http/describe-api-error';
import { AdminUserResponse } from '../user-management.models';
import { UserManagementService } from '../user-management.service';

interface UserCreateForm {
  fullName: FormControl<string>;
  email: FormControl<string>;
  password: FormControl<string>;
  role: FormControl<'USER' | 'ADMIN'>;
}

@Component({
  selector: 'app-user-create-dialog',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    LucideCircleAlert,
  ],
  templateUrl: './user-create-dialog.html',
  styleUrl: './user-create-dialog.scss',
})
export class UserCreateDialog {
  private readonly userManagementService = inject(UserManagementService);
  private readonly dialogRef = inject(MatDialogRef<UserCreateDialog, AdminUserResponse | null>);

  protected readonly form = new FormGroup<UserCreateForm>({
    fullName: new FormControl('', { nonNullable: true }),
    email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(8)] }),
    role: new FormControl('USER', { nonNullable: true, validators: [Validators.required] }),
  });

  protected readonly submitting = signal(false);
  protected readonly serverError = signal<string | null>(null);

  protected cancel(): void {
    this.dialogRef.close(null);
  }

  protected submit(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.serverError.set(null);

    const raw = this.form.getRawValue();
    this.userManagementService
      .create({
        fullName: raw.fullName,
        email: raw.email,
        password: raw.password,
        role: raw.role,
      })
      .pipe(finalize(() => this.submitting.set(false)))
      .subscribe({
        next: (user) => this.dialogRef.close(user),
        error: (error: HttpErrorResponse) => this.serverError.set(describeApiError(error)),
      });
  }
}
