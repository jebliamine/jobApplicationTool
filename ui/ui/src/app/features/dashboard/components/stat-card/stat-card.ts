import { Component, input } from '@angular/core';
import { MatCardModule } from '@angular/material/card';

/** Generic stat display — the icon is provided via content projection so this stays reusable across stats. */
@Component({
  selector: 'app-stat-card',
  imports: [MatCardModule],
  templateUrl: './stat-card.html',
  styleUrl: './stat-card.scss',
})
export class StatCard {
  readonly label = input.required<string>();
  readonly value = input.required<number>();
  readonly hint = input<string | null>(null);
}
