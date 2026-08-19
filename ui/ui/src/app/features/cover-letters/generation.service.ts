import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { GenerationRequestCreateRequest, GenerationRequestResponse } from './generation.models';

@Injectable({ providedIn: 'root' })
export class GenerationService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/generation-requests`;

  /** GET /generation-requests — backend scopes this to the caller's own requests, or all for admins. */
  list(): Observable<GenerationRequestResponse[]> {
    return this.http.get<GenerationRequestResponse[]>(this.baseUrl);
  }

  get(id: string): Observable<GenerationRequestResponse> {
    return this.http.get<GenerationRequestResponse>(`${this.baseUrl}/${id}`);
  }

  /**
   * POST /generation-requests — the placeholder generator completes
   * synchronously today, but the response always carries a status
   * (PENDING/IN_PROGRESS/COMPLETED/FAILED) so callers can treat this the
   * same way once a real, slower provider is introduced.
   */
  create(request: GenerationRequestCreateRequest): Observable<GenerationRequestResponse> {
    return this.http.post<GenerationRequestResponse>(this.baseUrl, request);
  }
}
