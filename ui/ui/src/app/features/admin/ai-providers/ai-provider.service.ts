import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { AdminAiProviderResponse, AiProviderTestResult, AiProviderUpdateRequest } from './ai-provider.models';

/** ADMIN-only backend enforces authorization — see AdminAiProviderService (backend). */
@Injectable({ providedIn: 'root' })
export class AdminAiProviderService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/admin/ai/providers`;

  list(): Observable<AdminAiProviderResponse[]> {
    return this.http.get<AdminAiProviderResponse[]>(this.baseUrl);
  }

  update(provider: string, request: AiProviderUpdateRequest): Observable<AdminAiProviderResponse> {
    return this.http.put<AdminAiProviderResponse>(`${this.baseUrl}/${provider}`, request);
  }

  test(provider: string): Observable<AiProviderTestResult> {
    return this.http.post<AiProviderTestResult>(`${this.baseUrl}/${provider}/test`, {});
  }
}
