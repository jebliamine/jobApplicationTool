import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { ResetPassword } from './reset-password';

describe('ResetPassword', () => {
  let fixture: ComponentFixture<ResetPassword>;
  let component: ResetPassword;
  let resetPasswordSpy: ReturnType<typeof vi.fn>;

  function setup(options: { token?: string | null; result?: any } = {}) {
    const { token = 'the-token', result = of(undefined) } = options;
    resetPasswordSpy = vi.fn().mockReturnValue(result);

    TestBed.configureTestingModule({
      imports: [ResetPassword, NoopAnimationsModule],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: { resetPassword: resetPasswordSpy } },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { queryParamMap: convertToParamMap(token ? { token } : {}) } },
        },
      ],
    });

    fixture = TestBed.createComponent(ResetPassword);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('shows a missing-token state when there is no token in the URL', () => {
    setup({ token: null });

    expect(component['hasToken']).toBe(false);
  });

  it('does not submit when the passwords do not match', () => {
    setup();
    component['form'].controls.newPassword.setValue('password-123');
    component['form'].controls.confirmPassword.setValue('different-123');

    component['submit']();

    expect(resetPasswordSpy).not.toHaveBeenCalled();
    expect(component['form'].hasError('passwordMismatch')).toBe(true);
  });

  it('does not submit a password shorter than 8 characters', () => {
    setup();
    component['form'].controls.newPassword.setValue('short1');
    component['form'].controls.confirmPassword.setValue('short1');

    component['submit']();

    expect(resetPasswordSpy).not.toHaveBeenCalled();
  });

  it('submits the token and new password, and shows success', () => {
    setup();
    component['form'].controls.newPassword.setValue('password-123');
    component['form'].controls.confirmPassword.setValue('password-123');

    component['submit']();

    expect(resetPasswordSpy).toHaveBeenCalledWith({ token: 'the-token', newPassword: 'password-123' });
    expect(component['done']()).toBe(true);
  });

  it('shows a server error when the token is invalid or expired', () => {
    setup({ result: throwError(() => new HttpErrorResponse({ status: 400, error: { message: 'This reset link has expired.' } })) });
    component['form'].controls.newPassword.setValue('password-123');
    component['form'].controls.confirmPassword.setValue('password-123');

    component['submit']();

    expect(component['done']()).toBe(false);
    expect(component['serverError']()).toBe('This reset link has expired.');
  });
});
