import { Injectable, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

const SUCCESS_DURATION_MS = 4000;
const ERROR_DURATION_MS = 5000;

/**
 * Thin wrapper around MatSnackBar so every feature doesn't redeclare the
 * same "Dismiss" action and duration constants for success/error toasts.
 */
@Injectable({ providedIn: 'root' })
export class ToastService {
  private readonly snackBar = inject(MatSnackBar);

  success(message: string): void {
    this.snackBar.open(message, 'Dismiss', { duration: SUCCESS_DURATION_MS });
  }

  error(message: string): void {
    this.snackBar.open(message, 'Dismiss', { duration: ERROR_DURATION_MS });
  }
}
