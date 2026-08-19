import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import {
  LucideArrowLeft,
  LucideBriefcase,
  LucideCircleAlert,
  LucideLink,
  LucideMapPin,
  LucidePencil,
  LucideRadio,
  LucideShieldCheck,
  LucideTrash2,
  LucideUser,
} from '@lucide/angular';
import { UserService } from '../../../core/user/user.service';
import { JobDeleteDialog, JobDeleteDialogData } from '../job-delete-dialog/job-delete-dialog';
import { describeJobError } from '../job-error';
import { EmploymentType, JobResponse, WorkMode } from '../job.models';
import { JobService } from '../job.service';

type LoadState = 'loading' | 'loaded' | 'error';

const EMPLOYMENT_TYPE_LABELS: Record<EmploymentType, string> = {
  FULL_TIME: 'Full-time',
  PART_TIME: 'Part-time',
  CONTRACT: 'Contract',
  INTERNSHIP: 'Internship',
  FREELANCE: 'Freelance',
};

const WORK_MODE_LABELS: Record<WorkMode, string> = {
  REMOTE: 'Remote',
  HYBRID: 'Hybrid',
  ONSITE: 'On-site',
};

@Component({
  selector: 'app-job-detail',
  imports: [
    DatePipe,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatProgressSpinnerModule,
    LucideArrowLeft,
    LucideBriefcase,
    LucideCircleAlert,
    LucideLink,
    LucideMapPin,
    LucidePencil,
    LucideRadio,
    LucideShieldCheck,
    LucideTrash2,
    LucideUser,
  ],
  templateUrl: './job-detail.html',
  styleUrl: './job-detail.scss',
})
export class JobDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly jobService = inject(JobService);
  private readonly userService = inject(UserService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  private readonly jobId = this.route.snapshot.paramMap.get('id')!;

  private readonly state = signal<LoadState>('loading');
  private readonly _job = signal<JobResponse | null>(null);
  protected readonly deleting = signal(false);

  protected readonly loading = computed(() => this.state() === 'loading');
  protected readonly error = computed(() => this.state() === 'error');
  protected readonly job = this._job.asReadonly();
  protected readonly isAdmin = computed(() => this.userService.currentUser()?.role === 'ADMIN');

  constructor() {
    this.load();
  }

  protected formatEmploymentType(type: EmploymentType | null): string {
    return type ? EMPLOYMENT_TYPE_LABELS[type] : '—';
  }

  protected formatWorkMode(mode: WorkMode | null): string {
    return mode ? WORK_MODE_LABELS[mode] : '—';
  }

  protected load(): void {
    this.state.set('loading');
    this.jobService.get(this.jobId).subscribe({
      next: (job) => {
        this._job.set(job);
        this.state.set('loaded');
      },
      error: () => this.state.set('error'),
    });
  }

  protected confirmDelete(): void {
    const job = this.job();
    if (!job || this.deleting()) {
      return;
    }
    const ref = this.dialog.open<JobDeleteDialog, JobDeleteDialogData, boolean>(JobDeleteDialog, {
      data: { jobTitle: job.title },
      width: '420px',
    });

    ref.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.performDelete(job);
      }
    });
  }

  private performDelete(job: JobResponse): void {
    this.deleting.set(true);
    this.jobService.delete(job.id).subscribe({
      next: () => {
        this.snackBar.open('Job deleted.', 'Dismiss', { duration: 4000 });
        this.router.navigateByUrl('/jobs');
      },
      error: (error: HttpErrorResponse) => {
        this.deleting.set(false);
        this.snackBar.open(describeJobError(error), 'Dismiss', { duration: 5000 });
      },
    });
  }
}
