import { Component, input, output } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatDividerModule } from '@angular/material/divider';
import { MatListModule } from '@angular/material/list';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslatePipe } from '@ngx-translate/core';
import { LucideArrowLeftRight, LucideBrainCircuit, LucideLayoutDashboard } from '@lucide/angular';
import { ADMIN_APP_PAGES, AppPage } from '../../core/navigation/app-pages';

/**
 * The admin-only operational sidebar — the existing sidebar-oriented layout,
 * scoped down to admin pages. Normal users never see this; it is only ever
 * rendered inside AdminShell.
 */
@Component({
  selector: 'app-admin-sidebar',
  imports: [
    RouterLink,
    RouterLinkActive,
    MatDividerModule,
    MatListModule,
    MatTooltipModule,
    TranslatePipe,
    LucideArrowLeftRight,
    LucideBrainCircuit,
    LucideLayoutDashboard,
  ],
  templateUrl: './admin-sidebar.html',
  styleUrl: './admin-sidebar.scss',
})
export class AdminSidebar {
  readonly collapsed = input(false);
  readonly linkActivated = output<void>();

  protected readonly adminPages: readonly AppPage[] = ADMIN_APP_PAGES;
}
