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
  funnelMetrics: FunnelMetricsResponse;
}

/**
 * Nested in DashboardResponse. responseRate/offerRate/averageDaysInCurrentStatus are derived from
 * each application's *current* status only — there is no per-status change history, so
 * averageDaysInCurrentStatus approximates "time in stage" using each application's last-updated
 * timestamp (see FunnelMetricsCalculator on the backend for the exact rules).
 */
export interface FunnelMetricsResponse {
  totalApplications: number;
  responseRate: number;
  offerRate: number;
  averageDaysInCurrentStatus: Partial<Record<ApplicationStatus, number>>;
  byCompany: CompanyFunnelStat[];
}

export interface CompanyFunnelStat {
  companyName: string;
  applications: number;
  responseRate: number;
  offerRate: number;
}
