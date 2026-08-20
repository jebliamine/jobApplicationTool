import { Component } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import { AccountSection } from './account-section/account-section';
import { AppearanceSection } from './appearance-section/appearance-section';
import { AiSection } from './ai-section/ai-section';
import { SecuritySection } from './security-section/security-section';

@Component({
  selector: 'app-settings',
  imports: [TranslatePipe, AccountSection, AppearanceSection, AiSection, SecuritySection],
  templateUrl: './settings.html',
  styleUrl: './settings.scss',
})
export class Settings {}
