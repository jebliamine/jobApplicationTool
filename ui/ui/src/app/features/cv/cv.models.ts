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
