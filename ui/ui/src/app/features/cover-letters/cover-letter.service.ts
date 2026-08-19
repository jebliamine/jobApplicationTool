import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CoverLetterResponse, CoverLetterUpdateRequest } from './cover-letter.models';

@Injectable({ providedIn: 'root' })
export class CoverLetterService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/cover-letters`;

  /**
   * GET /cover-letters?archived=… — backend scopes this to the caller's own
   * cover letters, or all for admins. Defaults to the active (non-archived)
   * view, matching the backend's default.
   */
  list(archived = false): Observable<CoverLetterResponse[]> {
    const params = new HttpParams().set('archived', archived);
    return this.http.get<CoverLetterResponse[]>(this.baseUrl, { params });
  }

  get(id: string): Observable<CoverLetterResponse> {
    return this.http.get<CoverLetterResponse>(`${this.baseUrl}/${id}`);
  }

  update(id: string, request: CoverLetterUpdateRequest): Observable<CoverLetterResponse> {
    return this.http.put<CoverLetterResponse>(`${this.baseUrl}/${id}`, request);
  }

  archive(id: string): Observable<CoverLetterResponse> {
    return this.http.patch<CoverLetterResponse>(`${this.baseUrl}/${id}/archive`, {});
  }

  unarchive(id: string): Observable<CoverLetterResponse> {
    return this.http.patch<CoverLetterResponse>(`${this.baseUrl}/${id}/unarchive`, {});
  }

  /** Permanent deletion — the backend restricts this to ADMIN regardless of what the UI shows. */
  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
