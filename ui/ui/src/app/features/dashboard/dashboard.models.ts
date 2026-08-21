import { ApplicationStatus } from '../applications/application.models';
import { GenerationStatus } from '../cover-letters/generation.models';

/**
 * Mirrors the response body of GET /api/v1/dashboard. Backend scopes every
 * count to the caller's own data, or global totals for admins — see
 * DashboardController/DashboardService. totalUsers is only populated for
 * admins. applicationsByDay covers the last 12 weeks, keyed by ISO date
 * (`YYYY-MM-DD`); a missing key means zero applications that day.
 */
export interface DashboardResponse {
  cvCount: number;
  jobCount: number;
  applicationCount: number;
  applicationStatusCounts: Record<ApplicationStatus, number>;
  applicationsByDay: Record<string, number>;
  activeCoverLetterCount: number;
  archivedCoverLetterCount: number;
  generationRequestCount: number;
  generationStatusCounts: Record<GenerationStatus, number>;
  totalUsers: number | null;
}
