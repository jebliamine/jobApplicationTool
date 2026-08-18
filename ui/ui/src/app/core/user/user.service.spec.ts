import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { UserProfile } from '../models/user.models';
import { UserService } from './user.service';

const PROFILE: UserProfile = { fullName: 'Jane Doe', email: 'jane@example.com', role: 'USER' };

describe('UserService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('starts idle with no current user', () => {
    expect(service.currentUser()).toBeNull();
    expect(service.loading()).toBe(false);
    expect(service.error()).toBe(false);
  });

  it('ensureLoaded fetches the profile and populates currentUser', () => {
    service.ensureLoaded();
    expect(service.loading()).toBe(true);

    const req = httpMock.expectOne(`${environment.apiUrl}/users/me`);
    expect(req.request.method).toBe('GET');
    req.flush(PROFILE);

    expect(service.loading()).toBe(false);
    expect(service.currentUser()).toEqual(PROFILE);
  });

  it('ensureLoaded does not refetch once already loaded', () => {
    service.ensureLoaded();
    httpMock.expectOne(`${environment.apiUrl}/users/me`).flush(PROFILE);

    service.ensureLoaded();

    httpMock.expectNone(`${environment.apiUrl}/users/me`);
  });

  it('sets error state and clears currentUser on a failed fetch', () => {
    service.ensureLoaded();
    httpMock.expectOne(`${environment.apiUrl}/users/me`).flush(
      { message: 'nope' },
      { status: 401, statusText: 'Unauthorized' },
    );

    expect(service.error()).toBe(true);
    expect(service.loading()).toBe(false);
    expect(service.currentUser()).toBeNull();
  });

  it('refresh re-fetches even after a successful load', () => {
    service.ensureLoaded();
    httpMock.expectOne(`${environment.apiUrl}/users/me`).flush(PROFILE);

    service.refresh();

    httpMock.expectOne(`${environment.apiUrl}/users/me`).flush(PROFILE);
  });

  it('clear resets state back to idle', () => {
    service.ensureLoaded();
    httpMock.expectOne(`${environment.apiUrl}/users/me`).flush(PROFILE);

    service.clear();

    expect(service.currentUser()).toBeNull();
    expect(service.loading()).toBe(false);
    expect(service.error()).toBe(false);
  });

  it('updateProfile PUTs to /users/me and replaces currentUser with the response', () => {
    service.ensureLoaded();
    httpMock.expectOne(`${environment.apiUrl}/users/me`).flush(PROFILE);

    const updated: UserProfile = { ...PROFILE, fullName: 'Jane Updated' };
    service.updateProfile({ fullName: 'Jane Updated', email: PROFILE.email }).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/users/me`);
    expect(req.request.method).toBe('PUT');
    req.flush(updated);

    expect(service.currentUser()).toEqual(updated);
  });

  it('cancels a stale in-flight fetch instead of letting it overwrite newer data', () => {
    // Simulates logout+login-as-a-different-user while the first GET is still in flight:
    // a naive implementation would let whichever response arrives last win.
    service.ensureLoaded();
    const first = httpMock.expectOne(`${environment.apiUrl}/users/me`);

    service.clear();
    service.ensureLoaded();
    const second = httpMock.expectOne(`${environment.apiUrl}/users/me`);

    // The first request was cancelled when the second one started, so its
    // response — even if the (real) backend still sent one — can never land.
    expect(first.cancelled).toBe(true);

    second.flush(PROFILE);
    expect(service.currentUser()).toEqual(PROFILE);
  });
});
