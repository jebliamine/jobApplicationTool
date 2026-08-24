import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { AdminCreateUserRequest, AdminUserResponse } from './user-management.models';

@Injectable({ providedIn: 'root' })
export class UserManagementService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/admin/users`;

  /** GET /admin/users */
  list(): Observable<AdminUserResponse[]> {
    return this.http.get<AdminUserResponse[]>(this.baseUrl);
  }

  /** POST /admin/users */
  create(request: AdminCreateUserRequest): Observable<AdminUserResponse> {
    return this.http.post<AdminUserResponse>(this.baseUrl, request);
  }

  /** DELETE /admin/users/{id} — rejected (400) if the user still owns any data; disable instead. */
  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  /** PUT /admin/users/{id}/role */
  updateRole(id: string, role: 'USER' | 'ADMIN'): Observable<AdminUserResponse> {
    return this.http.put<AdminUserResponse>(`${this.baseUrl}/${id}/role`, { role });
  }

  /** PUT /admin/users/{id}/enabled */
  updateEnabled(id: string, enabled: boolean): Observable<AdminUserResponse> {
    return this.http.put<AdminUserResponse>(`${this.baseUrl}/${id}/enabled`, { enabled });
  }
}
