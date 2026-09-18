/** Single source of truth for the `prefers-reduced-motion` check every product-viz vignette uses. */
export function prefersReducedMotion(): boolean {
  return typeof window !== 'undefined' && 'matchMedia' in window
    ? window.matchMedia('(prefers-reduced-motion: reduce)').matches
    : false;
}
