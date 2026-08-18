import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, Subscription, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { UpdateUserRequest, UserProfile } from '../models/user.models';

type LoadState = 'idle' | 'loading' | 'loaded' | 'error';

/**
 * Holds the current user's profile (from GET/PUT /users/me) as Signals —
 * the single source of truth every component (profile page, topbar,
 * sidebar) reads from. Fetched at most once per session; `ensureLoaded()`
 * is meant to be called from the auth guard so components never need to
 * trigger the fetch themselves.
 */
@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/users`;

  private readonly state = signal<LoadState>('idle');
  private readonly _currentUser = signal<UserProfile | null>(null);

  readonly currentUser = this._currentUser.asReadonly();
  readonly loading = computed(() => this.state() === 'loading');
  readonly error = computed(() => this.state() === 'error');

  /**
   * Tracks the in-flight GET so a rapid logout/login (or two calls racing)
   * can't have an older response land after a newer one and overwrite it
   * with stale data — starting a new fetch (or clearing) always cancels
   * whatever fetch was still in flight.
   */
  private pendingFetch: Subscription | null = null;

  /** Fetches the profile if it hasn't been loaded (or isn't already loading). */
  ensureLoaded(): void {
    if (this.state() === 'idle' || this.state() === 'error') {
      this.fetch();
    }
  }

  /** Forces a re-fetch, e.g. after a failed load. */
  refresh(): void {
    this.fetch();
  }

  /** PUT /users/me — updates state from the response on success. */
  updateProfile(request: UpdateUserRequest): Observable<UserProfile> {
    return this.http.put<UserProfile>(`${this.baseUrl}/me`, request).pipe(
      tap((profile) => {
        this._currentUser.set(profile);
        this.state.set('loaded');
      }),
    );
  }

  /** Resets state — called on logout so stale data doesn't leak into the next session. */
  clear(): void {
    this.pendingFetch?.unsubscribe();
    this.pendingFetch = null;
    this.state.set('idle');
    this._currentUser.set(null);
  }

  private fetch(): void {
    this.pendingFetch?.unsubscribe();
    this.state.set('loading');
    this.pendingFetch = this.http.get<UserProfile>(`${this.baseUrl}/me`).subscribe({
      next: (profile) => {
        this._currentUser.set(profile);
        this.state.set('loaded');
      },
      error: () => {
        this._currentUser.set(null);
        this.state.set('error');
      },
    });
  }
}
