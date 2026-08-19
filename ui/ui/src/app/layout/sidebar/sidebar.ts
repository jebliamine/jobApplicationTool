import { Component, computed, inject, input, output } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatDividerModule } from '@angular/material/divider';
import { MatListModule } from '@angular/material/list';
import { MatTooltipModule } from '@angular/material/tooltip';
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
import { UserService } from '../../core/user/user.service';

@Component({
  selector: 'app-sidebar',
  imports: [
    RouterLink,
    RouterLinkActive,
    MatDividerModule,
    MatListModule,
    MatTooltipModule,
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
}
