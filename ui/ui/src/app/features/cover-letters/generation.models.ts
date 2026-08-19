import { UserProfile } from '../../core/models/user.models';
import { CvResponse } from '../cv/cv.models';
import { JobResponse } from '../jobs/job.models';
import { CoverLetterResponse } from './cover-letter.models';

export type GenerationStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'FAILED';

/** Mirrors the response body of GET/POST /api/v1/generation-requests. */
export interface GenerationRequestResponse {
  id: string;
  job: JobResponse;
  cv: CvResponse | null;
  status: GenerationStatus;
  provider: string | null;
  model: string | null;
  errorMessage: string | null;
  coverLetter: CoverLetterResponse | null;
  owner: UserProfile;
  createdAt: string;
  startedAt: string | null;
  completedAt: string | null;
}

/** Request body for POST /api/v1/generation-requests — owner is never accepted from the client. */
export interface GenerationRequestCreateRequest {
  jobId: string;
  cvDocumentId: string;
}
