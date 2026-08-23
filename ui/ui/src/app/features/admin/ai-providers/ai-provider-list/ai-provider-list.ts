import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ToastService } from '../../../../core/ui/toast.service';
import { LucideCircleAlert, LucidePlus, LucideSettings, LucideTrash2 } from '@lucide/angular';
import { finalize } from 'rxjs';
import { describeApiError } from '../../../../core/http/describe-api-error';
import {
  ConfirmDialog,
  ConfirmDialogData,
} from '../../../../shared/components/confirm-dialog/confirm-dialog';
import { AiProviderCreate } from '../ai-provider-create/ai-provider-create';
import { AiProviderForm, AiProviderFormData } from '../ai-provider-form/ai-provider-form';
import { AdminAiProviderResponse, AiProviderCreateRequest, AiProviderUpdateRequest } from '../ai-provider.models';
import { AdminAiProviderService } from '../ai-provider.service';

@Component({
  selector: 'app-ai-provider-list',
  imports: [
    MatButtonModule,
    MatCardModule,
    MatProgressSpinnerModule,
    LucideCircleAlert,
    LucidePlus,
    LucideSettings,
    LucideTrash2,
  ],
  templateUrl: './ai-provider-list.html',
  styleUrl: './ai-provider-list.scss',
})
export class AiProviderList {
  private readonly adminAiProviderService = inject(AdminAiProviderService);
  private readonly dialog = inject(MatDialog);
  private readonly toast = inject(ToastService);

  protected readonly providers = signal<AdminAiProviderResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly serverError = signal<string | null>(null);
  protected readonly testingProviderId = signal<string | null>(null);
  protected readonly deletingProviderId = signal<string | null>(null);

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

  protected addProvider(): void {
    const ref = this.dialog.open<AiProviderCreate, undefined, AiProviderCreateRequest | undefined>(
      AiProviderCreate,
      { width: '480px' },
    );

    ref.afterClosed().subscribe((request) => {
      if (!request) {
        return;
      }
      this.adminAiProviderService.create(request).subscribe({
        next: (created) => {
          this.providers.update((current) => [...current, created]);
          this.toast.success(`${created.displayName} added.`);
        },
        error: (error: HttpErrorResponse) => this.toast.error(describeApiError(error)),
      });
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
      this.adminAiProviderService.update(provider.id, request).subscribe({
        next: (updated) => {
          this.providers.update((current) => current.map((p) => (p.id === updated.id ? updated : p)));
          this.toast.success(`${updated.displayName} updated.`);
        },
        error: (error: HttpErrorResponse) => this.toast.error(describeApiError(error)),
      });
    });
  }

  protected deleteProvider(provider: AdminAiProviderResponse): void {
    if (this.deletingProviderId()) {
      return;
    }
    const ref = this.dialog.open<ConfirmDialog, ConfirmDialogData, boolean>(ConfirmDialog, {
      data: {
        title: 'Delete provider?',
        message: `Are you sure you want to delete "${provider.displayName}"? This action cannot be undone.`,
      },
      width: '420px',
    });

    ref.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.performDelete(provider);
      }
    });
  }

  private performDelete(provider: AdminAiProviderResponse): void {
    this.deletingProviderId.set(provider.id);
    this.adminAiProviderService
      .delete(provider.id)
      .pipe(finalize(() => this.deletingProviderId.set(null)))
      .subscribe({
        next: () => {
          this.providers.update((current) => current.filter((p) => p.id !== provider.id));
          this.toast.success(`${provider.displayName} deleted.`);
        },
        error: (error: HttpErrorResponse) => this.toast.error(describeApiError(error)),
      });
  }

  protected testConnection(provider: AdminAiProviderResponse): void {
    if (this.testingProviderId()) {
      return;
    }
    this.testingProviderId.set(provider.id);
    this.adminAiProviderService
      .test(provider.id)
      .pipe(finalize(() => this.testingProviderId.set(null)))
      .subscribe({
        next: (result) => {
          const message = result.message ?? (result.success ? 'Connection successful.' : 'Connection failed.');
          this.toast.error(message);
        },
        error: (error: HttpErrorResponse) => this.toast.error(describeApiError(error)),
      });
  }
}
