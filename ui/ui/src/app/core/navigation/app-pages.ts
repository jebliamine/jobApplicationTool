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
  /** A translation key (see public/i18n/*.json under `nav.*`), not display text — render via `| translate`. */
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
  { label: 'nav.dashboard', path: '/dashboard', icon: 'dashboard' },
  { label: 'nav.cv', path: '/cv', icon: 'cv' },
  { label: 'nav.jobs', path: '/jobs', icon: 'jobs' },
  { label: 'nav.companies', path: '/companies', icon: 'companies' },
  { label: 'nav.applications', path: '/applications', icon: 'applications' },
  { label: 'nav.coverLetters', path: '/cover-letters', icon: 'cover-letters' },
  { label: 'nav.aiProviders', path: '/admin/ai-providers', icon: 'ai-providers', adminOnly: true },
  { label: 'nav.settings', path: '/settings', icon: 'settings' },
];
