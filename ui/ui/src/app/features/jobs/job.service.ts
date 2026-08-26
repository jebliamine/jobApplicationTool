import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { JobExtractionRequest, JobExtractionResponse, JobRequest, JobResponse } from './job.models';

@Injectable({ providedIn: 'root' })
export class JobService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/jobs`;

  /** GET /jobs — backend scopes this to the caller's own jobs, or all jobs for admins. */
  list(): Observable<JobResponse[]> {
    return this.http.get<JobResponse[]>(this.baseUrl);
  }

  get(id: string): Observable<JobResponse> {
    return this.http.get<JobResponse>(`${this.baseUrl}/${id}`);
  }

  create(request: JobRequest): Observable<JobResponse> {
    return this.http.post<JobResponse>(this.baseUrl, request);
  }

  /** POST /jobs/extract — stateless preview, nothing is persisted; providerId omitted uses the built-in Placeholder. */
  extract(rawText: string, providerId?: string): Observable<JobExtractionResponse> {
    const request: JobExtractionRequest = { rawText };
    const params = providerId ? { providerId } : undefined;
    return this.http.post<JobExtractionResponse>(`${this.baseUrl}/extract`, request, { params });
  }

  update(id: string, request: JobRequest): Observable<JobResponse> {
    return this.http.put<JobResponse>(`${this.baseUrl}/${id}`, request);
  }

  /** PUT /jobs/{id}/tags — replaces the job's full tag set with the given tag ids. */
  setTags(id: string, tagIds: string[]): Observable<JobResponse> {
    return this.http.put<JobResponse>(`${this.baseUrl}/${id}/tags`, tagIds);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
