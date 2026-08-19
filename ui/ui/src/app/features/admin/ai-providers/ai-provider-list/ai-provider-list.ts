import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { LucideCircleAlert, LucideSettings } from '@lucide/angular';
import { finalize } from 'rxjs';
import { describeApiError } from '../../../../core/http/describe-api-error';
import { AiProviderForm, AiProviderFormData } from '../ai-provider-form/ai-provider-form';
import { AdminAiProviderResponse, AiProviderUpdateRequest } from '../ai-provider.models';
import { AdminAiProviderService } from '../ai-provider.service';

@Component({
  selector: 'app-ai-provider-list',
  imports: [MatButtonModule, MatCardModule, MatProgressSpinnerModule, LucideCircleAlert, LucideSettings],
  templateUrl: './ai-provider-list.html',
  styleUrl: './ai-provider-list.scss',
})
export class AiProviderList {
  private readonly adminAiProviderService = inject(AdminAiProviderService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly providers = signal<AdminAiProviderResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly serverError = signal<string | null>(null);
  protected readonly testingProvider = signal<string | null>(null);

  constructor() {
    this.load();
  }

  protected load(): void {
    this.loading.set(true);
    this.serverError.set(null);
    this.adminAiProviderService
      .list()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (providers) => this.providers.set(providers),
        error: () => this.serverError.set('We could not load AI provider settings. Please try again.'),
      });
  }

  protected configure(provider: AdminAiProviderResponse): void {
    const ref = this.dialog.open<AiProviderForm, AiProviderFormData, AiProviderUpdateRequest | undefined>(
      AiProviderForm,
      { data: { provider }, width: '480px' },
    );

    ref.afterClosed().subscribe((request) => {
      if (!request) {
        return;
      }
      this.adminAiProviderService.update(provider.provider, request).subscribe({
        next: (updated) => {
          this.providers.update((current) => current.map((p) => (p.provider === updated.provider ? updated : p)));
          this.snackBar.open(`${updated.displayName} updated.`, 'Dismiss', { duration: 4000 });
        },
        error: (error: HttpErrorResponse) => this.snackBar.open(describeApiError(error), 'Dismiss', { duration: 5000 }),
      });
    });
  }

  protected testConnection(provider: AdminAiProviderResponse): void {
    if (this.testingProvider()) {
      return;
    }
    this.testingProvider.set(provider.provider);
    this.adminAiProviderService
      .test(provider.provider)
      .pipe(finalize(() => this.testingProvider.set(null)))
      .subscribe({
        next: (result) => {
          const message = result.message ?? (result.success ? 'Connection successful.' : 'Connection failed.');
          this.snackBar.open(message, 'Dismiss', { duration: 5000 });
        },
        error: (error: HttpErrorResponse) => this.snackBar.open(describeApiError(error), 'Dismiss', { duration: 5000 }),
      });
  }
}
