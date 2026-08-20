import { Injectable, effect, inject, signal } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

export type LanguageCode = 'en' | 'de';

const STORAGE_KEY = 'japp-language';

/**
 * Tracks the user's chosen UI language, falling back to the browser's
 * language on first run, and reflects it onto TranslateService + the
 * document's `lang` attribute. Same signal + effect + localStorage shape as
 * ThemeService, so the two settings behave identically (no reload either).
 */
@Injectable({ providedIn: 'root' })
export class LanguageService {
  private readonly translate = inject(TranslateService);

  readonly language = signal<LanguageCode>(this.readStoredOrBrowserLanguage());

  constructor() {
    effect(() => {
      const lang = this.language();
      this.translate.use(lang);
      document.documentElement.setAttribute('lang', lang);
    });
  }

  setLanguage(lang: LanguageCode): void {
    this.language.set(lang);
    localStorage.setItem(STORAGE_KEY, lang);
  }

  private readStoredOrBrowserLanguage(): LanguageCode {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored === 'en' || stored === 'de') {
      return stored;
    }
    return this.translate.getBrowserLang() === 'de' ? 'de' : 'en';
  }
}
