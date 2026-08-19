import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { LucideCircleAlert, LucideEye, LucideInbox, LucideMail, LucidePlus } from '@lucide/angular';
import { UserService } from '../../../core/user/user.service';
import { CoverLetterResponse } from '../cover-letter.models';
import { CoverLetterService } from '../cover-letter.service';

type LoadState = 'loading' | 'loaded' | 'error';

@Component({
  selector: 'app-cover-letter-list',
  imports: [
    DatePipe,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatTableModule,
    MatTooltipModule,
    LucideCircleAlert,
    LucideEye,
    LucideInbox,
    LucideMail,
    LucidePlus,
  ],
  templateUrl: './cover-letter-list.html',
  styleUrl: './cover-letter-list.scss',
})
export class CoverLetterList {
  private readonly coverLetterService = inject(CoverLetterService);
  private readonly userService = inject(UserService);
  private readonly router = inject(Router);

  private readonly state = signal<LoadState>('loading');
  private readonly _coverLetters = signal<CoverLetterResponse[]>([]);

  protected readonly loading = computed(() => this.state() === 'loading');
  protected readonly error = computed(() => this.state() === 'error');
  protected readonly coverLetters = this._coverLetters.asReadonly();
  protected readonly isAdmin = computed(() => this.userService.currentUser()?.role === 'ADMIN');

  protected readonly displayedColumns = computed(() => {
    const base = ['job', 'company', 'updatedAt'];
    return [...base, ...(this.isAdmin() ? ['owner'] : []), 'actions'];
  });

  constructor() {
    this.load();
  }

  protected load(): void {
    this.state.set('loading');
    this.coverLetterService.list().subscribe({
      next: (coverLetters) => {
        this._coverLetters.set(coverLetters);
        this.state.set('loaded');
      },
      error: () => this.state.set('error'),
    });
  }

  protected viewCoverLetter(coverLetter: CoverLetterResponse): void {
    this.router.navigateByUrl(`/cover-letters/${coverLetter.id}`);
  }
}
