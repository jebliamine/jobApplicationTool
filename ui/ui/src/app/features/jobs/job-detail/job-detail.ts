import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ToastService } from '../../../core/ui/toast.service';
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
import {
  ConfirmDialog,
  ConfirmDialogData,
} from '../../../shared/components/confirm-dialog/confirm-dialog';
import { TagEditor } from '../../../shared/components/tag-editor/tag-editor';
import { describeApiError } from '../../../core/http/describe-api-error';
import { TagResponse } from '../../../core/tags/tag.models';
import { TagService } from '../../../core/tags/tag.service';
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
    TagEditor,
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
  private readonly toast = inject(ToastService);
  private readonly tagService = inject(TagService);

  private readonly jobId = this.route.snapshot.paramMap.get('id')!;

  private readonly state = signal<LoadState>('loading');
  private readonly _job = signal<JobResponse | null>(null);
  protected readonly deleting = signal(false);
  protected readonly allTags = signal<TagResponse[]>([]);
  protected readonly tagSaving = signal(false);

  protected readonly loading = computed(() => this.state() === 'loading');
  protected readonly error = computed(() => this.state() === 'error');
  protected readonly job = this._job.asReadonly();
  protected readonly isAdmin = computed(() => this.userService.currentUser()?.role === 'ADMIN');

  constructor() {
    this.load();
    this.tagService.list().subscribe((tags) => this.allTags.set(tags));
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

  protected onAddTag(tagId: string): void {
    const job = this.job();
    if (!job) {
      return;
    }
    this.saveTags([...job.tags.map((tag) => tag.id), tagId]);
  }

  protected onRemoveTag(tagId: string): void {
    const job = this.job();
    if (!job) {
      return;
    }
    this.saveTags(job.tags.map((tag) => tag.id).filter((id) => id !== tagId));
  }

  protected onCreateTag(name: string): void {
    this.tagService.create({ name }).subscribe({
      next: (tag) => {
        this.allTags.update((tags) => [...tags, tag]);
        this.onAddTag(tag.id);
      },
      error: (error: HttpErrorResponse) => this.toast.error(describeApiError(error)),
    });
  }

  private saveTags(tagIds: string[]): void {
    const job = this.job();
    if (!job) {
      return;
    }
    this.tagSaving.set(true);
    this.jobService.setTags(job.id, tagIds).subscribe({
      next: (updated) => {
        this._job.set(updated);
        this.tagSaving.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.tagSaving.set(false);
        this.toast.error(describeApiError(error));
      },
    });
  }

  protected confirmDelete(): void {
    const job = this.job();
    if (!job || this.deleting()) {
      return;
    }
    const ref = this.dialog.open<ConfirmDialog, ConfirmDialogData, boolean>(ConfirmDialog, {
      data: {
        title: 'Delete job?',
        message: `Are you sure you want to delete "${job.title}"? This action cannot be undone.`,
      },
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
        this.toast.success('Job deleted.');
        this.router.navigateByUrl('/jobs');
      },
      error: (error: HttpErrorResponse) => {
        this.deleting.set(false);
        this.toast.error(describeApiError(error));
      },
    });
  }
}
