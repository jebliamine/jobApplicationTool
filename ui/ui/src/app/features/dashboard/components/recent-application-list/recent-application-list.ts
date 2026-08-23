import { DatePipe } from '@angular/common';
import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideInbox } from '@lucide/angular';
import { TranslatePipe } from '@ngx-translate/core';
import { StatusBadge } from '../../../../shared/components/status-badge/status-badge';
import {
  APPLICATION_STATUS_LABELS,
  APPLICATION_STATUS_SEVERITY,
} from '../../../applications/application-status';
import { ApplicationResponse, ApplicationStatus } from '../../../applications/application.models';

/** A short, real (never fabricated) slice of the caller's own most-recently-created applications. */
@Component({
  selector: 'app-recent-application-list',
  imports: [DatePipe, RouterLink, TranslatePipe, StatusBadge, LucideInbox],
  templateUrl: './recent-application-list.html',
  styleUrl: './recent-application-list.scss',
})
export class RecentApplicationList {
  readonly applications = input.required<readonly ApplicationResponse[]>();

  protected statusLabel(status: ApplicationStatus): string {
    return APPLICATION_STATUS_LABELS[status];
  }

  protected statusSeverity(status: ApplicationStatus) {
    return APPLICATION_STATUS_SEVERITY[status];
  }
}
