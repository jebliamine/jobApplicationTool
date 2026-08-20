import { Component, inject } from '@angular/core';
import { MatButtonToggleChange, MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { LucideMonitor, LucideMoon, LucideSun } from '@lucide/angular';
import { ThemeMode, ThemeService } from '../../../core/theme/theme.service';

@Component({
  selector: 'app-appearance-section',
  imports: [MatButtonToggleModule, MatCardModule, LucideMonitor, LucideMoon, LucideSun],
  templateUrl: './appearance-section.html',
  styleUrl: './appearance-section.scss',
})
export class AppearanceSection {
  protected readonly theme = inject(ThemeService);

  protected onModeChange(change: MatButtonToggleChange): void {
    this.theme.setMode(change.value as ThemeMode);
  }
}
