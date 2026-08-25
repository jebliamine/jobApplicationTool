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

/**
 * Request body for POST /api/v1/generation-requests — owner is never
 * accepted from the client. providerId is the id of an AiProviderConfiguration
 * instance (one of the ids returned by GET /api/v1/ai/providers — see
 * ../ai-provider.models.ts); omitting it defaults to the built-in Placeholder
 * instance on the backend. useStructuredCv requests the CV's AI-extracted
 * profile (see ../cv/cv.models.ts CvProfileResponse) as context instead of
 * the CV's raw text — the backend falls back to the raw text on its own if
 * no COMPLETED profile exists, so this is always safe to send.
 */
export interface GenerationRequestCreateRequest {
  jobId: string;
  cvDocumentId: string;
  providerId?: string;
  useStructuredCv?: boolean;
}
