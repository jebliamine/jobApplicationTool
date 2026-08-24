import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { LanguageService } from './core/language/language.service';
import { ThemeService } from './core/theme/theme.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  // Injected (not just imported) so it activates language detection and
  // persistence on every route, including the pre-auth login/register pages.
  private readonly languageService = inject(LanguageService);
  // Same reasoning: without this, `data-theme` is never applied on routes
  // that never construct the (previously topbar-only) ThemeToggle, so
  // dark/system preference silently fails to render on the public landing,
  // login, and register pages.
  private readonly themeService = inject(ThemeService);
}
