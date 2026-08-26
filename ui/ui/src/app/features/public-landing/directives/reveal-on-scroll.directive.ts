import { Directive, ElementRef, OnDestroy, OnInit, inject, input } from '@angular/core';

/**
 * Toggles `.is-visible` on the host once it scrolls into view, so CSS alone
 * can drive the fade/slide-in transition. Skips the observer entirely under
 * `prefers-reduced-motion` — the host starts visible via the .landing-reveal
 * base styles' no-JS fallback in that case.
 */
@Directive({
  selector: '[appRevealOnScroll]',
  host: {
    class: 'landing-reveal',
    '[style.transition-delay.ms]': 'delayMs()',
  },
})
export class RevealOnScrollDirective implements OnInit, OnDestroy {
  private readonly element = inject(ElementRef<HTMLElement>);
  private observer?: IntersectionObserver;
  private fallbackTimer?: ReturnType<typeof setTimeout>;

  readonly delayMs = input(0, { alias: 'appRevealOnScroll', transform: numberAttribute });

  ngOnInit(): void {
    const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    if (prefersReducedMotion || !('IntersectionObserver' in window)) {
      this.reveal();
      return;
    }

    this.observer = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          if (entry.isIntersecting) {
            this.reveal();
          }
        }
      },
      { threshold: 0.15, rootMargin: '0px 0px -40px 0px' },
    );
    this.observer.observe(this.element.nativeElement);

    // Safety net: an instant scroll (anchor link, browser scroll restoration)
    // can in rare cases land content in view before the observer's first
    // callback runs — never let it stay invisible past this point.
    this.fallbackTimer = setTimeout(() => this.reveal(), 1000);
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
    clearTimeout(this.fallbackTimer);
  }

  private reveal(): void {
    this.element.nativeElement.classList.add('is-visible');
    this.observer?.unobserve(this.element.nativeElement);
    clearTimeout(this.fallbackTimer);
  }
}

function numberAttribute(value: unknown): number {
  const parsed = typeof value === 'string' ? Number(value) : value;
  return typeof parsed === 'number' && !Number.isNaN(parsed) ? parsed : 0;
}
