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

/**
 * PUT /api/v1/applications/{id} overwrites every field from the request body
 * (see ApplicationService#applyRequest on the backend), so a status-only
 * change still has to resend the rest of the record unchanged. Single-sourced
 * here so application-list and the Kanban board can't drift on which fields
 * must be preserved.
 */
export function buildStatusChangeRequest(
  application: ApplicationResponse,
  status: ApplicationStatus,
): ApplicationRequest {
  return {
    jobId: application.job.id,
    cvDocumentId: application.cv?.id ?? null,
    coverLetterId: application.coverLetter?.id ?? null,
    status,
    appliedAt: application.appliedAt,
    deadline: application.deadline,
    followUpDate: application.followUpDate,
    interviewDate: application.interviewDate,
    contactPerson: application.contactPerson,
    notes: application.notes,
  };
}
