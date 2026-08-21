import { DatePipe } from '@angular/common';
import { Component, computed, input } from '@angular/core';

interface WeekBar {
  weekStart: Date;
  count: number;
  heightPercent: number;
}

/**
 * Hand-rolled vertical bar chart of applications per week (last 12 weeks) —
 * same "no charting library" rationale as StatusDistributionChart. Buckets
 * the day-keyed `byDay` map from the dashboard response into ISO
 * (Monday-start) weeks client-side; the backend only needs to return raw
 * daily counts.
 */
@Component({
  selector: 'app-activity-chart',
  imports: [DatePipe],
  templateUrl: './activity-chart.html',
  styleUrl: './activity-chart.scss',
})
export class ActivityChart {
  readonly byDay = input.required<Record<string, number>>();

  protected readonly weeks = computed<WeekBar[]>(() => {
    const byDay = this.byDay();
    const startOfThisWeek = mondayOf(new Date());

    const weekStarts: Date[] = [];
    for (let i = 11; i >= 0; i--) {
      const start = new Date(startOfThisWeek);
      start.setDate(start.getDate() - i * 7);
      weekStarts.push(start);
    }

    const rawCounts = weekStarts.map((weekStart) => {
      const weekEnd = new Date(weekStart);
      weekEnd.setDate(weekEnd.getDate() + 7);
      let count = 0;
      for (const [dateKey, dayCount] of Object.entries(byDay)) {
        const date = new Date(`${dateKey}T00:00:00`);
        if (date >= weekStart && date < weekEnd) {
          count += dayCount;
        }
      }
      return { weekStart, count };
    });

    const max = Math.max(1, ...rawCounts.map((week) => week.count));
    return rawCounts.map((week) => ({ ...week, heightPercent: (week.count / max) * 100 }));
  });

  protected readonly summary = computed(() =>
    this.weeks()
      .map((week) => `week of ${week.weekStart.toISOString().slice(0, 10)}: ${week.count}`)
      .join(', '),
  );
}

function mondayOf(date: Date): Date {
  const result = new Date(date);
  result.setHours(0, 0, 0, 0);
  const day = result.getDay();
  const diffToMonday = day === 0 ? -6 : 1 - day;
  result.setDate(result.getDate() + diffToMonday);
  return result;
}
