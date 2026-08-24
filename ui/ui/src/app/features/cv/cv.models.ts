import { UserProfile } from '../../core/models/user.models';

/** Mirrors the response body of GET/POST /api/v1/cv. */
export interface CvResponse {
  id: string;
  title: string;
  fileName: string;
  contentType: string;
  size: number;
  createdAt: string;
  updatedAt: string;
  owner: UserProfile;
}

/** Result of GET /api/v1/cv/{id}/download — filename comes from the backend's Content-Disposition header. */
export interface DownloadedCv {
  blob: Blob;
  filename: string;
}

export type ProfileGenerationStatus = 'NOT_ATTEMPTED' | 'IN_PROGRESS' | 'COMPLETED' | 'FAILED';

/** Mirrors ExperienceResponse. */
export interface ExperienceResponse {
  id: string;
  company: string | null;
  title: string | null;
  startDate: string | null;
  endDate: string | null;
  description: string | null;
}

/** Mirrors the response body of GET/POST /api/v1/cv/{id}/profile. */
export interface CvProfileResponse {
  id: string | null;
  fullName: string | null;
  summary: string | null;
  experiences: ExperienceResponse[];
  status: ProfileGenerationStatus;
  errorMessage: string | null;
  generatedAt: string | null;
}
