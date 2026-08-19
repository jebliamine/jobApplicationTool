import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, provideRouter } from '@angular/router';
import { UserService } from '../user/user.service';
import { adminGuard } from './admin.guard';

function fakeToken(claims: Record<string, unknown>): string {
  return `${btoa(JSON.stringify({ alg: 'none' }))}.${btoa(JSON.stringify(claims))}.signature`;
}

describe('adminGuard', () => {
  afterEach(() => localStorage.clear());

  function run(role: 'ADMIN' | 'USER' | null, authenticated: boolean) {
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
        { provide: UserService, useValue: { currentUser: () => (role ? { role } : null) } },
      ],
    });

    return TestBed.runInInjectionContext(() =>
      adminGuard({} as ActivatedRouteSnapshot, { url: '/admin/ai-providers' } as RouterStateSnapshot),
    );
  }

  it('redirects unauthenticated users to /login', () => {
    const result = run(null, false) as UrlTree;

    expect(result).toBeInstanceOf(UrlTree);
    expect(result.toString()).toContain('/login');
  });

  it('allows ADMIN users through', () => {
    expect(run('ADMIN', true)).toBe(true);
  });

  it('redirects authenticated non-admin users to /dashboard', () => {
    const result = run('USER', true) as UrlTree;

    expect(result).toBeInstanceOf(UrlTree);
    expect(result.toString()).toContain('/dashboard');
  });
});
