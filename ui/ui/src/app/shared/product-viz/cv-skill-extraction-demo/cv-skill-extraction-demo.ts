import { Component, DestroyRef, inject } from '@angular/core';
import { LucideFileText, LucideSparkles } from '@lucide/angular';

import { createStepCycle } from '../util/step-cycle';

/** Prepare, as a live moment: a CV gets scanned and skill chips fall out of it. */
@Component({
  selector: 'app-cv-skill-extraction-demo',
  imports: [LucideFileText, LucideSparkles],
  templateUrl: './cv-skill-extraction-demo.html',
  styleUrl: './cv-skill-extraction-demo.scss',
  host: { class: 'cv-extract-demo', 'aria-hidden': 'true' },
})
export class CvSkillExtractionDemo {
  readonly skills = ['TypeScript', 'Angular', 'REST APIs', 'Team leadership', 'CI/CD'];
  readonly step = createStepCycle(inject(DestroyRef), [1400, 1100, 2800]);
}
