import { Routes } from '@angular/router';
import { adminGuard } from './core/guards/admin.guard';
import { authGuard, guestGuard, publicGuard } from './core/guards/auth.guard';
import { AdminShell } from './layout/admin-shell/admin-shell';
import { UserShell } from './layout/user-shell/user-shell';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () => import('./features/public-landing/public-landing').then((m) => m.PublicLanding),
    canActivate: [publicGuard],
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login').then((m) => m.Login),
    canActivate: [guestGuard],
  },
  {
    path: 'register',
    loadComponent: () => import('./features/auth/register/register').then((m) => m.Register),
    canActivate: [guestGuard],
  },
  {
    path: '',
    component: UserShell,
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard').then((m) => m.Dashboard),
      },
      {
        path: 'cv',
        loadComponent: () => import('./features/cv/cv').then((m) => m.Cv),
      },
      {
        path: 'companies',
        children: [
          {
            path: '',
            loadComponent: () =>
              import('./features/companies/company-list/company-list').then((m) => m.CompanyList),
          },
          {
            path: 'new',
            loadComponent: () =>
              import('./features/companies/company-form/company-form').then((m) => m.CompanyForm),
          },
          {
            path: ':id',
            loadComponent: () =>
              import('./features/companies/company-detail/company-detail').then((m) => m.CompanyDetail),
          },
          {
            path: ':id/edit',
            loadComponent: () =>
              import('./features/companies/company-form/company-form').then((m) => m.CompanyForm),
          },
        ],
      },
      {
        path: 'jobs',
        children: [
          {
            path: '',
            loadComponent: () => import('./features/jobs/job-list/job-list').then((m) => m.JobList),
          },
          {
            path: 'new',
            loadComponent: () => import('./features/jobs/job-form/job-form').then((m) => m.JobForm),
          },
          {
            path: ':id',
            loadComponent: () => import('./features/jobs/job-detail/job-detail').then((m) => m.JobDetail),
          },
          {
            path: ':id/edit',
            loadComponent: () => import('./features/jobs/job-form/job-form').then((m) => m.JobForm),
          },
        ],
      },
      {
        path: 'applications',
        children: [
          {
            path: '',
            loadComponent: () =>
              import('./features/applications/application-list/application-list').then(
                (m) => m.ApplicationList,
              ),
          },
          {
            path: 'new',
            loadComponent: () =>
              import('./features/applications/application-form/application-form').then(
                (m) => m.ApplicationForm,
              ),
          },
          {
            path: 'board',
            loadComponent: () =>
              import('./features/applications/application-board/application-board').then(
                (m) => m.ApplicationBoard,
              ),
          },
          {
            path: ':id',
            loadComponent: () =>
              import('./features/applications/application-detail/application-detail').then(
                (m) => m.ApplicationDetail,
              ),
          },
          {
            path: ':id/edit',
            loadComponent: () =>
              import('./features/applications/application-form/application-form').then(
                (m) => m.ApplicationForm,
              ),
          },
        ],
      },
      {
        path: 'cover-letters',
        children: [
          {
            path: '',
            loadComponent: () =>
              import('./features/cover-letters/cover-letter-list/cover-letter-list').then(
                (m) => m.CoverLetterList,
              ),
          },
          {
            path: 'generate',
            loadComponent: () =>
              import('./features/cover-letters/generation-form/generation-form').then((m) => m.GenerationForm),
          },
          {
            path: ':id',
            loadComponent: () =>
              import('./features/cover-letters/cover-letter-detail/cover-letter-detail').then(
                (m) => m.CoverLetterDetail,
              ),
          },
        ],
      },
      {
        path: 'settings',
        loadComponent: () => import('./features/settings/settings').then((m) => m.Settings),
      },
      { path: 'profile', redirectTo: 'settings' },
    ],
  },
  {
    path: 'admin',
    component: AdminShell,
    canActivate: [authGuard, adminGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/admin/admin-dashboard/admin-dashboard').then((m) => m.AdminDashboard),
      },
      {
        path: 'ai-providers',
        loadComponent: () =>
          import('./features/admin/ai-providers/ai-provider-list/ai-provider-list').then((m) => m.AiProviderList),
      },
      {
        path: 'users',
        loadComponent: () =>
          import('./features/admin/user-management/user-management').then((m) => m.UserManagement),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
