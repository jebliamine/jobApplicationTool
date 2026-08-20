import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { LucideCheck, LucideMonitor, LucideMoon, LucideSun } from '@lucide/angular';
import { TranslatePipe } from '@ngx-translate/core';
import { ThemeMode, ThemeService } from '../../core/theme/theme.service';

@Component({
  selector: 'app-theme-toggle',
  imports: [
    MatButtonModule,
    MatMenuModule,
    MatTooltipModule,
    TranslatePipe,
    LucideSun,
    LucideMoon,
    LucideMonitor,
    LucideCheck,
  ],
  templateUrl: './theme-toggle.html',
  styleUrl: './theme-toggle.scss',
})
export class ThemeToggle {
  protected readonly theme = inject(ThemeService);

  protected setMode(mode: ThemeMode): void {
    this.theme.setMode(mode);
  }
}
