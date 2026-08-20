import { Component, computed, inject, input, output } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatDividerModule } from '@angular/material/divider';
import { MatListModule } from '@angular/material/list';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslatePipe } from '@ngx-translate/core';
import {
  LucideBriefcase,
  LucideBuilding2,
  LucideClipboardList,
  LucideFileText,
  LucideLayoutDashboard,
  LucideMail,
  LucideSettings,
  LucideShield,
} from '@lucide/angular';
import { APP_PAGES, AppPage } from '../../core/navigation/app-pages';
import { UserService } from '../../core/user/user.service';

@Component({
  selector: 'app-sidebar',
  imports: [
    RouterLink,
    RouterLinkActive,
    MatDividerModule,
    MatListModule,
    MatTooltipModule,
    TranslatePipe,
    LucideLayoutDashboard,
    LucideFileText,
    LucideBriefcase,
    LucideBuilding2,
    LucideClipboardList,
    LucideMail,
    LucideSettings,
    LucideShield,
  ],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
})
export class Sidebar {
  private readonly userService = inject(UserService);

  readonly collapsed = input(false);
  readonly linkActivated = output<void>();

  protected readonly isAdmin = computed(() => this.userService.currentUser()?.role === 'ADMIN');

  protected readonly workspacePages: readonly AppPage[] = APP_PAGES.filter(
    (page) => !page.adminOnly && page.path !== '/settings',
  );
  protected readonly adminPages: readonly AppPage[] = APP_PAGES.filter((page) => page.adminOnly);
  protected readonly settingsPage: AppPage = APP_PAGES.find((page) => page.path === '/settings')!;
}
