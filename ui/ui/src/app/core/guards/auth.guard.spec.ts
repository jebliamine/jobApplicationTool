import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, provideRouter } from '@angular/router';
import { authGuard, guestGuard } from './auth.guard';

function fakeToken(claims: Record<string, unknown>): string {
  return `${btoa(JSON.stringify({ alg: 'none' }))}.${btoa(JSON.stringify(claims))}.signature`;
}

describe('authGuard / guestGuard', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
  });

  afterEach(() => localStorage.clear());

  function run<T>(guard: (route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => T): T {
    return TestBed.runInInjectionContext(() =>
      guard({} as ActivatedRouteSnapshot, { url: '/dashboard' } as RouterStateSnapshot),
    );
  }

  it('authGuard redirects unauthenticated users to /login with a redirectTo param', () => {
    const result = run(authGuard) as UrlTree;

    expect(result).toBeInstanceOf(UrlTree);
    expect(result.toString()).toContain('/login');
    expect(result.toString()).toContain('redirectTo=%2Fdashboard');
  });

  it('authGuard allows authenticated users through', () => {
    localStorage.setItem(
      'japp-auth-token',
      fakeToken({ sub: 'a@b.com', exp: Math.floor(Date.now() / 1000) + 3600 }),
    );

    const result = run(authGuard);

    expect(result).toBe(true);
  });

  it('guestGuard allows unauthenticated users through', () => {
    const result = run(guestGuard);
    expect(result).toBe(true);
  });

  it('guestGuard redirects already-authenticated users to /dashboard', () => {
    localStorage.setItem(
      'japp-auth-token',
      fakeToken({ sub: 'a@b.com', exp: Math.floor(Date.now() / 1000) + 3600 }),
    );

    const result = run(guestGuard) as UrlTree;

    expect(result).toBeInstanceOf(UrlTree);
    expect(result.toString()).toContain('/dashboard');
  });
});
