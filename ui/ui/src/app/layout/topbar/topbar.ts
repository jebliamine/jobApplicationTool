import { Component, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { LucideMenu, LucidePanelLeftClose, LucidePanelLeftOpen } from '@lucide/angular';
import { NavSearch } from '../nav-search/nav-search';
import { ThemeToggle } from '../theme-toggle/theme-toggle';

@Component({
  selector: 'app-topbar',
  imports: [
    MatToolbarModule,
    MatButtonModule,
    MatTooltipModule,
    LucideMenu,
    LucidePanelLeftClose,
    LucidePanelLeftOpen,
    NavSearch,
    ThemeToggle,
  ],
  templateUrl: './topbar.html',
  styleUrl: './topbar.scss',
})
export class Topbar {
  readonly showMenuButton = input(false);
  readonly sidebarCollapsed = input(false);
  readonly menuToggle = output<void>();
  readonly collapseToggle = output<void>();
}
