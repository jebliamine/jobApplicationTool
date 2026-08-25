import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { TagRequest, TagResponse } from './tag.models';

/** Tags are per-user (each user manages their own vocabulary), shared across jobs and applications. */
@Injectable({ providedIn: 'root' })
export class TagService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/tags`;

  /** GET /tags — backend scopes this to the caller's own tags, or all tags for admins. */
  list(): Observable<TagResponse[]> {
    return this.http.get<TagResponse[]>(this.baseUrl);
  }

  create(request: TagRequest): Observable<TagResponse> {
    return this.http.post<TagResponse>(this.baseUrl, request);
  }

  rename(id: string, request: TagRequest): Observable<TagResponse> {
    return this.http.put<TagResponse>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
