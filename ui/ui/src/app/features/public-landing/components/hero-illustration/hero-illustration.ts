import { Component } from '@angular/core';
import { LucideBriefcase, LucideCheck, LucideFileText, LucideSparkles } from '@lucide/angular';

/**
 * A hand-authored, entirely local CSS/SVG composition of a CV document, a
 * cover-letter document, and an application-progress pill — no external
 * image assets, so there's no licensing question to resolve. Purely
 * decorative (aria-hidden) — the hero's actual message lives in its text.
 */
@Component({
  selector: 'app-hero-illustration',
  imports: [LucideBriefcase, LucideCheck, LucideFileText, LucideSparkles],
  templateUrl: './hero-illustration.html',
  styleUrl: './hero-illustration.scss',
})
export class HeroIllustration {}
