import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import {
  LucideBriefcase,
  LucideCircleAlert,
  LucideInbox,
  LucidePencil,
  LucidePlus,
  LucideTrash2,
} from '@lucide/angular';
import { UserService } from '../../../core/user/user.service';
import { describeJobError } from '../job-error';
import { JobDeleteDialog, JobDeleteDialogData } from '../job-delete-dialog/job-delete-dialog';
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
  selector: 'app-job-list',
  imports: [
    DatePipe,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatTableModule,
    MatTooltipModule,
    LucideBriefcase,
    LucideCircleAlert,
    LucideInbox,
    LucidePencil,
    LucidePlus,
    LucideTrash2,
  ],
  templateUrl: './job-list.html',
  styleUrl: './job-list.scss',
})
export class JobList {
  private readonly jobService = inject(JobService);
  private readonly userService = inject(UserService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly router = inject(Router);

  private readonly state = signal<LoadState>('loading');
  private readonly _jobs = signal<JobResponse[]>([]);
  protected readonly deletingId = signal<string | null>(null);

  protected readonly loading = computed(() => this.state() === 'loading');
  protected readonly error = computed(() => this.state() === 'error');
  protected readonly jobs = this._jobs.asReadonly();
  protected readonly isAdmin = computed(() => this.userService.currentUser()?.role === 'ADMIN');

  protected readonly displayedColumns = computed(() => {
    const base = ['title', 'company', 'location', 'employmentType', 'workMode', 'createdAt'];
    return [...base, ...(this.isAdmin() ? ['owner'] : []), 'actions'];
  });

  constructor() {
    this.load();
  }

  protected load(): void {
    this.state.set('loading');
    this.jobService.list().subscribe({
      next: (jobs) => {
        this._jobs.set(jobs);
        this.state.set('loaded');
      },
      error: () => this.state.set('error'),
    });
  }

  protected formatEmploymentType(type: EmploymentType | null): string {
    return type ? EMPLOYMENT_TYPE_LABELS[type] : '—';
  }

  protected formatWorkMode(mode: WorkMode | null): string {
    return mode ? WORK_MODE_LABELS[mode] : '—';
  }

  protected editJob(job: JobResponse): void {
    this.router.navigateByUrl(`/jobs/${job.id}/edit`);
  }

  protected confirmDelete(job: JobResponse): void {
    if (this.deletingId()) {
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
    this.deletingId.set(job.id);
    this.jobService.delete(job.id).subscribe({
      next: () => {
        this._jobs.update((current) => current.filter((j) => j.id !== job.id));
        this.deletingId.set(null);
        this.snackBar.open('Job deleted.', 'Dismiss', { duration: 4000 });
      },
      error: (error: HttpErrorResponse) => {
        this.deletingId.set(null);
        this.snackBar.open(describeJobError(error), 'Dismiss', { duration: 5000 });
      },
    });
  }
}
