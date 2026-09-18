import { Component, DestroyRef, inject } from '@angular/core';
import { LucideClipboardList, LucideFileText } from '@lucide/angular';

import { createStepCycle } from '../util/step-cycle';

/** Prepare, the other half: a pasted job description turns into structured requirement tags. */
@Component({
  selector: 'app-job-analysis-demo',
  imports: [LucideClipboardList, LucideFileText],
  templateUrl: './job-analysis-demo.html',
  styleUrl: './job-analysis-demo.scss',
  host: { class: 'job-analysis-demo', 'aria-hidden': 'true' },
})
export class JobAnalysisDemo {
  readonly requirements = ['5+ years experience', 'TypeScript', 'Remote-friendly', 'B2 English'];
  readonly step = createStepCycle(inject(DestroyRef), [1400, 1300, 2600]);
}
