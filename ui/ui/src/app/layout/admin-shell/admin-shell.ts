import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterOutlet } from '@angular/router';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { MatSidenavModule } from '@angular/material/sidenav';
import { TranslatePipe } from '@ngx-translate/core';
import { map } from 'rxjs';
import { ADMIN_APP_PAGES } from '../../core/navigation/app-pages';
import { AdminSidebar } from '../admin-sidebar/admin-sidebar';
import { Topbar } from '../topbar/topbar';

/**
 * The admin operational shell — the pre-existing sidebar-based layout,
 * scoped to admin pages only (see AdminSidebar). Regular users never render
 * this; it is only reached via /admin/*, gated by adminGuard. Structurally
 * identical to the pre-redesign single Shell — see UserShell for the new
 * top-nav layout normal users get instead.
 */
@Component({
  selector: 'app-admin-shell',
  imports: [RouterOutlet, MatSidenavModule, TranslatePipe, AdminSidebar, Topbar],
  templateUrl: './admin-shell.html',
  styleUrl: './admin-shell.scss',
})
export class AdminShell {
  private readonly breakpointObserver = inject(BreakpointObserver);

  protected readonly searchPages = ADMIN_APP_PAGES;

  protected readonly isHandset = toSignal(
    this.breakpointObserver
      .observe(Breakpoints.Handset)
      .pipe(map((result) => result.matches)),
    { initialValue: this.breakpointObserver.isMatched(Breakpoints.Handset) },
  );

  private readonly sidenavOpenedOnHandset = signal(false);
  private readonly collapsedOnDesktop = signal(false);

  protected readonly sidenavMode = computed(() => (this.isHandset() ? 'over' : 'side'));
  protected readonly sidenavOpened = computed(() =>
    this.isHandset() ? this.sidenavOpenedOnHandset() : true,
  );
  protected readonly collapsed = computed(() => !this.isHandset() && this.collapsedOnDesktop());

  protected toggleSidenav(): void {
    this.sidenavOpenedOnHandset.update((opened) => !opened);
  }

  protected onOpenedChange(opened: boolean): void {
    if (this.isHandset()) {
      this.sidenavOpenedOnHandset.set(opened);
    }
  }

  protected closeSidenavOnHandset(): void {
    if (this.isHandset()) {
      this.sidenavOpenedOnHandset.set(false);
    }
  }

  protected toggleCollapsed(): void {
    this.collapsedOnDesktop.update((collapsed) => !collapsed);
  }
}
