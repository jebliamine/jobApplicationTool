import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { SearchResultResponse } from './search.models';

@Injectable({ providedIn: 'root' })
export class SearchService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/search`;

  /** GET /search?q=... — backend scopes this to the caller's own jobs/companies/applications/cover letters, or everyone's for admins. */
  search(query: string): Observable<SearchResultResponse[]> {
    return this.http.get<SearchResultResponse[]>(this.baseUrl, { params: { q: query } });
  }
}
