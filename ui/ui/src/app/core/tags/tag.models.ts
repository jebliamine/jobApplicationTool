/** Mirrors the response body of GET/POST/PUT /api/v1/tags. */
export interface TagResponse {
  id: string;
  name: string;
}

/** Request body for POST /api/v1/tags and PUT /api/v1/tags/{id}. */
export interface TagRequest {
  name: string;
}
