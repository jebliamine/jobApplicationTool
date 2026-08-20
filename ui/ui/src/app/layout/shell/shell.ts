import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterOutlet } from '@angular/router';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { MatSidenavModule } from '@angular/material/sidenav';
import { TranslatePipe } from '@ngx-translate/core';
import { map } from 'rxjs';
import { Sidebar } from '../sidebar/sidebar';
import { Topbar } from '../topbar/topbar';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, MatSidenavModule, TranslatePipe, Sidebar, Topbar],
  templateUrl: './shell.html',
  styleUrl: './shell.scss',
})
export class Shell {
  private readonly breakpointObserver = inject(BreakpointObserver);

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
