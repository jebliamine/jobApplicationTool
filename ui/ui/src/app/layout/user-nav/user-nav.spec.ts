import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTranslateService } from '@ngx-translate/core';
import { UserNav } from './user-nav';

describe('UserNav', () => {
  let fixture: ComponentFixture<UserNav>;
  let component: UserNav;

  beforeEach(async () => {
    // jsdom has no matchMedia implementation — ThemeService (used transitively via
    // ThemeToggle, rendered inside UserNav) calls it in its constructor.
    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      configurable: true,
      value: (query: string) => ({
        matches: false,
        media: query,
        addEventListener: () => {},
        removeEventListener: () => {},
        // Legacy API — Angular CDK's BreakpointObserver (used transitively by
        // Material components like mat-menu) still calls these.
        addListener: () => {},
        removeListener: () => {},
      }),
    });

    await TestBed.configureTestingModule({
      imports: [UserNav],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideTranslateService({ lang: 'en', fallbackLang: 'en' }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(UserNav);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('renders a link for every workspace page, and never for settings or admin pages', () => {
    const root = fixture.nativeElement as HTMLElement;
    const hrefs = Array.from(root.querySelectorAll('.user-nav__link')).map((a) => a.getAttribute('href'));
    expect(hrefs).toEqual(
      expect.arrayContaining(['/dashboard', '/cv', '/jobs', '/companies', '/applications', '/cover-letters']),
    );
    expect(hrefs).not.toContain('/settings');
    expect(hrefs.some((href) => href?.startsWith('/admin'))).toBe(false);
  });

  it('the mobile menu is closed by default and toggles open/closed', () => {
    expect(component['mobileMenuOpen']()).toBe(false);
    expect(fixture.nativeElement.querySelector('.user-nav__mobile-panel')).toBeNull();

    component['toggleMobileMenu']();
    fixture.detectChanges();

    expect(component['mobileMenuOpen']()).toBe(true);
    expect(fixture.nativeElement.querySelector('.user-nav__mobile-panel')).not.toBeNull();
  });

  it('closeMobileMenu closes an open panel', () => {
    component['toggleMobileMenu']();
    expect(component['mobileMenuOpen']()).toBe(true);

    component['closeMobileMenu']();

    expect(component['mobileMenuOpen']()).toBe(false);
  });
});
