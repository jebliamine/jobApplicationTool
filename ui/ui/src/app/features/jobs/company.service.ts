import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CompanyRequest, CompanyResponse } from './company.models';

/**
 * Companies have no dedicated page yet — they're created/selected from
 * within the Job form, so only list() and create() are needed for now.
 */
@Injectable({ providedIn: 'root' })
export class CompanyService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/companies`;

  list(): Observable<CompanyResponse[]> {
    return this.http.get<CompanyResponse[]>(this.baseUrl);
  }

  create(request: CompanyRequest): Observable<CompanyResponse> {
    return this.http.post<CompanyResponse>(this.baseUrl, request);
  }
}
