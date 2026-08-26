import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ExternalJobSearchResponse } from './job-search.models';

@Injectable({ providedIn: 'root' })
export class JobSearchService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/job-search`;

  /** GET /job-search — combines Adzuna/Jooble/JSearch; a source with no key configured just contributes zero results. */
  search(keyword: string, location: string, page = 1): Observable<ExternalJobSearchResponse> {
    const params: Record<string, string> = { page: String(page) };
    if (keyword.trim()) {
      params['keyword'] = keyword.trim();
    }
    if (location.trim()) {
      params['location'] = location.trim();
    }
    return this.http.get<ExternalJobSearchResponse>(this.baseUrl, { params });
  }
}
