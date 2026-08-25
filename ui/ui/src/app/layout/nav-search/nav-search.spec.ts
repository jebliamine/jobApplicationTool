import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { provideTranslateService } from '@ngx-translate/core';
import { of } from 'rxjs';
import { AppPage } from '../../core/navigation/app-pages';
import { SearchResultResponse } from '../../core/search/search.models';
import { SearchService } from '../../core/search/search.service';
import { NavSearch } from './nav-search';

const PAGES: AppPage[] = [
  { label: 'nav.dashboard', path: '/dashboard', icon: 'dashboard' },
  { label: 'nav.jobs', path: '/jobs', icon: 'jobs' },
];

const JOB_RESULT: SearchResultResponse = {
  type: 'JOB',
  id: '11111111-1111-1111-1111-111111111111',
  title: 'Backend Engineer',
  subtitle: 'Acme Corp',
};

// Longer than nav-search's own debounceTime so the switchMap chain settles before assertions.
async function flushDebounce(): Promise<void> {
  await new Promise((resolve) => setTimeout(resolve, 300));
}

describe('NavSearch', () => {
  let fixture: ComponentFixture<NavSearch>;
  let component: NavSearch;
  let router: Router;
  let searchSpy: ReturnType<typeof vi.fn>;

  async function setup(contentSearch = false): Promise<void> {
    searchSpy = vi.fn().mockReturnValue(of([JOB_RESULT]));

    await TestBed.configureTestingModule({
      imports: [NavSearch],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideTranslateService({ lang: 'en', fallbackLang: 'en' }),
        { provide: SearchService, useValue: { search: searchSpy } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(NavSearch);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('pages', PAGES);
    fixture.componentRef.setInput('contentSearch', contentSearch);
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    fixture.detectChanges();
  }

  it('filters pages by their (translated) label', async () => {
    await setup();

    component['control'].setValue('jobs');

    expect(component['pageResults']()).toEqual([PAGES[1]]);
  });

  it('selecting a page label navigates to its path and clears the field', async () => {
    await setup();
    component['control'].setValue('jobs');

    component['navigateByLabel']('nav.jobs');

    expect(router.navigate).toHaveBeenCalledWith(['/jobs']);
    expect(component['control'].value).toBe('');
  });

  it('does not call the search service when contentSearch is disabled', async () => {
    await setup(false);

    component['control'].setValue('backend');
    await flushDebounce();

    expect(searchSpy).not.toHaveBeenCalled();
    expect(component['contentResults']()).toEqual([]);
  });

  it('calls the search service and populates contentResults when contentSearch is enabled', async () => {
    await setup(true);

    component['control'].setValue('backend');
    await flushDebounce();

    expect(searchSpy).toHaveBeenCalledWith('backend');
    expect(component['contentResults']()).toEqual([JOB_RESULT]);
  });

  it('does not search for a query shorter than the minimum length', async () => {
    await setup(true);

    component['control'].setValue('b');
    await flushDebounce();

    expect(searchSpy).not.toHaveBeenCalled();
  });

  it('selecting a content result navigates to its route and clears the field', async () => {
    await setup(true);
    component['control'].setValue('backend');
    await flushDebounce();

    component['onOptionSelected']({ option: { value: JOB_RESULT } } as any);

    expect(router.navigateByUrl).toHaveBeenCalledWith('/jobs/11111111-1111-1111-1111-111111111111');
    expect(component['control'].value).toBe('');
  });

  it('Enter prefers a matching page over a content result', async () => {
    await setup(true);
    component['control'].setValue('jobs');
    await flushDebounce();

    component['onEnter']();

    expect(router.navigate).toHaveBeenCalledWith(['/jobs']);
    expect(router.navigateByUrl).not.toHaveBeenCalled();
  });

  it('Enter falls back to the first content result when no page matches', async () => {
    await setup(true);
    component['control'].setValue('backend');
    await flushDebounce();

    component['onEnter']();

    expect(router.navigateByUrl).toHaveBeenCalledWith('/jobs/11111111-1111-1111-1111-111111111111');
  });
});
