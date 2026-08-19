import { UserProfile } from '../../core/models/user.models';
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
  company: CompanyResponse;
  owner: UserProfile;
  createdAt: string;
  updatedAt: string;
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
}
