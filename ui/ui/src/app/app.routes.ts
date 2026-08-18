import { Routes } from '@angular/router';

const loadPlaceholder = () =>
  import('./shared/components/placeholder-page/placeholder-page').then((m) => m.PlaceholderPage);

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard').then((m) => m.Dashboard),
  },
  {
    path: 'cv',
    loadComponent: loadPlaceholder,
    data: { title: 'CV', description: 'CV management is not implemented yet.' },
  },
  {
    path: 'jobs',
    loadComponent: loadPlaceholder,
    data: { title: 'Jobs', description: 'Job tracking is not implemented yet.' },
  },
  {
    path: 'applications',
    loadComponent: loadPlaceholder,
    data: { title: 'Applications', description: 'Application tracking is not implemented yet.' },
  },
  {
    path: 'cover-letters',
    loadComponent: loadPlaceholder,
    data: { title: 'Cover Letters', description: 'Cover letter generation is not implemented yet.' },
  },
  {
    path: 'settings',
    loadComponent: loadPlaceholder,
    data: { title: 'Settings', description: 'Application settings are not implemented yet.' },
  },
  { path: '**', redirectTo: 'dashboard' },
];
