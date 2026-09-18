import { Component, DestroyRef, inject } from '@angular/core';
import { LucideMail, LucideSparkles } from '@lucide/angular';

import { createStepCycle } from '../util/step-cycle';

/** Apply, as a live moment: CV skills and job requirements feed a letter that writes itself in. */
@Component({
  selector: 'app-cover-letter-generation-demo',
  imports: [LucideMail, LucideSparkles],
  templateUrl: './cover-letter-generation-demo.html',
  styleUrl: './cover-letter-generation-demo.scss',
  host: { class: 'cover-letter-gen-demo', 'aria-hidden': 'true' },
})
export class CoverLetterGenerationDemo {
  readonly inputChips = ['Angular', 'TypeScript', '5+ years'];
  readonly letterLines = [92, 100, 78, 100, 55];
  readonly step = createStepCycle(inject(DestroyRef), [1300, 1000, 3000]);
}
