import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CompanyRequest, CompanyResponse } from './company.models';

@Injectable({ providedIn: 'root' })
export class CompanyService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/companies`;

  /** GET /companies — backend scopes this to the caller's own companies, or all companies for admins. */
  list(): Observable<CompanyResponse[]> {
    return this.http.get<CompanyResponse[]>(this.baseUrl);
  }

  get(id: string): Observable<CompanyResponse> {
    return this.http.get<CompanyResponse>(`${this.baseUrl}/${id}`);
  }

  create(request: CompanyRequest): Observable<CompanyResponse> {
    return this.http.post<CompanyResponse>(this.baseUrl, request);
  }

  update(id: string, request: CompanyRequest): Observable<CompanyResponse> {
    return this.http.put<CompanyResponse>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
