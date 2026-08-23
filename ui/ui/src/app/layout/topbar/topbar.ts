import { Component, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { LucideMenu, LucidePanelLeftClose, LucidePanelLeftOpen } from '@lucide/angular';
import { TranslatePipe } from '@ngx-translate/core';
import { AppPage } from '../../core/navigation/app-pages';
import { NavSearch } from '../nav-search/nav-search';
import { NotificationBell } from '../notification-bell/notification-bell';
import { ThemeToggle } from '../theme-toggle/theme-toggle';
import { UserMenu } from '../user-menu/user-menu';

@Component({
  selector: 'app-topbar',
  imports: [
    MatToolbarModule,
    MatButtonModule,
    MatTooltipModule,
    TranslatePipe,
    LucideMenu,
    LucidePanelLeftClose,
    LucidePanelLeftOpen,
    NavSearch,
    NotificationBell,
    ThemeToggle,
    UserMenu,
  ],
  templateUrl: './topbar.html',
  styleUrl: './topbar.scss',
})
export class Topbar {
  readonly showMenuButton = input(false);
  readonly sidebarCollapsed = input(false);
  readonly searchPages = input.required<readonly AppPage[]>();
  readonly menuToggle = output<void>();
  readonly collapseToggle = output<void>();
}
