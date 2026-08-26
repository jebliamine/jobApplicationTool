import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';

function fakeToken(claims: Record<string, unknown>): string {
  const header = btoa(JSON.stringify({ alg: 'none' }));
  const payload = btoa(JSON.stringify(claims));
  return `${header}.${payload}.signature`;
}

describe('AuthService', () => {
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('starts unauthenticated when there is no stored token', () => {
    const service = TestBed.inject(AuthService);

    expect(service.isAuthenticated()).toBe(false);
    expect(service.getToken()).toBeNull();
  });

  it('stores the token and becomes authenticated after login', () => {
    const service = TestBed.inject(AuthService);
    const token = fakeToken({ sub: 'jane@example.com', exp: Math.floor(Date.now() / 1000) + 3600 });

    service.login({ email: 'jane@example.com', password: 'secret' }).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
    expect(req.request.method).toBe('POST');
    req.flush({ token });

    expect(service.isAuthenticated()).toBe(true);
    expect(service.getToken()).toBe(token);
    expect(service.currentUserEmail()).toBe('jane@example.com');
    expect(localStorage.getItem('japp-auth-token')).toBe(token);
  });

  it('stores the token after register', () => {
    const service = TestBed.inject(AuthService);
    const token = fakeToken({ sub: 'new@example.com', exp: Math.floor(Date.now() / 1000) + 3600 });

    service
      .register({ fullName: 'New User', email: 'new@example.com', password: 'secret' })
      .subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/auth/register`);
    expect(req.request.method).toBe('POST');
    req.flush({ token });

    expect(service.isAuthenticated()).toBe(true);
  });

  it('treats an expired stored token as unauthenticated', () => {
    const expiredToken = fakeToken({
      sub: 'jane@example.com',
      exp: Math.floor(Date.now() / 1000) - 10,
    });
    localStorage.setItem('japp-auth-token', expiredToken);

    const service = TestBed.inject(AuthService);

    expect(service.isAuthenticated()).toBe(false);
  });

  it('forgotPassword() POSTs /auth/forgot-password', () => {
    const service = TestBed.inject(AuthService);

    service.forgotPassword({ email: 'jane@example.com' }).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/auth/forgot-password`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'jane@example.com' });
    req.flush(null);
  });

  it('resetPassword() POSTs /auth/reset-password', () => {
    const service = TestBed.inject(AuthService);

    service.resetPassword({ token: 'abc123', newPassword: 'new-password-123' }).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/auth/reset-password`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ token: 'abc123', newPassword: 'new-password-123' });
    req.flush(null);
  });

  it('verifyEmail() POSTs /auth/verify-email', () => {
    const service = TestBed.inject(AuthService);

    service.verifyEmail({ token: 'xyz789' }).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/auth/verify-email`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ token: 'xyz789' });
    req.flush(null);
  });

  it('resendVerification() POSTs /auth/resend-verification', () => {
    const service = TestBed.inject(AuthService);

    service.resendVerification({ email: 'jane@example.com' }).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/auth/resend-verification`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'jane@example.com' });
    req.flush(null);
  });

  it('clears the token on logout', () => {
    const service = TestBed.inject(AuthService);
    const token = fakeToken({ sub: 'jane@example.com', exp: Math.floor(Date.now() / 1000) + 3600 });
    service.login({ email: 'jane@example.com', password: 'secret' }).subscribe();
    httpMock.expectOne(`${environment.apiUrl}/auth/login`).flush({ token });

    service.logout();

    expect(service.isAuthenticated()).toBe(false);
    expect(localStorage.getItem('japp-auth-token')).toBeNull();
  });
});
