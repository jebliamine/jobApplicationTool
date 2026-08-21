import { Component, computed, input } from '@angular/core';
import {
  APPLICATION_STATUSES,
  ApplicationStatus,
} from '../../../applications/application.models';
import {
  APPLICATION_STATUS_LABELS,
  APPLICATION_STATUS_SEVERITY,
  ApplicationStatusSeverity,
} from '../../../applications/application-status';

interface StatusBar {
  status: ApplicationStatus;
  label: string;
  severity: ApplicationStatusSeverity;
  count: number;
  widthPercent: number;
}

/**
 * Hand-rolled horizontal bar chart (no charting library — see the Dashboard
 * v2 phase notes: ngx-charts hit a real npm peer conflict against this
 * project's exact Angular version, and this chart is simple enough not to
 * need one). Bars reuse the same severity → color mapping as StatusBadge, so
 * the chart and the status badges elsewhere agree visually.
 */
@Component({
  selector: 'app-status-distribution-chart',
  templateUrl: './status-distribution-chart.html',
  styleUrl: './status-distribution-chart.scss',
})
export class StatusDistributionChart {
  readonly counts = input.required<Record<ApplicationStatus, number>>();

  protected readonly bars = computed<StatusBar[]>(() => {
    const counts = this.counts();
    const max = Math.max(1, ...APPLICATION_STATUSES.map((status) => counts[status] ?? 0));
    return APPLICATION_STATUSES.map((status) => {
      const count = counts[status] ?? 0;
      return {
        status,
        label: APPLICATION_STATUS_LABELS[status],
        severity: APPLICATION_STATUS_SEVERITY[status],
        count,
        widthPercent: (count / max) * 100,
      };
    });
  });

  protected readonly summary = computed(() =>
    this.bars()
      .map((bar) => `${bar.label}: ${bar.count}`)
      .join(', '),
  );
}
