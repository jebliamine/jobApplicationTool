export type SearchResultType = 'JOB' | 'COMPANY' | 'APPLICATION' | 'COVER_LETTER';

/** Mirrors the response body of GET /api/v1/search?q=... */
export interface SearchResultResponse {
  type: SearchResultType;
  id: string;
  title: string;
  subtitle: string | null;
}

/** Where each result type's detail page lives — the backend deliberately doesn't send a route. */
export function routeForSearchResult(result: SearchResultResponse): string {
  switch (result.type) {
    case 'JOB':
      return `/jobs/${result.id}`;
    case 'COMPANY':
      return `/companies/${result.id}`;
    case 'APPLICATION':
      return `/applications/${result.id}`;
    case 'COVER_LETTER':
      return `/cover-letters/${result.id}`;
  }
}
