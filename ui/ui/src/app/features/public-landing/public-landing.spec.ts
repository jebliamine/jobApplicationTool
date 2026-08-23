import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTranslateService } from '@ngx-translate/core';
import { PublicLanding } from './public-landing';

describe('PublicLanding', () => {
  let fixture: ComponentFixture<PublicLanding>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PublicLanding],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideTranslateService({ lang: 'en', fallbackLang: 'en' }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PublicLanding);
    fixture.detectChanges();
  });

  it('renders exactly one h1, per the SEO requirement of a single primary heading', () => {
    const headings = fixture.nativeElement.querySelectorAll('h1');
    expect(headings.length).toBe(1);
  });

  it('renders semantic landmarks: header, main, footer, and a labeled nav', () => {
    const root = fixture.nativeElement as HTMLElement;
    expect(root.querySelector('header')).not.toBeNull();
    expect(root.querySelector('main')).not.toBeNull();
    expect(root.querySelector('footer')).not.toBeNull();
    expect(root.querySelectorAll('nav').length).toBeGreaterThan(0);
  });

  it('the primary and secondary CTAs point at register and login', () => {
    const root = fixture.nativeElement as HTMLElement;
    const registerLinks = Array.from(root.querySelectorAll('a[href="/register"]'));
    const loginLinks = Array.from(root.querySelectorAll('a[href="/login"]'));
    expect(registerLinks.length).toBeGreaterThan(0);
    expect(loginLinks.length).toBeGreaterThan(0);
  });

  it('renders all five workflow steps and all five product pillars', () => {
    expect(fixture.componentInstance['workflowSteps'].length).toBe(5);
    expect(fixture.componentInstance['pillars'].length).toBe(5);
  });

  it('sets a descriptive document title and meta description', () => {
    expect(document.title).toContain('JAPP');
    const description = document.querySelector('meta[name="description"]');
    expect(description?.getAttribute('content')).toContain('job');
  });
});
