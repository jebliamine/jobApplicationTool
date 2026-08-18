import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CvResponse, DownloadedCv } from './cv.models';

@Injectable({ providedIn: 'root' })
export class CvService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/cv`;

  /** GET /cv — backend scopes this to the caller's own CVs, or all CVs for admins. */
  list(): Observable<CvResponse[]> {
    return this.http.get<CvResponse[]>(this.baseUrl);
  }

  /** POST /cv (multipart) — the browser sets the multipart boundary; never set Content-Type manually. */
  upload(file: File, title: string): Observable<CvResponse> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('title', title);
    return this.http.post<CvResponse>(this.baseUrl, formData);
  }

  /**
   * GET /cv/{id}/view — fetched as an authenticated blob (via the existing
   * interceptor) rather than a plain new-tab navigation, since a plain
   * navigation can't attach the Authorization header for a localStorage JWT.
   */
  view(id: string): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/${id}/view`, { responseType: 'blob' });
  }

  /** GET /cv/{id}/download — filename read from the response's Content-Disposition header, never hard-coded. */
  download(id: string): Observable<DownloadedCv> {
    return this.http
      .get(`${this.baseUrl}/${id}/download`, { observe: 'response', responseType: 'blob' })
      .pipe(
        map((response) => ({
          blob: response.body as Blob,
          filename: extractFilename(response.headers.get('Content-Disposition')) ?? 'cv',
        })),
      );
  }

  /** DELETE /cv/{id} */
  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}

function extractFilename(contentDisposition: string | null): string | null {
  if (!contentDisposition) {
    return null;
  }
  const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i);
  if (utf8Match) {
    try {
      return decodeURIComponent(utf8Match[1]);
    } catch {
      // fall through to the quoted form below
    }
  }
  const quotedMatch = contentDisposition.match(/filename="([^"]+)"/i);
  return quotedMatch ? quotedMatch[1] : null;
}
