import { UserProfile } from '../../core/models/user.models';
import { TagResponse } from '../../core/tags/tag.models';
import { CompanyResponse } from '../companies/company.models';

export type EmploymentType = 'FULL_TIME' | 'PART_TIME' | 'CONTRACT' | 'INTERNSHIP' | 'FREELANCE';
export type WorkMode = 'REMOTE' | 'HYBRID' | 'ONSITE';

export const EMPLOYMENT_TYPES: EmploymentType[] = [
  'FULL_TIME',
  'PART_TIME',
  'CONTRACT',
  'INTERNSHIP',
  'FREELANCE',
];

export const WORK_MODES: WorkMode[] = ['REMOTE', 'HYBRID', 'ONSITE'];

/** Mirrors the response body of GET/POST/PUT /api/v1/jobs. */
export interface JobResponse {
  id: string;
  title: string;
  description: string;
  location: string | null;
  employmentType: EmploymentType | null;
  workMode: WorkMode | null;
  url: string | null;
  source: string | null;
  salaryRange: string | null;
  company: CompanyResponse;
  owner: UserProfile;
  createdAt: string;
  updatedAt: string;
  tags: TagResponse[];
}

/** Request body for POST/PUT /api/v1/jobs. */
export interface JobRequest {
  companyId: string;
  title: string;
  description: string;
  location: string | null;
  employmentType: EmploymentType | null;
  workMode: WorkMode | null;
  url: string | null;
  source: string | null;
  salaryRange: string | null;
}

/** Request body for POST /api/v1/jobs/extract. */
export interface JobExtractionRequest {
  rawText: string;
}

/**
 * Response body of POST /api/v1/jobs/extract — suggested field values to review before creating
 * the job. companyName is a plain string, not a companyId: the form matches it against the
 * user's existing companies, or offers to create a new one.
 */
export interface JobExtractionResponse {
  title: string | null;
  companyName: string | null;
  description: string | null;
  location: string | null;
  employmentType: EmploymentType | null;
  workMode: WorkMode | null;
  salaryRange: string | null;
  url: string | null;
}
