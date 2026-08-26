import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { ForgotPassword } from './forgot-password';

describe('ForgotPassword', () => {
  let fixture: ComponentFixture<ForgotPassword>;
  let component: ForgotPassword;
  let forgotPasswordSpy: ReturnType<typeof vi.fn>;

  function setup(result: any = of(undefined)) {
    forgotPasswordSpy = vi.fn().mockReturnValue(result);

    TestBed.configureTestingModule({
      imports: [ForgotPassword, NoopAnimationsModule],
      providers: [provideRouter([]), { provide: AuthService, useValue: { forgotPassword: forgotPasswordSpy } }],
    });

    fixture = TestBed.createComponent(ForgotPassword);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('does not submit an invalid email', () => {
    setup();
    component['form'].controls.email.setValue('not-an-email');

    component['submit']();

    expect(forgotPasswordSpy).not.toHaveBeenCalled();
  });

  it('shows the generic confirmation on success', () => {
    setup();
    component['form'].controls.email.setValue('jane@example.com');

    component['submit']();

    expect(forgotPasswordSpy).toHaveBeenCalledWith({ email: 'jane@example.com' });
    expect(component['sent']()).toBe(true);
  });

  it('shows a server error when the request fails outright', () => {
    setup(throwError(() => new HttpErrorResponse({ status: 0 })));
    component['form'].controls.email.setValue('jane@example.com');

    component['submit']();

    expect(component['sent']()).toBe(false);
    expect(component['serverError']()).not.toBeNull();
  });
});
