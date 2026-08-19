import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApplicationRequest, ApplicationResponse } from './application.models';

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

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
