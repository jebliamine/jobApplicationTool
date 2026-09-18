import { Component, DestroyRef, inject } from '@angular/core';
import { LucideCheck, LucideMessageCircle, LucideSend, LucideUsers } from '@lucide/angular';

import { createStepCycle } from '../util/step-cycle';

interface LifecycleStage {
  label: string;
}

/** Track, as a live moment: one application card moving through its real lifecycle stages. */
@Component({
  selector: 'app-application-lifecycle-demo',
  imports: [LucideCheck, LucideMessageCircle, LucideSend, LucideUsers],
  templateUrl: './application-lifecycle-demo.html',
  styleUrl: './application-lifecycle-demo.scss',
  host: { class: 'app-lifecycle-demo', 'aria-hidden': 'true' },
})
export class ApplicationLifecycleDemo {
  readonly stages: LifecycleStage[] = [
    { label: 'Applied' },
    { label: 'Phone Screen' },
    { label: 'Interviewing' },
    { label: 'Offer' },
  ];

  readonly step = createStepCycle(inject(DestroyRef), [1400, 1400, 1400, 2200]);
}
