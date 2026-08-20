import { Component, inject } from '@angular/core';
import { MatButtonToggleChange, MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { LucideMonitor, LucideMoon, LucideSun } from '@lucide/angular';
import { TranslatePipe } from '@ngx-translate/core';
import { LanguageCode, LanguageService } from '../../../core/language/language.service';
import { ThemeMode, ThemeService } from '../../../core/theme/theme.service';

@Component({
  selector: 'app-appearance-section',
  imports: [MatButtonToggleModule, MatCardModule, TranslatePipe, LucideMonitor, LucideMoon, LucideSun],
  templateUrl: './appearance-section.html',
  styleUrl: './appearance-section.scss',
})
export class AppearanceSection {
  protected readonly theme = inject(ThemeService);
  protected readonly language = inject(LanguageService);

  protected onModeChange(change: MatButtonToggleChange): void {
    this.theme.setMode(change.value as ThemeMode);
  }

  protected onLanguageChange(change: MatButtonToggleChange): void {
    this.language.setLanguage(change.value as LanguageCode);
  }
}
