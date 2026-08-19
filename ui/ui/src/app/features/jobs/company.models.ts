import { UserProfile } from '../../core/models/user.models';

/** Mirrors the response body of GET/POST /api/v1/companies. */
export interface CompanyResponse {
  id: string;
  name: string;
  website: string | null;
  location: string | null;
  notes: string | null;
  owner: UserProfile;
  createdAt: string;
  updatedAt: string;
}

/** Request body for POST/PUT /api/v1/companies. */
export interface CompanyRequest {
  name: string;
  website: string | null;
  location: string | null;
  notes: string | null;
}
