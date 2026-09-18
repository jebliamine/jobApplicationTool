import { Component, ElementRef, computed, effect, inject, signal, viewChild } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { Meta, Title } from '@angular/platform-browser';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { LucideLock, LucideServer, LucideShieldCheck, LucideSparkles } from '@lucide/angular';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';

import { RevealOnScrollDirective } from '../../shared/directives/reveal-on-scroll.directive';
import { ScrollStageDirective } from '../../shared/directives/scroll-stage.directive';
import { ApplicationLifecycleDemo } from '../../shared/product-viz/application-lifecycle-demo/application-lifecycle-demo';
import { CoverLetterGenerationDemo } from '../../shared/product-viz/cover-letter-generation-demo/cover-letter-generation-demo';
import { CvSkillExtractionDemo } from '../../shared/product-viz/cv-skill-extraction-demo/cv-skill-extraction-demo';
import { JobAnalysisDemo } from '../../shared/product-viz/job-analysis-demo/job-analysis-demo';
import { JobSearchDemo } from '../../shared/product-viz/job-search-demo/job-search-demo';
import { WorkflowSpine, type WorkflowStage } from '../../shared/product-viz/workflow-spine/workflow-spine';
import { ThemeToggle } from '../../layout/theme-toggle/theme-toggle';

interface ValueProp {
  readonly key: 'discover' | 'prepare' | 'apply' | 'track';
  readonly titleKey: string;
  readonly descriptionKey: string;
}

interface TrustItem {
  readonly icon: 'auth' | 'encryption' | 'stack' | 'providers';
  readonly labelKey: string;
}

const VALUE_PROPS: readonly ValueProp[] = [
  { key: 'discover', titleKey: 'home.valueProps.discover.title', descriptionKey: 'home.valueProps.discover.description' },
  { key: 'prepare', titleKey: 'home.valueProps.prepare.title', descriptionKey: 'home.valueProps.prepare.description' },
  { key: 'apply', titleKey: 'home.valueProps.apply.title', descriptionKey: 'home.valueProps.apply.description' },
  { key: 'track', titleKey: 'home.valueProps.track.title', descriptionKey: 'home.valueProps.track.description' },
];

const TRUST_ITEMS: readonly TrustItem[] = [
  { icon: 'auth', labelKey: 'home.trust.items.auth' },
  { icon: 'encryption', labelKey: 'home.trust.items.encryption' },
  { icon: 'stack', labelKey: 'home.trust.items.stack' },
  { icon: 'providers', labelKey: 'home.trust.items.providers' },
];

/**
 * The merged home page: the product narrative for a logged-out visitor.
 * Parallel to `PublicLanding` during the redesign migration — not yet
 * wired to `''` or made auth-aware (that's a later phase); this page is
 * for side-by-side review before cutover.
 */
@Component({
  selector: 'app-home',
  imports: [
    RouterLink,
    MatButtonModule,
    TranslatePipe,
    ThemeToggle,
    RevealOnScrollDirective,
    ScrollStageDirective,
    JobSearchDemo,
    CvSkillExtractionDemo,
    JobAnalysisDemo,
    CoverLetterGenerationDemo,
    ApplicationLifecycleDemo,
    WorkflowSpine,
    LucideLock,
    LucideServer,
    LucideShieldCheck,
    LucideSparkles,
  ],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home {
  private readonly title = inject(Title);
  private readonly meta = inject(Meta);
  private readonly translate = inject(TranslateService);

  protected readonly valueProps = VALUE_PROPS;
  protected readonly trustItems = TRUST_ITEMS;
  protected readonly currentYear = new Date().getFullYear();

  // Drives the floating nav's scroll-spy highlight — which of the four
  // feature sections (via appScrollStage on each <section>) is currently
  // centered in the viewport.
  protected readonly activeSection = signal<'discover' | 'prepare' | 'apply' | 'track' | null>(null);

  protected onSectionActive(key: 'discover' | 'prepare' | 'apply' | 'track', active: boolean): void {
    if (active) {
      this.activeSection.set(key);
    } else if (this.activeSection() === key) {
      this.activeSection.set(null);
    }
  }

  // Drives the header's morph from a flush, full-width bar (at the very top,
  // merged with the hero) into the floating rounded glass pill once the user
  // starts scrolling. IntersectionObserver on a 1px sentinel just below the
  // header — not a scroll listener, per this app's own animation guardrail
  // against window.addEventListener('scroll').
  private readonly scrollSentinel = viewChild<ElementRef<HTMLElement>>('scrollSentinel');
  protected readonly scrolled = signal(false);

  // workflow-spine takes resolved strings, not translation keys, so its
  // input is rebuilt whenever the active language changes.
  private readonly langChange = toSignal(this.translate.onLangChange, { initialValue: null });
  protected readonly workflowStages = computed<readonly WorkflowStage[]>(() => {
    this.langChange();
    return [
      {
        key: 'discover',
        label: this.translate.instant('home.workflow.stages.discover.label'),
        description: this.translate.instant('home.workflow.stages.discover.description'),
      },
      {
        key: 'prepare',
        label: this.translate.instant('home.workflow.stages.prepare.label'),
        description: this.translate.instant('home.workflow.stages.prepare.description'),
      },
      {
        key: 'apply',
        label: this.translate.instant('home.workflow.stages.apply.label'),
        description: this.translate.instant('home.workflow.stages.apply.description'),
      },
      {
        key: 'track',
        label: this.translate.instant('home.workflow.stages.track.label'),
        description: this.translate.instant('home.workflow.stages.track.description'),
      },
    ];
  });

  constructor() {
    effect((onCleanup) => {
      const el = this.scrollSentinel()?.nativeElement;
      if (!el || !('IntersectionObserver' in window)) {
        return;
      }
      const observer = new IntersectionObserver(([entry]) => this.scrolled.set(!entry.isIntersecting), {
        threshold: 0,
      });
      observer.observe(el);
      onCleanup(() => observer.disconnect());
    });

    // Same client-rendered-only caveat as PublicLanding — see its constructor
    // comment. Kept in sync manually until this page replaces that one.
    const title = 'JAPP — Organize your job search: CVs, jobs, companies, cover letters, and applications';
    const description =
      'JAPP is a job application management workspace: manage your CV, save job opportunities and companies, ' +
      'generate tailored cover letters with AI, and track every application from applied to offer.';

    this.title.setTitle(title);
    this.meta.updateTag({ name: 'description', content: description });
    this.meta.updateTag({ property: 'og:type', content: 'website' });
    this.meta.updateTag({ property: 'og:site_name', content: 'JAPP' });
    this.meta.updateTag({ property: 'og:title', content: 'JAPP — Organize your job search' });
    this.meta.updateTag({ property: 'og:description', content: description });
    this.meta.updateTag({ name: 'twitter:card', content: 'summary' });
    this.meta.updateTag({ name: 'twitter:title', content: 'JAPP — Organize your job search' });
    this.meta.updateTag({ name: 'twitter:description', content: description });
  }
}
