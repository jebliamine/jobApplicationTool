export type AppPageIcon =
  | 'dashboard'
  | 'cv'
  | 'jobs'
  | 'companies'
  | 'applications'
  | 'cover-letters'
  | 'ai-providers'
  | 'settings';

export interface AppPage {
  readonly label: string;
  readonly path: string;
  readonly icon: AppPageIcon;
  /** When true, only shown/searchable for ADMIN users — see nav-search.ts. */
  readonly adminOnly?: boolean;
}

/**
 * Single source of truth for the application's top-level pages — used by
 * both the topbar search (nav-search.ts) and the sidebar to suggest,
 * navigate to, and render available pages.
 */
export const APP_PAGES: readonly AppPage[] = [
  { label: 'Dashboard', path: '/dashboard', icon: 'dashboard' },
  { label: 'CV', path: '/cv', icon: 'cv' },
  { label: 'Jobs', path: '/jobs', icon: 'jobs' },
  { label: 'Companies', path: '/companies', icon: 'companies' },
  { label: 'Applications', path: '/applications', icon: 'applications' },
  { label: 'Cover Letters', path: '/cover-letters', icon: 'cover-letters' },
  { label: 'AI Providers', path: '/admin/ai-providers', icon: 'ai-providers', adminOnly: true },
  { label: 'Settings', path: '/settings', icon: 'settings' },
];
