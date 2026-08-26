import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { VerifyEmail } from './verify-email';

describe('VerifyEmail', () => {
  let fixture: ComponentFixture<VerifyEmail>;
  let component: VerifyEmail;

  function setup(options: { token?: string | null; result?: any } = {}) {
    const { token = 'the-token', result = of(undefined) } = options;
    const verifyEmailSpy = vi.fn().mockReturnValue(result);

    TestBed.configureTestingModule({
      imports: [VerifyEmail],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: { verifyEmail: verifyEmailSpy } },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { queryParamMap: convertToParamMap(token ? { token } : {}) } },
        },
      ],
    });

    fixture = TestBed.createComponent(VerifyEmail);
    component = fixture.componentInstance;
    fixture.detectChanges();
    return verifyEmailSpy;
  }

  it('shows a missing-token state without calling the backend when there is no token', () => {
    const verifyEmailSpy = setup({ token: null });

    expect(component['state']()).toBe('missing-token');
    expect(verifyEmailSpy).not.toHaveBeenCalled();
  });

  it('calls verifyEmail with the token from the URL and shows success', () => {
    const verifyEmailSpy = setup();

    expect(verifyEmailSpy).toHaveBeenCalledWith({ token: 'the-token' });
    expect(component['state']()).toBe('success');
  });

  it('shows an error state when the token is invalid or expired', () => {
    setup({
      result: throwError(() => new HttpErrorResponse({ status: 400, error: { message: 'This verification link has expired.' } })),
    });

    expect(component['state']()).toBe('error');
    expect(component['errorMessage']()).toBe('This verification link has expired.');
  });
});
