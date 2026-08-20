import { UserProfile } from '../../core/models/user.models';
import { CoverLetterResponse } from '../cover-letters/cover-letter.models';
import { CvResponse } from '../cv/cv.models';
import { JobResponse } from '../jobs/job.models';

export type ApplicationStatus =
  | 'APPLIED'
  | 'PHONE_SCREEN'
  | 'INTERVIEWING'
  | 'OFFER'
  | 'REJECTED'
  | 'WITHDRAWN'
  | 'ACCEPTED';

export const APPLICATION_STATUSES: ApplicationStatus[] = [
  'APPLIED',
  'PHONE_SCREEN',
  'INTERVIEWING',
  'OFFER',
  'REJECTED',
  'WITHDRAWN',
  'ACCEPTED',
];

/** Mirrors the response body of GET/POST/PUT /api/v1/applications. */
export interface ApplicationResponse {
  id: string;
  job: JobResponse;
  cv: CvResponse | null;
  coverLetter: CoverLetterResponse | null;
  status: ApplicationStatus;
  appliedAt: string;
  deadline: string | null;
  followUpDate: string | null;
  interviewDate: string | null;
  contactPerson: string | null;
  notes: string | null;
  owner: UserProfile;
  createdAt: string;
  updatedAt: string;
}

/** Request body for POST/PUT /api/v1/applications — owner is never accepted from the client. */
export interface ApplicationRequest {
  jobId: string;
  cvDocumentId: string | null;
  coverLetterId: string | null;
  status: ApplicationStatus;
  appliedAt: string;
  deadline: string | null;
  followUpDate: string | null;
  interviewDate: string | null;
  contactPerson: string | null;
  notes: string | null;
}
