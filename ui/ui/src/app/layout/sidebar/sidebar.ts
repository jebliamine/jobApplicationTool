import { Component, input, output } from '@angular/core';
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
} from '@lucide/angular';

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
  ],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
})
export class Sidebar {
  readonly collapsed = input(false);
  readonly linkActivated = output<void>();
}
