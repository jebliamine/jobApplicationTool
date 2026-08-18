import { Component, input } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { LucideConstruction } from '@lucide/angular';

/**
 * Generic "not implemented yet" page for sidebar/search destinations that
 * have no backend behind them yet. Route `data` (title/description) is
 * bound to these inputs via the router's component-input binding.
 */
@Component({
  selector: 'app-placeholder-page',
  imports: [MatCardModule, LucideConstruction],
  templateUrl: './placeholder-page.html',
  styleUrl: './placeholder-page.scss',
})
export class PlaceholderPage {
  readonly title = input.required<string>();
  readonly description = input<string>('This feature is not implemented yet.');
}
