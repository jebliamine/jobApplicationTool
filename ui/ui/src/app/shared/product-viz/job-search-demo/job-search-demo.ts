import { Component, DestroyRef, inject, signal } from '@angular/core';
import { LucideBriefcase, LucideCheck, LucideMapPin, LucideSearch } from '@lucide/angular';

import { prefersReducedMotion } from '../util/reduced-motion';

interface DemoResult {
  title: string;
  company: string;
  location: string;
}

/**
 * Discover, as a live moment rather than an icon: a query gets typed, real
 * result rows populate, and one gets saved. Purely decorative (aria-hidden)
 * — the section copy around it carries the actual message.
 */
@Component({
  selector: 'app-job-search-demo',
  imports: [LucideBriefcase, LucideCheck, LucideMapPin, LucideSearch],
  templateUrl: './job-search-demo.html',
  styleUrl: './job-search-demo.scss',
  host: { class: 'job-search-demo', 'aria-hidden': 'true' },
})
export class JobSearchDemo {
  private readonly destroyRef = inject(DestroyRef);
  private timer?: ReturnType<typeof setTimeout>;

  readonly results: DemoResult[] = [
    { title: 'Senior Frontend Engineer', company: 'Nordlicht Software', location: 'Berlin · Remote' },
    { title: 'Frontend Engineer, Platform', company: 'Kessler & Voss', location: 'Hamburg' },
    { title: 'UI Engineer', company: 'Feldmann Systems', location: 'Remote' },
  ];

  readonly query = 'Frontend Engineer, Berlin';
  readonly step = signal(0);
  readonly typedLength = signal(0);

  constructor() {
    if (prefersReducedMotion()) {
      this.step.set(3);
      this.typedLength.set(this.query.length);
      return;
    }
    this.schedule(0, 900);
    this.destroyRef.onDestroy(() => clearTimeout(this.timer));
  }

  private schedule(step: number, delay: number): void {
    this.timer = setTimeout(() => this.enterStep(step), delay);
  }

  private enterStep(step: number): void {
    this.step.set(step);
    switch (step) {
      case 0:
        this.typedLength.set(0);
        this.schedule(1, 700);
        break;
      case 1:
        this.typeQuery();
        break;
      case 2:
        this.schedule(3, 1600);
        break;
      case 3:
        this.schedule(0, 2400);
        break;
    }
  }

  private typeQuery(): void {
    const tick = () => {
      const len = this.typedLength();
      if (len >= this.query.length) {
        this.schedule(2, 500);
        return;
      }
      this.typedLength.set(len + 1);
      this.timer = setTimeout(tick, 32);
    };
    tick();
  }
}
