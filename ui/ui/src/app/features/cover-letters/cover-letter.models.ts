import { UserProfile } from '../../core/models/user.models';
import { CvResponse } from '../cv/cv.models';
import { JobResponse } from '../jobs/job.models';

/** Mirrors the response body of GET/PUT /api/v1/cover-letters. */
export interface CoverLetterResponse {
  id: string;
  resultText: string;
  generationRequestId: string;
  job: JobResponse;
  cv: CvResponse | null;
  owner: UserProfile;
  archived: boolean;
  createdAt: string;
  updatedAt: string;
}

/** Request body for PUT /api/v1/cover-letters/{id} — only the edited text is accepted. */
export interface CoverLetterUpdateRequest {
  resultText: string;
}
