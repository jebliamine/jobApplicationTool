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
import { ActivityChart } from '../../../shared/components/activity-chart/activity-chart';
import { StatCard } from '../../../shared/components/stat-card/stat-card';
import { StatusDistributionChart } from '../../../shared/components/status-distribution-chart/status-distribution-chart';
import { DashboardResponse } from '../../dashboard/dashboard.models';
import { DashboardService } from '../../dashboard/dashboard.service';

type LoadState = 'loading' | 'loaded' | 'error';

/**
 * The operational, org-wide view — GET /dashboard already returns global
 * totals (not the admin's own data) whenever the caller is an ADMIN, so
 * this reuses the exact same DashboardService/DashboardResponse the
 * authenticated home uses, just presented for oversight rather than a
 * personal workspace. See dashboard.models.ts for the backend contract.
 */
@Component({
  selector: 'app-admin-dashboard',
  imports: [
    MatButtonModule,
    MatProgressSpinnerModule,
    TranslatePipe,
    ActivityChart,
    StatCard,
    StatusDistributionChart,
    LucideBriefcase,
    LucideCircleAlert,
    LucideFileText,
    LucideMail,
    LucideSend,
    LucideSparkles,
    LucideUsers,
  ],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.scss',
})
export class AdminDashboard {
  private readonly dashboardService = inject(DashboardService);

  private readonly state = signal<LoadState>('loading');
  private readonly _dashboard = signal<DashboardResponse | null>(null);

  protected readonly loading = computed(() => this.state() === 'loading');
  protected readonly error = computed(() => this.state() === 'error');
  protected readonly dashboard = this._dashboard.asReadonly();
  protected readonly topCompanies = computed(() => this.dashboard()?.funnelMetrics.byCompany.slice(0, 5) ?? []);

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

  protected formatPercent(rate: number): string {
    return `${Math.round(rate * 100)}%`;
  }
}
