import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse, LoginRequest, RegisterRequest } from '../models/auth.models';

const TOKEN_STORAGE_KEY = 'japp-auth-token';

interface JwtPayload {
  sub?: string;
  exp?: number;
}

/**
 * Holds authentication state as Signals and talks to POST /auth/login and
 * /auth/register. The backend only ever returns `{ token }` — there is no
 * user profile endpoint yet, so `currentUserEmail` is read back out of the
 * JWT `sub` claim (the same claim JwtService.generateToken sets) purely for
 * display purposes; it is never trusted for authorization decisions.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/auth`;

  private readonly token = signal<string | null>(this.readStoredToken());

  readonly isAuthenticated = computed(() => {
    const token = this.token();
    return token !== null && !isTokenExpired(token);
  });

  readonly currentUserEmail = computed(() => {
    const token = this.token();
    return token ? (decodeJwtPayload(token)?.sub ?? null) : null;
  });

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.baseUrl}/login`, request)
      .pipe(tap((response) => this.setToken(response.token)));
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.baseUrl}/register`, request)
      .pipe(tap((response) => this.setToken(response.token)));
  }

  logout(): void {
    this.token.set(null);
    localStorage.removeItem(TOKEN_STORAGE_KEY);
  }

  getToken(): string | null {
    return this.token();
  }

  private setToken(token: string): void {
    this.token.set(token);
    localStorage.setItem(TOKEN_STORAGE_KEY, token);
  }

  private readStoredToken(): string | null {
    return localStorage.getItem(TOKEN_STORAGE_KEY);
  }
}

function decodeJwtPayload(token: string): JwtPayload | null {
  const [, payload] = token.split('.');
  if (!payload) {
    return null;
  }
  try {
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
    return JSON.parse(atob(base64)) as JwtPayload;
  } catch {
    return null;
  }
}

function isTokenExpired(token: string): boolean {
  const payload = decodeJwtPayload(token);
  if (!payload?.exp) {
    return false;
  }
  return Date.now() >= payload.exp * 1000;
}
