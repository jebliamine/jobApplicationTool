import { ApplicationStatus } from './application.models';

export const APPLICATION_STATUS_LABELS: Record<ApplicationStatus, string> = {
  APPLIED: 'Applied',
  PHONE_SCREEN: 'Phone screen',
  INTERVIEWING: 'Interviewing',
  OFFER: 'Offer',
  REJECTED: 'Rejected',
  WITHDRAWN: 'Withdrawn',
  ACCEPTED: 'Accepted',
};

export type ApplicationStatusSeverity = 'info' | 'warning' | 'success' | 'error' | 'neutral';

export const APPLICATION_STATUS_SEVERITY: Record<ApplicationStatus, ApplicationStatusSeverity> = {
  APPLIED: 'info',
  PHONE_SCREEN: 'info',
  INTERVIEWING: 'warning',
  OFFER: 'success',
  ACCEPTED: 'success',
  REJECTED: 'error',
  WITHDRAWN: 'neutral',
};
