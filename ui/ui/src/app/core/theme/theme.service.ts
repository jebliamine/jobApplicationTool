import { Injectable, computed, effect, signal } from '@angular/core';

export type ThemeMode = 'light' | 'dark' | 'system';
export type EffectiveTheme = 'light' | 'dark';

const STORAGE_KEY = 'japp-theme';
const DARK_MEDIA_QUERY = '(prefers-color-scheme: dark)';

/**
 * Tracks the user's chosen theme mode (light/dark/system), resolves it
 * against the OS preference when set to "system", and reflects the result
 * onto `<html data-theme="...">` so styles.scss can key off it.
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly media = window.matchMedia(DARK_MEDIA_QUERY);
  private readonly systemPrefersDark = signal(this.media.matches);

  readonly mode = signal<ThemeMode>(this.readStoredMode());

  readonly effectiveTheme = computed<EffectiveTheme>(() => {
    const mode = this.mode();
    return mode === 'system' ? (this.systemPrefersDark() ? 'dark' : 'light') : mode;
  });

  constructor() {
    this.media.addEventListener('change', (event) => {
      this.systemPrefersDark.set(event.matches);
    });

    effect(() => {
      document.documentElement.setAttribute('data-theme', this.effectiveTheme());
    });
  }

  setMode(mode: ThemeMode): void {
    this.mode.set(mode);
    localStorage.setItem(STORAGE_KEY, mode);
  }

  private readStoredMode(): ThemeMode {
    const stored = localStorage.getItem(STORAGE_KEY);
    return stored === 'light' || stored === 'dark' || stored === 'system' ? stored : 'system';
  }
}
