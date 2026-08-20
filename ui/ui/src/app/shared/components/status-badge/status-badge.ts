import { Component, input } from '@angular/core';

export type StatusBadgeSeverity = 'info' | 'warning' | 'success' | 'error' | 'neutral';

@Component({
  selector: 'app-status-badge',
  templateUrl: './status-badge.html',
  styleUrl: './status-badge.scss',
})
export class StatusBadge {
  readonly label = input.required<string>();
  readonly severity = input.required<StatusBadgeSeverity>();
}
