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
  LucideCalendar,
  LucideCircleAlert,
  LucideFileText,
  LucideMail,
  LucideNotebookText,
  LucidePencil,
  LucideShieldCheck,
  LucideTrash2,
  LucideUser,
} from '@lucide/angular';
import { describeApiError } from '../../../core/http/describe-api-error';
import { UserService } from '../../../core/user/user.service';
import {
  ConfirmDialog,
  ConfirmDialogData,
} from '../../../shared/components/confirm-dialog/confirm-dialog';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';
import { TagEditor } from '../../../shared/components/tag-editor/tag-editor';
import { TagResponse } from '../../../core/tags/tag.models';
import { TagService } from '../../../core/tags/tag.service';
import { InterviewStageEditor } from '../interview-stage-editor/interview-stage-editor';
import { ApplicationResponse, ApplicationStatus, InterviewStageRequest } from '../application.models';
import { ApplicationService } from '../application.service';
import {
  APPLICATION_STATUS_LABELS,
  APPLICATION_STATUS_SEVERITY,
  ApplicationStatusSeverity,
} from '../application-status';

type LoadState = 'loading' | 'loaded' | 'error';

@Component({
  selector: 'app-application-detail',
  imports: [
    DatePipe,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatProgressSpinnerModule,
    LucideArrowLeft,
    LucideBriefcase,
    LucideCalendar,
    LucideCircleAlert,
    LucideFileText,
    LucideMail,
    LucideNotebookText,
    LucidePencil,
    LucideShieldCheck,
    LucideTrash2,
    LucideUser,
    StatusBadge,
    TagEditor,
    InterviewStageEditor,
  ],
  templateUrl: './application-detail.html',
  styleUrl: './application-detail.scss',
})
export class ApplicationDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly applicationService = inject(ApplicationService);
  private readonly userService = inject(UserService);
  private readonly dialog = inject(MatDialog);
  private readonly toast = inject(ToastService);
  private readonly tagService = inject(TagService);

  private readonly applicationId = this.route.snapshot.paramMap.get('id')!;

  private readonly state = signal<LoadState>('loading');
  private readonly _application = signal<ApplicationResponse | null>(null);
  protected readonly deleting = signal(false);
  protected readonly allTags = signal<TagResponse[]>([]);
  protected readonly tagSaving = signal(false);
  protected readonly stageSaving = signal(false);

  protected readonly loading = computed(() => this.state() === 'loading');
  protected readonly error = computed(() => this.state() === 'error');
  protected readonly application = this._application.asReadonly();
  protected readonly isAdmin = computed(() => this.userService.currentUser()?.role === 'ADMIN');

  constructor() {
    this.load();
    this.tagService.list().subscribe((tags) => this.allTags.set(tags));
  }

  protected statusLabel(status: ApplicationStatus): string {
    return APPLICATION_STATUS_LABELS[status];
  }

  protected statusSeverity(status: ApplicationStatus): ApplicationStatusSeverity {
    return APPLICATION_STATUS_SEVERITY[status];
  }

  protected load(): void {
    this.state.set('loading');
    this.applicationService.get(this.applicationId).subscribe({
      next: (application) => {
        this._application.set(application);
        this.state.set('loaded');
      },
      error: () => this.state.set('error'),
    });
  }

  protected onAddTag(tagId: string): void {
    const application = this.application();
    if (!application) {
      return;
    }
    this.saveTags([...application.tags.map((tag) => tag.id), tagId]);
  }

  protected onRemoveTag(tagId: string): void {
    const application = this.application();
    if (!application) {
      return;
    }
    this.saveTags(application.tags.map((tag) => tag.id).filter((id) => id !== tagId));
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
    const application = this.application();
    if (!application) {
      return;
    }
    this.tagSaving.set(true);
    this.applicationService.setTags(application.id, tagIds).subscribe({
      next: (updated) => {
        this._application.set(updated);
        this.tagSaving.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.tagSaving.set(false);
        this.toast.error(describeApiError(error));
      },
    });
  }

  protected onAddStage(request: InterviewStageRequest): void {
    const application = this.application();
    if (!application) {
      return;
    }
    this.stageSaving.set(true);
    this.applicationService.addInterviewStage(application.id, request).subscribe({
      next: (updated) => {
        this._application.set(updated);
        this.stageSaving.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.stageSaving.set(false);
        this.toast.error(describeApiError(error));
      },
    });
  }

  protected onToggleStageCompleted(event: { stageId: string; completed: boolean }): void {
    const application = this.application();
    const stage = application?.interviewStages.find((candidate) => candidate.id === event.stageId);
    if (!application || !stage) {
      return;
    }
    this.updateStage(application.id, stage.id, {
      title: stage.title,
      scheduledDate: stage.scheduledDate,
      notes: stage.notes,
      completed: event.completed,
    });
  }

  protected onRemoveStage(stageId: string): void {
    const application = this.application();
    if (!application) {
      return;
    }
    this.stageSaving.set(true);
    this.applicationService.removeInterviewStage(application.id, stageId).subscribe({
      next: (updated) => {
        this._application.set(updated);
        this.stageSaving.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.stageSaving.set(false);
        this.toast.error(describeApiError(error));
      },
    });
  }

  private updateStage(applicationId: string, stageId: string, request: InterviewStageRequest): void {
    this.stageSaving.set(true);
    this.applicationService.updateInterviewStage(applicationId, stageId, request).subscribe({
      next: (updated) => {
        this._application.set(updated);
        this.stageSaving.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.stageSaving.set(false);
        this.toast.error(describeApiError(error));
      },
    });
  }

  protected confirmDelete(): void {
    const application = this.application();
    if (!application || this.deleting()) {
      return;
    }
    const ref = this.dialog.open<ConfirmDialog, ConfirmDialogData, boolean>(ConfirmDialog, {
      data: {
        title: 'Delete application?',
        message: `Are you sure you want to delete your application for "${application.job.title}"? This action cannot be undone.`,
      },
      width: '420px',
    });

    ref.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.performDelete(application);
      }
    });
  }

  private performDelete(application: ApplicationResponse): void {
    this.deleting.set(true);
    this.applicationService.delete(application.id).subscribe({
      next: () => {
        this.toast.success('Application deleted.');
        this.router.navigateByUrl('/applications');
      },
      error: (error: HttpErrorResponse) => {
        this.deleting.set(false);
        this.toast.error(describeApiError(error));
      },
    });
  }
}
