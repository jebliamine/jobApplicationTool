import { Component, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import {
  LucideBriefcase,
  LucideCircleAlert,
  LucideFileText,
  LucideMail,
  LucideSend,
  LucideSparkles,
  LucideUsers,
} from '@lucide/angular';
import { TranslatePipe } from '@ngx-translate/core';
import { UserService } from '../../core/user/user.service';
import { StatCard } from './components/stat-card/stat-card';
import { DashboardResponse } from './dashboard.models';
import { DashboardService } from './dashboard.service';

type LoadState = 'loading' | 'loaded' | 'error';

@Component({
  selector: 'app-dashboard',
  imports: [
    MatButtonModule,
    MatProgressSpinnerModule,
    TranslatePipe,
    StatCard,
    LucideBriefcase,
    LucideCircleAlert,
    LucideFileText,
    LucideMail,
    LucideSend,
    LucideSparkles,
    LucideUsers,
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard {
  private readonly dashboardService = inject(DashboardService);
  private readonly userService = inject(UserService);

  private readonly state = signal<LoadState>('loading');
  private readonly _dashboard = signal<DashboardResponse | null>(null);

  protected readonly loading = computed(() => this.state() === 'loading');
  protected readonly error = computed(() => this.state() === 'error');
  protected readonly dashboard = this._dashboard.asReadonly();
  protected readonly isAdmin = computed(() => this.userService.currentUser()?.role === 'ADMIN');

  constructor() {
    this.load();
  }

  protected load(): void {
    this.state.set('loading');
    this.dashboardService.get().subscribe({
      next: (dashboard) => {
        this._dashboard.set(dashboard);
        this.state.set('loaded');
      },
      error: () => this.state.set('error'),
    });
  }
}
