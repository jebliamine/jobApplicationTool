import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  AdminAiProviderResponse,
  AiProviderCreateRequest,
  AiProviderTestResult,
  AiProviderUpdateRequest,
} from './ai-provider.models';

/** ADMIN-only backend enforces authorization — see AdminAiProviderService (backend). */
@Injectable({ providedIn: 'root' })
export class AdminAiProviderService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/admin/ai/providers`;

  list(): Observable<AdminAiProviderResponse[]> {
    return this.http.get<AdminAiProviderResponse[]>(this.baseUrl);
  }

  create(request: AiProviderCreateRequest): Observable<AdminAiProviderResponse> {
    return this.http.post<AdminAiProviderResponse>(this.baseUrl, request);
  }

  update(id: string, request: AiProviderUpdateRequest): Observable<AdminAiProviderResponse> {
    return this.http.put<AdminAiProviderResponse>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  test(id: string): Observable<AiProviderTestResult> {
    return this.http.post<AiProviderTestResult>(`${this.baseUrl}/${id}/test`, {});
  }
}
