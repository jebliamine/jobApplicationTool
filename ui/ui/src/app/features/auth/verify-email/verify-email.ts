import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { LucideCircleAlert, LucideCircleCheck } from '@lucide/angular';
import { describeApiError } from '../../../core/http/describe-api-error';
import { AuthService } from '../../../core/auth/auth.service';

type VerifyState = 'verifying' | 'success' | 'error' | 'missing-token';

@Component({
  selector: 'app-verify-email',
  imports: [RouterLink, MatButtonModule, MatCardModule, MatProgressSpinnerModule, LucideCircleAlert, LucideCircleCheck],
  templateUrl: './verify-email.html',
  styleUrl: './verify-email.scss',
})
export class VerifyEmail {
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);

  protected readonly state = signal<VerifyState>('verifying');
  protected readonly errorMessage = signal<string | null>(null);

  constructor() {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (!token || !token.trim()) {
      this.state.set('missing-token');
      return;
    }

    this.authService.verifyEmail({ token }).subscribe({
      next: () => this.state.set('success'),
      error: (error: HttpErrorResponse) => {
        this.errorMessage.set(describeApiError(error));
        this.state.set('error');
      },
    });
  }
}
