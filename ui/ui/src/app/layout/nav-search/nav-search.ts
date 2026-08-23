import { Component, ElementRef, computed, effect, inject, input, signal, viewChild } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatAutocompleteModule, MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { LucideSearch, LucideX } from '@lucide/angular';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { AppPage } from '../../core/navigation/app-pages';

/**
 * Navigates between known application pages — not a backend/data search.
 * On compact (mobile) layouts it starts as an icon button and expands into
 * a full-width field that overlays the topbar. The searchable page set is
 * supplied by the host shell (UserShell passes USER_APP_PAGES, AdminShell
 * passes ADMIN_APP_PAGES) so this component stays shell-agnostic.
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

  readonly pages = input.required<readonly AppPage[]>();
  readonly compact = input(false);

  protected readonly expanded = signal(false);
  protected readonly control = new FormControl('', { nonNullable: true });

  private readonly query = toSignal(this.control.valueChanges, { initialValue: '' });
  protected readonly results = computed(() => {
    const term = this.query().trim().toLowerCase();
    const pages = this.pages();
    // page.label is a translation key (see app-pages.ts) — filter against the
    // resolved display text so search works in whichever language is active.
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
  }

  protected expand(): void {
    this.expanded.set(true);
  }

  protected collapse(): void {
    this.expanded.set(false);
    this.control.setValue('');
  }

  protected onOptionSelected(event: MatAutocompleteSelectedEvent): void {
    this.navigateByLabel(event.option.value as string);
  }

  protected onEnter(): void {
    const [first] = this.results();
    if (first) {
      this.navigateByLabel(first.label);
    }
  }

  private navigateByLabel(label: string): void {
    const page = this.pages().find((candidate) => candidate.label === label);
    if (page) {
      this.router.navigate([page.path]);
    }
    this.control.setValue('');
    if (this.compact()) {
      this.expanded.set(false);
    }
  }
}
