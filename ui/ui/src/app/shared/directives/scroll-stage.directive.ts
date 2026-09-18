import { Directive, ElementRef, OnDestroy, OnInit, inject, output } from '@angular/core';

/**
 * Reports whether the host sits in the thin band around the viewport's
 * vertical center, and toggles `.is-active` on the host to match — used by
 * `workflow-spine` to highlight whichever Discover/Prepare/Apply/Track stage
 * the user has scrolled to. Unlike `appRevealOnScroll` this isn't an
 * accessibility-gated entrance effect (there's no `prefers-reduced-motion`
 * bypass here): it's a state readout, not a decorative animation — under
 * reduced motion the host still needs to know which stage is current, it
 * just shouldn't animate the *transition* between states (handled in CSS).
 */
@Directive({
  selector: '[appScrollStage]',
  host: { '[class.is-active]': 'active' },
})
export class ScrollStageDirective implements OnInit, OnDestroy {
  private readonly element = inject(ElementRef<HTMLElement>);
  private observer?: IntersectionObserver;

  active = false;
  readonly activeChange = output<boolean>();

  ngOnInit(): void {
    if (!('IntersectionObserver' in window)) {
      this.setActive(true);
      return;
    }
    this.observer = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          this.setActive(entry.isIntersecting);
        }
      },
      { threshold: 0, rootMargin: '-45% 0px -45% 0px' },
    );
    this.observer.observe(this.element.nativeElement);
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
  }

  private setActive(value: boolean): void {
    if (this.active === value) {
      return;
    }
    this.active = value;
    this.activeChange.emit(value);
  }
}
