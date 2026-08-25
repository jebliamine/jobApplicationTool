import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApplicationRequest, ApplicationResponse, InterviewStageRequest } from './application.models';

@Injectable({ providedIn: 'root' })
export class ApplicationService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/applications`;

  /** GET /applications — backend scopes this to the caller's own applications, or all applications for admins. */
  list(): Observable<ApplicationResponse[]> {
    return this.http.get<ApplicationResponse[]>(this.baseUrl);
  }

  get(id: string): Observable<ApplicationResponse> {
    return this.http.get<ApplicationResponse>(`${this.baseUrl}/${id}`);
  }

  create(request: ApplicationRequest): Observable<ApplicationResponse> {
    return this.http.post<ApplicationResponse>(this.baseUrl, request);
  }

  update(id: string, request: ApplicationRequest): Observable<ApplicationResponse> {
    return this.http.put<ApplicationResponse>(`${this.baseUrl}/${id}`, request);
  }

  /** PUT /applications/{id}/tags — replaces the application's full tag set with the given tag ids. */
  setTags(id: string, tagIds: string[]): Observable<ApplicationResponse> {
    return this.http.put<ApplicationResponse>(`${this.baseUrl}/${id}/tags`, tagIds);
  }

  /** POST /applications/{id}/interview-stages — adds one new round to the interview pipeline. */
  addInterviewStage(id: string, request: InterviewStageRequest): Observable<ApplicationResponse> {
    return this.http.post<ApplicationResponse>(`${this.baseUrl}/${id}/interview-stages`, request);
  }

  updateInterviewStage(id: string, stageId: string, request: InterviewStageRequest): Observable<ApplicationResponse> {
    return this.http.put<ApplicationResponse>(`${this.baseUrl}/${id}/interview-stages/${stageId}`, request);
  }

  removeInterviewStage(id: string, stageId: string): Observable<ApplicationResponse> {
    return this.http.delete<ApplicationResponse>(`${this.baseUrl}/${id}/interview-stages/${stageId}`);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
