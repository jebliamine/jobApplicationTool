import { Component, HostListener, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import {
  LucideBriefcase,
  LucideBuilding2,
  LucideClipboardList,
  LucideFileText,
  LucideLayoutDashboard,
  LucideMail,
  LucideMenu,
  LucideX,
} from '@lucide/angular';
import { TranslatePipe } from '@ngx-translate/core';
import { USER_APP_PAGES, AppPage } from '../../core/navigation/app-pages';
import { NavSearch } from '../nav-search/nav-search';
import { NotificationBell } from '../notification-bell/notification-bell';
import { ThemeToggle } from '../theme-toggle/theme-toggle';
import { UserMenu } from '../user-menu/user-menu';

/**
 * The top navigation for the normal-user product shell — replaces the
 * sidebar entirely for non-admin routes (see UserShell). Primary links are
 * inline in the bar on desktop/tablet; on narrow viewports they collapse
 * behind a toggle into a full-width dropdown panel rather than a sidebar
 * drawer, per the redesign's explicit "no sidebar for normal users"
 * requirement.
 */
@Component({
  selector: 'app-user-nav',
  imports: [
    RouterLink,
    RouterLinkActive,
    MatButtonModule,
    TranslatePipe,
    NavSearch,
    NotificationBell,
    ThemeToggle,
    UserMenu,
    LucideBriefcase,
    LucideBuilding2,
    LucideClipboardList,
    LucideFileText,
    LucideLayoutDashboard,
    LucideMail,
    LucideMenu,
    LucideX,
  ],
  templateUrl: './user-nav.html',
  styleUrl: './user-nav.scss',
})
export class UserNav {
  protected readonly pages: readonly AppPage[] = USER_APP_PAGES;
  protected readonly mobileMenuOpen = signal(false);

  protected toggleMobileMenu(): void {
    this.mobileMenuOpen.update((open) => !open);
  }

  protected closeMobileMenu(): void {
    this.mobileMenuOpen.set(false);
  }

  @HostListener('window:resize')
  protected onResize(): void {
    // A resize past the breakpoint hides the toggle button via CSS, but the
    // panel itself has no CSS-only way to know it should close with it.
    if (window.innerWidth >= 900) {
      this.mobileMenuOpen.set(false);
    }
  }
}
