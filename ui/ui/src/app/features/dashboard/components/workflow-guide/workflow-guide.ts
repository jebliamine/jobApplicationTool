import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import {
  LucideBriefcase,
  LucideBuilding2,
  LucideClipboardList,
  LucideMail,
  LucideTrendingUp,
} from '@lucide/angular';
import { TranslatePipe } from '@ngx-translate/core';

type WorkflowGuideIcon = 'jobs' | 'companies' | 'cover-letters' | 'applications' | 'tracking';

interface WorkflowGuideStep {
  step: number;
  icon: WorkflowGuideIcon;
  titleKey: string;
  descriptionKey: string;
  ctaKey: string;
  link: string;
}

/** Five-step actionable guide shown to a new user with no CVs, jobs, or applications yet. */
@Component({
  selector: 'app-workflow-guide',
  imports: [
    RouterLink,
    MatButtonModule,
    TranslatePipe,
    LucideBriefcase,
    LucideBuilding2,
    LucideClipboardList,
    LucideMail,
    LucideTrendingUp,
  ],
  templateUrl: './workflow-guide.html',
  styleUrl: './workflow-guide.scss',
})
export class WorkflowGuide {
  protected readonly steps: WorkflowGuideStep[] = [
    {
      step: 1,
      icon: 'jobs',
      titleKey: 'dashboard.gettingStarted.steps.job.title',
      descriptionKey: 'dashboard.gettingStarted.steps.job.description',
      ctaKey: 'dashboard.gettingStarted.steps.job.cta',
      link: '/jobs/new',
    },
    {
      step: 2,
      icon: 'companies',
      titleKey: 'dashboard.gettingStarted.steps.company.title',
      descriptionKey: 'dashboard.gettingStarted.steps.company.description',
      ctaKey: 'dashboard.gettingStarted.steps.company.cta',
      link: '/companies/new',
    },
    {
      step: 3,
      icon: 'cover-letters',
      titleKey: 'dashboard.gettingStarted.steps.coverLetter.title',
      descriptionKey: 'dashboard.gettingStarted.steps.coverLetter.description',
      ctaKey: 'dashboard.gettingStarted.steps.coverLetter.cta',
      link: '/cover-letters/generate',
    },
    {
      step: 4,
      icon: 'applications',
      titleKey: 'dashboard.gettingStarted.steps.application.title',
      descriptionKey: 'dashboard.gettingStarted.steps.application.description',
      ctaKey: 'dashboard.gettingStarted.steps.application.cta',
      link: '/applications/new',
    },
    {
      step: 5,
      icon: 'tracking',
      titleKey: 'dashboard.gettingStarted.steps.tracking.title',
      descriptionKey: 'dashboard.gettingStarted.steps.tracking.description',
      ctaKey: 'dashboard.gettingStarted.steps.tracking.cta',
      link: '/applications/board',
    },
  ];
}
