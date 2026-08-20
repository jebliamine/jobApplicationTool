import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ToastService } from '../../../core/ui/toast.service';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import {
  LucideBuilding2,
  LucideCircleAlert,
  LucideEye,
  LucideInbox,
  LucidePencil,
  LucidePlus,
  LucideTrash2,
} from '@lucide/angular';
import { UserService } from '../../../core/user/user.service';
import { describeApiError } from '../../../core/http/describe-api-error';
import {
  ConfirmDialog,
  ConfirmDialogData,
} from '../../../shared/components/confirm-dialog/confirm-dialog';
import { CompanyResponse } from '../company.models';
import { CompanyService } from '../company.service';

type LoadState = 'loading' | 'loaded' | 'error';

@Component({
  selector: 'app-company-list',
  imports: [
    DatePipe,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatTableModule,
    MatTooltipModule,
    LucideBuilding2,
    LucideCircleAlert,
    LucideEye,
    LucideInbox,
    LucidePencil,
    LucidePlus,
    LucideTrash2,
  ],
  templateUrl: './company-list.html',
  styleUrl: './company-list.scss',
})
export class CompanyList {
  private readonly companyService = inject(CompanyService);
  private readonly userService = inject(UserService);
  private readonly dialog = inject(MatDialog);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  private readonly state = signal<LoadState>('loading');
  private readonly _companies = signal<CompanyResponse[]>([]);
  protected readonly deletingId = signal<string | null>(null);

  protected readonly loading = computed(() => this.state() === 'loading');
  protected readonly error = computed(() => this.state() === 'error');
  protected readonly companies = this._companies.asReadonly();
  protected readonly isAdmin = computed(() => this.userService.currentUser()?.role === 'ADMIN');

  protected readonly displayedColumns = computed(() => {
    const base = ['name', 'website', 'location', 'createdAt'];
    return [...base, ...(this.isAdmin() ? ['owner'] : []), 'actions'];
  });

  constructor() {
    this.load();
  }

  protected load(): void {
    this.state.set('loading');
    this.companyService.list().subscribe({
      next: (companies) => {
        this._companies.set(companies);
        this.state.set('loaded');
      },
      error: () => this.state.set('error'),
    });
  }

  protected viewCompany(company: CompanyResponse): void {
    this.router.navigateByUrl(`/companies/${company.id}`);
  }

  protected editCompany(company: CompanyResponse): void {
    this.router.navigateByUrl(`/companies/${company.id}/edit`);
  }

  protected confirmDelete(company: CompanyResponse): void {
    if (this.deletingId()) {
      return;
    }
    const ref = this.dialog.open<ConfirmDialog, ConfirmDialogData, boolean>(ConfirmDialog, {
      data: {
        title: 'Delete company?',
        message: `Are you sure you want to delete "${company.name}"? This action cannot be undone.`,
      },
      width: '420px',
    });

    ref.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.performDelete(company);
      }
    });
  }

  private performDelete(company: CompanyResponse): void {
    this.deletingId.set(company.id);
    this.companyService.delete(company.id).subscribe({
      next: () => {
        this._companies.update((current) => current.filter((c) => c.id !== company.id));
        this.deletingId.set(null);
        this.toast.success('Company deleted.');
      },
      error: (error: HttpErrorResponse) => {
        this.deletingId.set(null);
        this.toast.error(describeApiError(error));
      },
    });
  }
}
