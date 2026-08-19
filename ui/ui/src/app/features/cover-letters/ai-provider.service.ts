import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AiProviderResponse } from './ai-provider.models';

/** Authenticated, not admin-only — lets the generation form load providers dynamically instead of hardcoding them. */
@Injectable({ providedIn: 'root' })
export class AiProviderService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/ai/providers`;

  list(): Observable<AiProviderResponse[]> {
    return this.http.get<AiProviderResponse[]>(this.baseUrl);
  }
}
