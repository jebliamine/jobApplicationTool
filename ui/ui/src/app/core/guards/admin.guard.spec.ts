import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, provideRouter } from '@angular/router';
import { Observable, firstValueFrom, isObservable } from 'rxjs';
import { UserService } from '../user/user.service';
import { adminGuard } from './admin.guard';

function fakeToken(claims: Record<string, unknown>): string {
  return `${btoa(JSON.stringify({ alg: 'none' }))}.${btoa(JSON.stringify(claims))}.signature`;
}

/** The guard returns a plain value once loading is already settled, or an Observable while it waits. */
async function resolve(result: unknown): Promise<boolean | UrlTree> {
  return isObservable(result)
    ? firstValueFrom(result as Observable<boolean | UrlTree>)
    : ((await result) as boolean | UrlTree);
}

describe('adminGuard', () => {
  afterEach(() => localStorage.clear());

  function run(role: 'ADMIN' | 'USER' | null, authenticated: boolean, loading = signal(false)) {
    localStorage.clear();
    if (authenticated) {
      localStorage.setItem(
        'japp-auth-token',
        fakeToken({ sub: 'a@b.com', exp: Math.floor(Date.now() / 1000) + 3600 }),
      );
    }

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: UserService, useValue: { currentUser: () => (role ? { role } : null), loading } },
      ],
    });

    return TestBed.runInInjectionContext(() =>
      adminGuard({} as ActivatedRouteSnapshot, { url: '/admin/ai-providers' } as RouterStateSnapshot),
    );
  }

  it('redirects unauthenticated users to /login', async () => {
    const result = (await resolve(run(null, false))) as UrlTree;

    expect(result).toBeInstanceOf(UrlTree);
    expect(result.toString()).toContain('/login');
  });

  it('allows ADMIN users through once the profile has loaded', async () => {
    expect(await resolve(run('ADMIN', true))).toBe(true);
  });

  it('redirects authenticated non-admin users to /dashboard', async () => {
    const result = (await resolve(run('USER', true))) as UrlTree;

    expect(result).toBeInstanceOf(UrlTree);
    expect(result.toString()).toContain('/dashboard');
  });

  it('waits for an in-flight profile fetch to settle before deciding, instead of reading a still-null value', async () => {
    // Regression test: authGuard's ensureLoaded() is fire-and-forget, so on a
    // fresh load of an admin URL, currentUser() can still be null while
    // loading() is true. The guard must not decide until loading flips false.
    const loading = signal(true);
    const resultPromise = resolve(run('ADMIN', true, loading));

    loading.set(false);

    expect(await resultPromise).toBe(true);
  });
});
