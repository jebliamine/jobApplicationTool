import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTranslateService } from '@ngx-translate/core';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { ToastService } from '../../../core/ui/toast.service';
import { UserProfile } from '../../../core/models/user.models';
import { UserService } from '../../../core/user/user.service';
import { AccountSection } from './account-section';

const VERIFIED_USER: UserProfile = {
  fullName: 'Jane Doe',
  email: 'jane@example.com',
  role: 'USER',
  emailVerified: true,
};

const UNVERIFIED_USER: UserProfile = { ...VERIFIED_USER, emailVerified: false };

describe('AccountSection', () => {
  let fixture: ComponentFixture<AccountSection>;
  let component: AccountSection;
  let resendSpy: ReturnType<typeof vi.fn>;
  let toastSuccessSpy: ReturnType<typeof vi.fn>;
  let toastErrorSpy: ReturnType<typeof vi.fn>;

  function setup(user: UserProfile | null, resendResult: any = of(undefined)) {
    resendSpy = vi.fn().mockReturnValue(resendResult);
    toastSuccessSpy = vi.fn();
    toastErrorSpy = vi.fn();

    TestBed.configureTestingModule({
      imports: [AccountSection, NoopAnimationsModule],
      providers: [
        provideTranslateService({ lang: 'en', fallbackLang: 'en' }),
        {
          provide: UserService,
          useValue: {
            currentUser: () => user,
            loading: () => false,
            error: () => false,
            refresh: vi.fn(),
            avatarObjectUrl: () => null,
          },
        },
        { provide: AuthService, useValue: { resendVerification: resendSpy } },
        { provide: ToastService, useValue: { success: toastSuccessSpy, error: toastErrorSpy } },
      ],
    });

    fixture = TestBed.createComponent(AccountSection);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('shows the unverified banner when the account is unverified', () => {
    setup(UNVERIFIED_USER);

    expect(fixture.nativeElement.querySelector('.account-section__unverified')).not.toBeNull();
  });

  it('hides the unverified banner when the account is verified', () => {
    setup(VERIFIED_USER);

    expect(fixture.nativeElement.querySelector('.account-section__unverified')).toBeNull();
  });

  it('resendVerification sends the current user email and shows a success toast', () => {
    setup(UNVERIFIED_USER);

    component['resendVerification']();

    expect(resendSpy).toHaveBeenCalledWith({ email: 'jane@example.com' });
    expect(toastSuccessSpy).toHaveBeenCalled();
  });

  it('shows an error toast when resending fails', () => {
    setup(UNVERIFIED_USER, throwError(() => new Error('boom')));

    component['resendVerification']();

    expect(toastErrorSpy).toHaveBeenCalled();
  });

  it('does nothing when already resending', () => {
    setup(UNVERIFIED_USER);
    component['resendingVerification'].set(true);

    component['resendVerification']();

    expect(resendSpy).not.toHaveBeenCalled();
  });
});
