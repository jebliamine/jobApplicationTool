import { DestroyRef, signal, type Signal } from '@angular/core';

import { prefersReducedMotion } from './reduced-motion';

/**
 * Drives a vignette's discrete-state animation: `durationsMs[i]` is how long
 * step `i` stays on screen before advancing (looping back to 0 after the
 * last step). Under `prefers-reduced-motion` it jumps straight to the final,
 * most-informative step and never schedules a timer.
 */
export function createStepCycle(destroyRef: DestroyRef, durationsMs: number[]): Signal<number> {
  const step = signal(0);
  if (prefersReducedMotion() || durationsMs.length === 0) {
    step.set(Math.max(durationsMs.length - 1, 0));
    return step;
  }

  let index = 0;
  let timer: ReturnType<typeof setTimeout>;
  const tick = () => {
    index = (index + 1) % durationsMs.length;
    step.set(index);
    timer = setTimeout(tick, durationsMs[index]);
  };
  timer = setTimeout(tick, durationsMs[0]);
  destroyRef.onDestroy(() => clearTimeout(timer));
  return step;
}
