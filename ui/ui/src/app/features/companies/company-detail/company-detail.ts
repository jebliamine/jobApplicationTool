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
  LucideCircleAlert,
  LucideGlobe,
  LucideMapPin,
  LucideNotebookText,
  LucidePencil,
  LucideShieldCheck,
  LucideTrash2,
  LucideUser,
} from '@lucide/angular';
import { UserService } from '../../../core/user/user.service';
import { describeJobError } from '../../jobs/job-error';
import {
  CompanyDeleteDialog,
  CompanyDeleteDialogData,
} from '../company-delete-dialog/company-delete-dialog';
import { CompanyResponse } from '../company.models';
import { CompanyService } from '../company.service';

type LoadState = 'loading' | 'loaded' | 'error';

@Component({
  selector: 'app-company-detail',
  imports: [
    DatePipe,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatProgressSpinnerModule,
    LucideArrowLeft,
    LucideCircleAlert,
    LucideGlobe,
    LucideMapPin,
    LucideNotebookText,
    LucidePencil,
    LucideShieldCheck,
    LucideTrash2,
    LucideUser,
  ],
  templateUrl: './company-detail.html',
  styleUrl: './company-detail.scss',
})
export class CompanyDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly companyService = inject(CompanyService);
  private readonly userService = inject(UserService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  private readonly companyId = this.route.snapshot.paramMap.get('id')!;

  private readonly state = signal<LoadState>('loading');
  private readonly _company = signal<CompanyResponse | null>(null);
  protected readonly deleting = signal(false);

  protected readonly loading = computed(() => this.state() === 'loading');
  protected readonly error = computed(() => this.state() === 'error');
  protected readonly company = this._company.asReadonly();
  protected readonly isAdmin = computed(() => this.userService.currentUser()?.role === 'ADMIN');

  constructor() {
    this.load();
  }

  protected load(): void {
    this.state.set('loading');
    this.companyService.get(this.companyId).subscribe({
      next: (company) => {
        this._company.set(company);
        this.state.set('loaded');
      },
      error: () => this.state.set('error'),
    });
  }

  protected confirmDelete(): void {
    const company = this.company();
    if (!company || this.deleting()) {
      return;
    }
    const ref = this.dialog.open<CompanyDeleteDialog, CompanyDeleteDialogData, boolean>(
      CompanyDeleteDialog,
      {
        data: { companyName: company.name },
        width: '420px',
      },
    );

    ref.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.performDelete(company);
      }
    });
  }

  private performDelete(company: CompanyResponse): void {
    this.deleting.set(true);
    this.companyService.delete(company.id).subscribe({
      next: () => {
        this.snackBar.open('Company deleted.', 'Dismiss', { duration: 4000 });
        this.router.navigateByUrl('/companies');
      },
      error: (error: HttpErrorResponse) => {
        this.deleting.set(false);
        this.snackBar.open(describeJobError(error), 'Dismiss', { duration: 5000 });
      },
    });
  }
}
