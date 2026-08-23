import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { LucideLogOut, LucideShield, LucideUser } from '@lucide/angular';
import { TranslatePipe } from '@ngx-translate/core';
import { AuthService } from '../../core/auth/auth.service';
import { UserService } from '../../core/user/user.service';

@Component({
  selector: 'app-user-menu',
  imports: [
    RouterLink,
    MatButtonModule,
    MatMenuModule,
    MatTooltipModule,
    TranslatePipe,
    LucideLogOut,
    LucideShield,
    LucideUser,
  ],
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

  protected readonly isAdmin = computed(() => this.userService.currentUser()?.role === 'ADMIN');

  protected logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
