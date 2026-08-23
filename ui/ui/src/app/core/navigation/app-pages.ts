export type AppPageIcon =
  | 'dashboard'
  | 'cv'
  | 'jobs'
  | 'companies'
  | 'applications'
  | 'cover-letters'
  | 'ai-providers'
  | 'admin-dashboard';

export interface AppPage {
  /** A translation key (see public/i18n/*.json under `nav.*`), not display text — render via `| translate`. */
  readonly label: string;
  readonly path: string;
  readonly icon: AppPageIcon;
}

/**
 * The normal-user workspace pages — rendered by UserNav's top navigation and
 * searched by its NavSearch instance. Settings is deliberately absent: it is
 * only ever reachable from the user menu, never a primary nav destination.
 */
export const USER_APP_PAGES: readonly AppPage[] = [
  { label: 'nav.dashboard', path: '/dashboard', icon: 'dashboard' },
  { label: 'nav.cv', path: '/cv', icon: 'cv' },
  { label: 'nav.jobs', path: '/jobs', icon: 'jobs' },
  { label: 'nav.companies', path: '/companies', icon: 'companies' },
  { label: 'nav.applications', path: '/applications', icon: 'applications' },
  { label: 'nav.coverLetters', path: '/cover-letters', icon: 'cover-letters' },
];

/** The admin-only operational pages — rendered by AdminShell's sidebar and searched by its own NavSearch instance. */
export const ADMIN_APP_PAGES: readonly AppPage[] = [
  { label: 'nav.adminDashboard', path: '/admin/dashboard', icon: 'admin-dashboard' },
  { label: 'nav.aiProviders', path: '/admin/ai-providers', icon: 'ai-providers' },
];
