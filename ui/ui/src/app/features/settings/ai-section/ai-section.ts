import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { LucideCircleAlert, LucideSparkles } from '@lucide/angular';
import { TranslatePipe } from '@ngx-translate/core';
import { AiProviderService } from '../../cover-letters/ai-provider.service';
import { AiProviderResponse } from '../../cover-letters/ai-provider.models';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';
import { UserService } from '../../../core/user/user.service';

type LoadState = 'loading' | 'loaded' | 'error';

@Component({
  selector: 'app-ai-section',
  imports: [
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatProgressSpinnerModule,
    TranslatePipe,
    LucideCircleAlert,
    LucideSparkles,
    StatusBadge,
  ],
  templateUrl: './ai-section.html',
  styleUrl: './ai-section.scss',
})
export class AiSection {
  private readonly aiProviderService = inject(AiProviderService);
  private readonly userService = inject(UserService);

  private readonly state = signal<LoadState>('loading');
  private readonly _providers = signal<AiProviderResponse[]>([]);

  protected readonly loading = computed(() => this.state() === 'loading');
  protected readonly error = computed(() => this.state() === 'error');
  protected readonly providers = this._providers.asReadonly();
  protected readonly isAdmin = computed(() => this.userService.currentUser()?.role === 'ADMIN');

  constructor() {
    this.load();
  }

  protected load(): void {
    this.state.set('loading');
    this.aiProviderService.list().subscribe({
      next: (providers) => {
        this._providers.set(providers);
        this.state.set('loaded');
      },
      error: () => this.state.set('error'),
    });
  }
}
