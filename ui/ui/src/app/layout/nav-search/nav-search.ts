import { Component, ElementRef, computed, effect, inject, input, signal, viewChild } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatAutocompleteModule, MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { LucideSearch, LucideX } from '@lucide/angular';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { catchError, debounceTime, distinctUntilChanged, finalize, of, switchMap } from 'rxjs';
import { AppPage } from '../../core/navigation/app-pages';
import { SearchResultResponse, routeForSearchResult } from '../../core/search/search.models';
import { SearchService } from '../../core/search/search.service';

const CONTENT_SEARCH_DEBOUNCE_MS = 250;
const MIN_CONTENT_QUERY_LENGTH = 2;

/**
 * Navigates between known application pages — not a backend/data search.
 * On compact (mobile) layouts it starts as an icon button and expands into
 * a full-width field that overlays the topbar. The searchable page set is
 * supplied by the host shell (UserShell passes USER_APP_PAGES, AdminShell
 * passes ADMIN_APP_PAGES) so this component stays shell-agnostic.
 *
 * When `contentSearch` is enabled (UserShell only — see its own reasoning),
 * this also queries GET /api/v1/search and merges real jobs/companies/
 * applications/cover-letters into the same dropdown, in a second group below
 * the static pages.
 */
@Component({
  selector: 'app-nav-search',
  imports: [
    ReactiveFormsModule,
    MatAutocompleteModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    TranslatePipe,
    LucideSearch,
    LucideX,
  ],
  templateUrl: './nav-search.html',
  styleUrl: './nav-search.scss',
})
export class NavSearch {
  private readonly router = inject(Router);
  private readonly translate = inject(TranslateService);
  private readonly searchService = inject(SearchService);

  readonly pages = input.required<readonly AppPage[]>();
  readonly compact = input(false);
  readonly contentSearch = input(false);

  protected readonly expanded = signal(false);
  protected readonly control = new FormControl('', { nonNullable: true });
  protected readonly searching = signal(false);
  protected readonly contentResults = signal<SearchResultResponse[]>([]);

  private readonly query = toSignal(this.control.valueChanges, { initialValue: '' });
  protected readonly pageResults = computed(() => {
    const term = this.query().trim().toLowerCase();
    const pages = this.pages();
    return term
      ? pages.filter((page) =>
          (this.translate.instant(page.label) as string).toLowerCase().includes(term),
        )
      : pages;
  });

  private readonly searchInput = viewChild<ElementRef<HTMLInputElement>>('searchInput');

  constructor() {
    effect(() => {
      if (this.expanded()) {
        this.searchInput()?.nativeElement.focus();
      }
    });

    toObservable(this.query)
      .pipe(
        debounceTime(CONTENT_SEARCH_DEBOUNCE_MS),
        distinctUntilChanged(),
        switchMap((term) => {
          const trimmed = term.trim();
          if (!this.contentSearch() || trimmed.length < MIN_CONTENT_QUERY_LENGTH) {
            return of<SearchResultResponse[]>([]);
          }
          this.searching.set(true);
          return this.searchService.search(trimmed).pipe(
            catchError(() => of<SearchResultResponse[]>([])),
            finalize(() => this.searching.set(false)),
          );
        }),
      )
      .subscribe((results) => this.contentResults.set(results));
  }

  protected expand(): void {
    this.expanded.set(true);
  }

  protected collapse(): void {
    this.expanded.set(false);
    this.control.setValue('');
  }

  protected resultTypeLabel(result: SearchResultResponse): string {
    return this.translate.instant(`search.types.${result.type}`);
  }

  protected onOptionSelected(event: MatAutocompleteSelectedEvent): void {
    const value = event.option.value as string | SearchResultResponse;
    if (typeof value === 'string') {
      this.navigateByLabel(value);
    } else {
      this.navigateToResult(value);
    }
  }

  protected onEnter(): void {
    const [firstPage] = this.pageResults();
    if (firstPage) {
      this.navigateByLabel(firstPage.label);
      return;
    }
    const [firstResult] = this.contentResults();
    if (firstResult) {
      this.navigateToResult(firstResult);
    }
  }

  private navigateByLabel(label: string): void {
    const page = this.pages().find((candidate) => candidate.label === label);
    if (page) {
      this.router.navigate([page.path]);
    }
    this.closeSearch();
  }

  private navigateToResult(result: SearchResultResponse): void {
    this.router.navigateByUrl(routeForSearchResult(result));
    this.closeSearch();
  }

  private closeSearch(): void {
    this.control.setValue('');
    if (this.compact()) {
      this.expanded.set(false);
    }
  }
}
