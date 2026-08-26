import { HttpClient } from '@angular/common/http';
import { Injectable, computed, effect, inject, signal, untracked } from '@angular/core';
import { Observable, Subscription, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ChangePasswordRequest, UpdateUserRequest, UserProfile } from '../models/user.models';

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

  /**
   * The avatar endpoint requires auth, so a plain `<img [src]>` can't hit it directly (no way to
   * attach the Authorization header to an <img> request) — instead it's fetched as a Blob (auth
   * interceptor attaches the header the same as any other HttpClient call) and re-exposed as an
   * object URL. Re-derived automatically whenever currentUser().avatarUrl changes.
   */
  private readonly _avatarObjectUrl = signal<string | null>(null);
  readonly avatarObjectUrl = this._avatarObjectUrl.asReadonly();

  constructor() {
    effect(() => {
      this.loadAvatarBlob(this._currentUser()?.avatarUrl ?? null);
    });
  }

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

  /** PUT /users/me/password — no response body; current-user state is unaffected. */
  changePassword(request: ChangePasswordRequest): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/me/password`, request);
  }

  /** POST /users/me/avatar (multipart) — updates state from the response, same as updateProfile. */
  uploadAvatar(file: File): Observable<UserProfile> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<UserProfile>(`${this.baseUrl}/me/avatar`, formData).pipe(
      tap((profile) => {
        this._currentUser.set(profile);
        this.state.set('loaded');
      }),
    );
  }

  /** DELETE /users/me/avatar — reverts to the initials avatar. */
  deleteAvatar(): Observable<UserProfile> {
    return this.http.delete<UserProfile>(`${this.baseUrl}/me/avatar`).pipe(
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

  private loadAvatarBlob(avatarPath: string | null): void {
    // untracked: this method only ever runs inside the effect() above, which is tracking
    // currentUser().avatarUrl — reading _avatarObjectUrl() here too (even just to revoke the old
    // one) would register it as a second dependency, and setting it below would then re-trigger
    // this same effect, in an infinite refetch loop.
    const previous = untracked(this._avatarObjectUrl);
    if (previous) {
      URL.revokeObjectURL(previous);
      this._avatarObjectUrl.set(null);
    }
    if (!avatarPath) {
      return;
    }

    this.http.get(`${environment.apiUrl}${avatarPath}`, { responseType: 'blob' }).subscribe({
      next: (blob) => this._avatarObjectUrl.set(URL.createObjectURL(blob)),
      // A stale avatarUrl (e.g. the file was deleted directly on disk) just falls back to initials.
      error: () => this._avatarObjectUrl.set(null),
    });
  }
}
