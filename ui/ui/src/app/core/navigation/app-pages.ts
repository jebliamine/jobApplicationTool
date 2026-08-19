export interface AppPage {
  readonly label: string;
  readonly path: string;
  /** When true, only shown/searchable for ADMIN users — see nav-search.ts. */
  readonly adminOnly?: boolean;
}

/**
 * Single source of truth for the application's top-level pages, used by the
 * topbar search to suggest and navigate to available pages. Sidebar labels
 * and routes should stay in sync with this list.
 */
export const APP_PAGES: readonly AppPage[] = [
  { label: 'Dashboard', path: '/dashboard' },
  { label: 'CV', path: '/cv' },
  { label: 'Jobs', path: '/jobs' },
  { label: 'Applications', path: '/applications' },
  { label: 'Cover Letters', path: '/cover-letters' },
  { label: 'AI Providers', path: '/admin/ai-providers', adminOnly: true },
  { label: 'Settings', path: '/settings' },
];
