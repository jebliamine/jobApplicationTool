import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import {
  LucideBriefcase,
  LucideCircleAlert,
  LucideFileText,
  LucideMail,
  LucideSend,
  LucideSparkles,
} from '@lucide/angular';
import { TranslatePipe } from '@ngx-translate/core';
import { forkJoin } from 'rxjs';
import { ActivityChart } from '../../shared/components/activity-chart/activity-chart';
import { StatCard } from '../../shared/components/stat-card/stat-card';
import { StatusDistributionChart } from '../../shared/components/status-distribution-chart/status-distribution-chart';
import { UserService } from '../../core/user/user.service';
import { ApplicationResponse } from '../applications/application.models';
import { ApplicationService } from '../applications/application.service';
import { RecentApplicationList } from './components/recent-application-list/recent-application-list';
import { WorkflowGuide } from './components/workflow-guide/workflow-guide';
import { DashboardResponse } from './dashboard.models';
import { DashboardService } from './dashboard.service';

type LoadState = 'loading' | 'loaded' | 'error';

const RECENT_APPLICATIONS_LIMIT = 5;

@Component({
  selector: 'app-dashboard',
  imports: [
    RouterLink,
    MatButtonModule,
    MatProgressSpinnerModule,
    TranslatePipe,
    ActivityChart,
    RecentApplicationList,
    StatCard,
    StatusDistributionChart,
    WorkflowGuide,
    LucideBriefcase,
    LucideCircleAlert,
    LucideFileText,
    LucideMail,
    LucideSend,
    LucideSparkles,
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard {
  private readonly dashboardService = inject(DashboardService);
  private readonly applicationService = inject(ApplicationService);
  private readonly userService = inject(UserService);

  private readonly state = signal<LoadState>('loading');
  private readonly _dashboard = signal<DashboardResponse | null>(null);
  private readonly _recentApplications = signal<ApplicationResponse[]>([]);

  protected readonly loading = computed(() => this.state() === 'loading');
  protected readonly error = computed(() => this.state() === 'error');
  protected readonly dashboard = this._dashboard.asReadonly();
  protected readonly recentApplications = this._recentApplications.asReadonly();

  protected readonly firstName = computed(() => {
    const user = this.userService.currentUser();
    const name = user?.fullName?.trim();
    return name ? name.split(' ')[0] : (user?.email ?? '');
  });

  /** A caller with zero data anywhere gets the getting-started guide instead of empty stat cards/charts. */
  protected readonly isNewUser = computed(() => {
    const data = this.dashboard();
    return !!data && data.cvCount === 0 && data.jobCount === 0 && data.applicationCount === 0;
  });

  protected readonly topCompanies = computed(() => this.dashboard()?.funnelMetrics.byCompany.slice(0, 5) ?? []);

  constructor() {
    this.load();
  }

  protected load(): void {
    this.state.set('loading');
    forkJoin({
      dashboard: this.dashboardService.get(),
      applications: this.applicationService.list(),
    }).subscribe({
      next: ({ dashboard, applications }) => {
        this._dashboard.set(dashboard);
        this._recentApplications.set(
          [...applications]
            .sort((a, b) => Date.parse(b.createdAt) - Date.parse(a.createdAt))
            .slice(0, RECENT_APPLICATIONS_LIMIT),
        );
        this.state.set('loaded');
      },
      error: () => this.state.set('error'),
    });
  }

  protected formatPercent(rate: number): string {
    return `${Math.round(rate * 100)}%`;
  }
}
