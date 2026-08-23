import { NgTemplateOutlet } from '@angular/common';
import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';

/**
 * Generic stat display — the icon is provided via content projection so this
 * stays reusable across stats. When `link` is set, the whole card becomes a
 * real router link (not a click handler on a non-interactive element, so it
 * stays keyboard/screen-reader accessible) to that page.
 */
@Component({
  selector: 'app-stat-card',
  imports: [MatCardModule, NgTemplateOutlet, RouterLink],
  templateUrl: './stat-card.html',
  styleUrl: './stat-card.scss',
})
export class StatCard {
  readonly label = input.required<string>();
  readonly value = input.required<number>();
  readonly hint = input<string | null>(null);
  readonly link = input<string | null>(null);
}
