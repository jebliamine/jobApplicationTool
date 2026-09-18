import { Component, computed, input, signal } from '@angular/core';
import { LucideClipboardList, LucideMail, LucideSearch, LucideSparkles } from '@lucide/angular';

import { ScrollStageDirective } from '../../directives/scroll-stage.directive';

export interface WorkflowStage {
  key: 'discover' | 'prepare' | 'apply' | 'track';
  label: string;
  description: string;
}

const DEFAULT_STAGES: readonly WorkflowStage[] = [
  {
    key: 'discover',
    label: 'Discover',
    description: 'Search live listings and save the roles worth pursuing.',
  },
  {
    key: 'prepare',
    label: 'Prepare',
    description: 'Extract skills from your CV and requirements from the posting.',
  },
  {
    key: 'apply',
    label: 'Apply',
    description: 'Generate a cover letter tailored to both, in your voice.',
  },
  {
    key: 'track',
    label: 'Track',
    description: 'Follow every application from applied through to offer.',
  },
];

/**
 * The Discover → Prepare → Apply → Track spine: a vertical, scroll-linked
 * sequence (not a decorative vignette — the labels/descriptions are real
 * content, so this isn't aria-hidden). Highlights whichever stage the
 * reader has scrolled to via `appScrollStage`.
 */
@Component({
  selector: 'app-workflow-spine',
  imports: [ScrollStageDirective, LucideClipboardList, LucideMail, LucideSearch, LucideSparkles],
  templateUrl: './workflow-spine.html',
  styleUrl: './workflow-spine.scss',
  host: { class: 'workflow-spine' },
})
export class WorkflowSpine {
  // Defaults to English copy so the component still works standalone; a
  // consuming page (e.g. the translated `home` page) passes real, translated
  // stage content instead — this component stays translation-agnostic.
  readonly stages = input<readonly WorkflowStage[]>(DEFAULT_STAGES);

  readonly activeIndex = signal(0);
  readonly fillPercent = computed(() =>
    this.stages().length > 1 ? (this.activeIndex() / (this.stages().length - 1)) * 100 : 0,
  );

  onStageActive(index: number, active: boolean): void {
    if (active) {
      this.activeIndex.set(index);
    }
  }
}
