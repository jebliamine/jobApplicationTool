import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule, MatSlideToggleChange } from '@angular/material/slide-toggle';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { LucideCircleAlert, LucideInbox, LucidePlus, LucideTrash2 } from '@lucide/angular';
import { finalize } from 'rxjs';
import { describeApiError } from '../../../core/http/describe-api-error';
import { ToastService } from '../../../core/ui/toast.service';
import { UserService } from '../../../core/user/user.service';
import {
  ConfirmDialog,
  ConfirmDialogData,
} from '../../../shared/components/confirm-dialog/confirm-dialog';
import { AdminUserResponse } from './user-management.models';
import { UserManagementService } from './user-management.service';
import { UserCreateDialog } from './user-create-dialog/user-create-dialog';

type LoadState = 'loading' | 'loaded' | 'error';

@Component({
  selector: 'app-user-management',
  imports: [
    DatePipe,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatTableModule,
    MatTooltipModule,
    LucideCircleAlert,
    LucideInbox,
    LucidePlus,
    LucideTrash2,
  ],
  templateUrl: './user-management.html',
  styleUrl: './user-management.scss',
})
export class UserManagement {
  private readonly userManagementService = inject(UserManagementService);
  private readonly currentUserService = inject(UserService);
  private readonly toast = inject(ToastService);
  private readonly dialog = inject(MatDialog);

  private readonly state = signal<LoadState>('loading');
  private readonly _users = signal<AdminUserResponse[]>([]);
  protected readonly updatingId = signal<string | null>(null);
  protected readonly deletingId = signal<string | null>(null);

  protected readonly loading = computed(() => this.state() === 'loading');
  protected readonly error = computed(() => this.state() === 'error');
  protected readonly users = this._users.asReadonly();
  protected readonly displayedColumns = ['fullName', 'email', 'role', 'enabled', 'createdAt', 'actions'];

  constructor() {
    this.load();
  }

  protected load(): void {
    this.state.set('loading');
    this.userManagementService.list().subscribe({
      next: (users) => {
        this._users.set(users);
        this.state.set('loaded');
      },
      error: () => this.state.set('error'),
    });
  }

  /** The row for the currently signed-in admin — role/enabled controls are disabled here, matching backend self-protection. */
  protected isSelf(user: AdminUserResponse): boolean {
    return user.email === this.currentUserService.currentUser()?.email;
  }

  protected changeRole(user: AdminUserResponse, role: 'USER' | 'ADMIN'): void {
    if (role === user.role || this.updatingId()) {
      return;
    }
    this.updatingId.set(user.id);
    this.userManagementService
      .updateRole(user.id, role)
      .pipe(finalize(() => this.updatingId.set(null)))
      .subscribe({
        next: (updated) => this.replace(updated),
        error: (error: HttpErrorResponse) => this.toast.error(describeApiError(error)),
      });
  }

  protected toggleEnabled(user: AdminUserResponse, event: MatSlideToggleChange): void {
    if (this.updatingId()) {
      event.source.checked = user.enabled;
      return;
    }
    const enabled = event.checked;
    this.updatingId.set(user.id);
    this.userManagementService
      .updateEnabled(user.id, enabled)
      .pipe(finalize(() => this.updatingId.set(null)))
      .subscribe({
        next: (updated) => this.replace(updated),
        error: (error: HttpErrorResponse) => {
          event.source.checked = user.enabled;
          this.toast.error(describeApiError(error));
        },
      });
  }

  protected addUser(): void {
    const ref = this.dialog.open<UserCreateDialog, undefined, AdminUserResponse | null>(UserCreateDialog, {
      width: '420px',
    });

    ref.afterClosed().subscribe((created) => {
      if (created) {
        this._users.update((current) => [...current, created]);
        this.toast.success('User created.');
      }
    });
  }

  protected confirmDelete(user: AdminUserResponse): void {
    if (this.deletingId()) {
      return;
    }
    const ref = this.dialog.open<ConfirmDialog, ConfirmDialogData, boolean>(ConfirmDialog, {
      data: {
        title: 'Delete user?',
        message: `Are you sure you want to delete "${user.email}"? This is only possible for users with no jobs, applications, or other data.`,
      },
      width: '420px',
    });

    ref.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.performDelete(user);
      }
    });
  }

  private performDelete(user: AdminUserResponse): void {
    this.deletingId.set(user.id);
    this.userManagementService
      .delete(user.id)
      .pipe(finalize(() => this.deletingId.set(null)))
      .subscribe({
        next: () => {
          this._users.update((current) => current.filter((u) => u.id !== user.id));
          this.toast.success('User deleted.');
        },
        error: (error: HttpErrorResponse) => this.toast.error(describeApiError(error)),
      });
  }

  private replace(updated: AdminUserResponse): void {
    this._users.update((current) => current.map((u) => (u.id === updated.id ? updated : u)));
  }
}
