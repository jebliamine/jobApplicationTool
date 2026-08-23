import { Component, inject } from '@angular/core';
import { Meta, Title } from '@angular/platform-browser';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import {
  LucideBriefcase,
  LucideBuilding2,
  LucideClipboardList,
  LucideFileText,
  LucideMail,
} from '@lucide/angular';
import { TranslatePipe } from '@ngx-translate/core';
import { HeroIllustration } from './components/hero-illustration/hero-illustration';

interface Pillar {
  readonly icon: 'cv' | 'jobs' | 'companies' | 'applications' | 'cover-letters';
  readonly titleKey: string;
  readonly descriptionKey: string;
}

interface WorkflowStep {
  readonly step: number;
  readonly titleKey: string;
  readonly descriptionKey: string;
}

const PILLARS: readonly Pillar[] = [
  { icon: 'cv', titleKey: 'landing.pillars.cv.title', descriptionKey: 'landing.pillars.cv.description' },
  { icon: 'jobs', titleKey: 'landing.pillars.jobs.title', descriptionKey: 'landing.pillars.jobs.description' },
  {
    icon: 'companies',
    titleKey: 'landing.pillars.companies.title',
    descriptionKey: 'landing.pillars.companies.description',
  },
  {
    icon: 'applications',
    titleKey: 'landing.pillars.applications.title',
    descriptionKey: 'landing.pillars.applications.description',
  },
  {
    icon: 'cover-letters',
    titleKey: 'landing.pillars.coverLetters.title',
    descriptionKey: 'landing.pillars.coverLetters.description',
  },
];

const WORKFLOW_STEPS: readonly WorkflowStep[] = [
  { step: 1, titleKey: 'landing.howItWorks.steps.job.title', descriptionKey: 'landing.howItWorks.steps.job.description' },
  {
    step: 2,
    titleKey: 'landing.howItWorks.steps.company.title',
    descriptionKey: 'landing.howItWorks.steps.company.description',
  },
  {
    step: 3,
    titleKey: 'landing.howItWorks.steps.coverLetter.title',
    descriptionKey: 'landing.howItWorks.steps.coverLetter.description',
  },
  {
    step: 4,
    titleKey: 'landing.howItWorks.steps.application.title',
    descriptionKey: 'landing.howItWorks.steps.application.description',
  },
  {
    step: 5,
    titleKey: 'landing.howItWorks.steps.tracking.title',
    descriptionKey: 'landing.howItWorks.steps.tracking.description',
  },
];

/**
 * The public, unauthenticated marketing page at '/' — publicGuard redirects
 * already-authenticated visitors straight to /dashboard, so this only ever
 * renders for a logged-out visitor. Owns the full product narrative (the
 * authenticated home is workspace-focused instead — see Dashboard).
 */
@Component({
  selector: 'app-public-landing',
  imports: [
    RouterLink,
    MatButtonModule,
    TranslatePipe,
    HeroIllustration,
    LucideBriefcase,
    LucideBuilding2,
    LucideClipboardList,
    LucideFileText,
    LucideMail,
  ],
  templateUrl: './public-landing.html',
  styleUrl: './public-landing.scss',
})
export class PublicLanding {
  private readonly title = inject(Title);
  private readonly meta = inject(Meta);

  protected readonly pillars = PILLARS;
  protected readonly workflowSteps = WORKFLOW_STEPS;
  protected readonly currentYear = new Date().getFullYear();

  constructor() {
    // Client-rendered only (no SSR/prerendering in this app yet) — this helps
    // the browser tab/history and any crawler that does execute JS, but is
    // not sufficient on its own for true crawlability or Open Graph previews.
    this.title.setTitle('JAPP — Organize your job search: CVs, jobs, companies, cover letters, and applications');
    this.meta.updateTag({
      name: 'description',
      content:
        'JAPP is a job application management workspace: manage your CV, save job opportunities and companies, ' +
        'generate tailored cover letters with AI, and track every application from applied to offer.',
    });
  }
}
