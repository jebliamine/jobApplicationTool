import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Meta, Title } from '@angular/platform-browser';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import {
  LucideBrainCircuit,
  LucideBriefcase,
  LucideBuilding2,
  LucideCircleDashed,
  LucideClipboardList,
  LucideFileText,
  LucideLock,
  LucideMail,
  LucidePlug,
  LucideServer,
  LucideShieldCheck,
  LucideSparkles,
} from '@lucide/angular';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { ThemeToggle } from '../../layout/theme-toggle/theme-toggle';
import { HeroIllustration } from './components/hero-illustration/hero-illustration';
import { RevealOnScrollDirective } from './directives/reveal-on-scroll.directive';

// No backend contact endpoint exists yet — the form builds a mailto: link
// client-side and hands off to the visitor's own email client rather than
// inventing a POST /api/v1/contact endpoint that isn't part of the API contract.
const BETA_CONTACT_EMAIL = 'yukiszuki9@gmail.com';

interface ContactForm {
  name: FormControl<string>;
  email: FormControl<string>;
  message: FormControl<string>;
}

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

interface FaqItem {
  readonly questionKey: string;
  readonly answerKey: string;
}

interface TrustItem {
  readonly icon: 'auth' | 'encryption' | 'stack';
  readonly labelKey: string;
}

interface AiProvider {
  readonly icon: 'gemini' | 'openai-compatible' | 'anthropic' | 'placeholder';
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

const TRUST_ITEMS: readonly TrustItem[] = [
  { icon: 'auth', labelKey: 'landing.trust.items.auth' },
  { icon: 'encryption', labelKey: 'landing.trust.items.encryption' },
  { icon: 'stack', labelKey: 'landing.trust.items.stack' },
];

const AI_PROVIDERS: readonly AiProvider[] = [
  {
    icon: 'gemini',
    titleKey: 'landing.aiProviders.items.gemini.title',
    descriptionKey: 'landing.aiProviders.items.gemini.description',
  },
  {
    icon: 'openai-compatible',
    titleKey: 'landing.aiProviders.items.openaiCompatible.title',
    descriptionKey: 'landing.aiProviders.items.openaiCompatible.description',
  },
  {
    icon: 'anthropic',
    titleKey: 'landing.aiProviders.items.anthropic.title',
    descriptionKey: 'landing.aiProviders.items.anthropic.description',
  },
  {
    icon: 'placeholder',
    titleKey: 'landing.aiProviders.items.placeholder.title',
    descriptionKey: 'landing.aiProviders.items.placeholder.description',
  },
];

// Mirrored (in English) in the FAQPage JSON-LD in index.html — keep the two
// in sync if these change, since that copy is static and won't pick up
// translation edits automatically.
const FAQ_ITEMS: readonly FaqItem[] = [
  { questionKey: 'landing.faq.items.whatIsJapp.question', answerKey: 'landing.faq.items.whatIsJapp.answer' },
  { questionKey: 'landing.faq.items.pricing.question', answerKey: 'landing.faq.items.pricing.answer' },
  { questionKey: 'landing.faq.items.stack.question', answerKey: 'landing.faq.items.stack.answer' },
  { questionKey: 'landing.faq.items.coverLetters.question', answerKey: 'landing.faq.items.coverLetters.answer' },
  { questionKey: 'landing.faq.items.privacy.question', answerKey: 'landing.faq.items.privacy.answer' },
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
    ReactiveFormsModule,
    MatButtonModule,
    MatExpansionModule,
    MatFormFieldModule,
    MatInputModule,
    TranslatePipe,
    ThemeToggle,
    HeroIllustration,
    RevealOnScrollDirective,
    LucideBrainCircuit,
    LucideBriefcase,
    LucideBuilding2,
    LucideCircleDashed,
    LucideClipboardList,
    LucideFileText,
    LucideLock,
    LucideMail,
    LucidePlug,
    LucideServer,
    LucideShieldCheck,
    LucideSparkles,
  ],
  templateUrl: './public-landing.html',
  styleUrl: './public-landing.scss',
})
export class PublicLanding {
  private readonly title = inject(Title);
  private readonly meta = inject(Meta);
  private readonly translate = inject(TranslateService);

  protected readonly pillars = PILLARS;
  protected readonly trustItems = TRUST_ITEMS;
  protected readonly aiProviders = AI_PROVIDERS;
  protected readonly workflowSteps = WORKFLOW_STEPS;
  protected readonly faqItems = FAQ_ITEMS;
  protected readonly currentYear = new Date().getFullYear();

  protected readonly contactForm = new FormGroup<ContactForm>({
    name: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
    message: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });
  protected readonly contactSent = signal(false);

  constructor() {
    // Client-rendered only (no SSR/prerendering in this app yet) — this helps
    // the browser tab/history and any crawler that does execute JS, but is
    // not sufficient on its own for true crawlability or Open Graph previews.
    // The real fallback for non-JS crawlers is the static <head>/<noscript>
    // content in index.html, which these calls otherwise duplicate.
    const title = 'JAPP — Organize your job search: CVs, jobs, companies, cover letters, and applications';
    const description =
      'JAPP is a job application management workspace: manage your CV, save job opportunities and companies, ' +
      'generate tailored cover letters with AI, and track every application from applied to offer.';

    this.title.setTitle(title);
    this.meta.updateTag({ name: 'description', content: description });
    this.meta.updateTag({ name: 'keywords', content: 'job application tracker, job search organizer, CV manager, cover letter generator, AI cover letter' });
    this.meta.updateTag({ property: 'og:type', content: 'website' });
    this.meta.updateTag({ property: 'og:site_name', content: 'JAPP' });
    this.meta.updateTag({ property: 'og:title', content: 'JAPP — Organize your job search' });
    this.meta.updateTag({ property: 'og:description', content: description });
    this.meta.updateTag({ name: 'twitter:card', content: 'summary' });
    this.meta.updateTag({ name: 'twitter:title', content: 'JAPP — Organize your job search' });
    this.meta.updateTag({ name: 'twitter:description', content: description });
  }

  /**
   * Opens the visitor's own email client with a pre-filled beta-test request
   * (see the BETA_CONTACT_EMAIL comment above for why this isn't a backend call).
   */
  protected submitContactForm(): void {
    if (this.contactForm.invalid) {
      this.contactForm.markAllAsTouched();
      return;
    }

    const { name, email, message } = this.contactForm.getRawValue();
    const subject = this.translate.instant('landing.contact.form.mailSubject');
    const body = this.translate.instant('landing.contact.form.mailBody', { name, email, message });
    const mailtoUrl = `mailto:${BETA_CONTACT_EMAIL}?subject=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}`;

    window.location.href = mailtoUrl;
    this.contactSent.set(true);
    this.contactForm.reset();
  }
}
