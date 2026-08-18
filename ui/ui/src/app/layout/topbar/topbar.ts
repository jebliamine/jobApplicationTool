import { Component, inject, input, output } from '@angular/core';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { LucideLogOut, LucideMenu, LucidePanelLeftClose, LucidePanelLeftOpen } from '@lucide/angular';
import { AuthService } from '../../core/auth/auth.service';
import { NavSearch } from '../nav-search/nav-search';
import { ThemeToggle } from '../theme-toggle/theme-toggle';

@Component({
  selector: 'app-topbar',
  imports: [
    MatToolbarModule,
    MatButtonModule,
    MatTooltipModule,
    LucideLogOut,
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
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly showMenuButton = input(false);
  readonly sidebarCollapsed = input(false);
  readonly menuToggle = output<void>();
  readonly collapseToggle = output<void>();

  protected readonly userEmail = this.authService.currentUserEmail;

  protected logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
