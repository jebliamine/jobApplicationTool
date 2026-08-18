import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { LucideLogOut, LucideUser } from '@lucide/angular';
import { AuthService } from '../../core/auth/auth.service';
import { UserService } from '../../core/user/user.service';

@Component({
  selector: 'app-user-menu',
  imports: [RouterLink, MatButtonModule, MatMenuModule, MatTooltipModule, LucideLogOut, LucideUser],
  templateUrl: './user-menu.html',
  styleUrl: './user-menu.scss',
})
export class UserMenu {
  private readonly authService = inject(AuthService);
  private readonly userService = inject(UserService);
  private readonly router = inject(Router);

  protected readonly initial = computed(() => {
    const name = this.userService.currentUser()?.fullName ?? this.authService.currentUserEmail();
    return (name ?? '?').charAt(0).toUpperCase();
  });

  protected logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
