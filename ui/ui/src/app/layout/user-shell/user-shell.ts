import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { UserNav } from '../user-nav/user-nav';

/**
 * The normal-user product shell — a top navigation bar instead of a
 * sidebar, per the redesign. Every non-admin authenticated route renders
 * inside this shell, including for admin users managing their own personal
 * job-search data (see AdminShell for the separate operational layout).
 */
@Component({
  selector: 'app-user-shell',
  imports: [RouterOutlet, TranslatePipe, UserNav],
  templateUrl: './user-shell.html',
  styleUrl: './user-shell.scss',
})
export class UserShell {}
