import { EmploymentType, WorkMode } from '../jobs/job.models';

export type ExternalJobSource = 'ADZUNA' | 'JOOBLE' | 'JSEARCH';

/**
 * One listing from GET /api/v1/job-search. Mirrors JobExtractionResponse field-for-field
 * (title/companyName/description/location/employmentType/workMode/salaryRange/url) on purpose —
 * "Save to my jobs" reuses the exact same prefill path as paste-to-import in JobForm.
 */
export interface ExternalJobListing {
  source: ExternalJobSource;
  externalId: string | null;
  title: string | null;
  companyName: string | null;
  description: string | null;
  location: string | null;
  employmentType: EmploymentType | null;
  workMode: WorkMode | null;
  salaryRange: string | null;
  url: string | null;
  postedAt: string | null;
}

export interface JobSearchSourceSummary {
  source: ExternalJobSource;
  configured: boolean;
  succeeded: boolean;
  resultCount: number;
}

/** Response body of GET /api/v1/job-search. */
export interface ExternalJobSearchResponse {
  results: ExternalJobListing[];
  sources: JobSearchSourceSummary[];
}
