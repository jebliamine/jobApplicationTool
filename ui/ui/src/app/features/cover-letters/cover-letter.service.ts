import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CoverLetterResponse, CoverLetterUpdateRequest } from './cover-letter.models';

@Injectable({ providedIn: 'root' })
export class CoverLetterService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/cover-letters`;

  /** GET /cover-letters — backend scopes this to the caller's own cover letters, or all for admins. */
  list(): Observable<CoverLetterResponse[]> {
    return this.http.get<CoverLetterResponse[]>(this.baseUrl);
  }

  get(id: string): Observable<CoverLetterResponse> {
    return this.http.get<CoverLetterResponse>(`${this.baseUrl}/${id}`);
  }

  update(id: string, request: CoverLetterUpdateRequest): Observable<CoverLetterResponse> {
    return this.http.put<CoverLetterResponse>(`${this.baseUrl}/${id}`, request);
  }
}
