/**
 * Runtime configuration for the Angular app. There is no per-environment
 * backend yet, so this single file covers both `ng serve` and `ng build`;
 * split into environment.development.ts / fileReplacements if a distinct
 * production API URL is introduced later.
 */
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/v1',
};
