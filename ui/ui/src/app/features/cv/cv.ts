import { Component, computed, inject, signal } from '@angular/core';
import { UserService } from '../../core/user/user.service';
import { CvList } from './cv-list/cv-list';
import { CvUpload } from './cv-upload/cv-upload';
import { CvResponse } from './cv.models';
import { CvService } from './cv.service';

type LoadState = 'loading' | 'loaded' | 'error';

@Component({
  selector: 'app-cv',
  imports: [CvList, CvUpload],
  templateUrl: './cv.html',
  styleUrl: './cv.scss',
})
export class Cv {
  private readonly cvService = inject(CvService);
  private readonly userService = inject(UserService);

  private readonly state = signal<LoadState>('loading');
  private readonly _cvs = signal<CvResponse[]>([]);

  protected readonly loading = computed(() => this.state() === 'loading');
  protected readonly error = computed(() => this.state() === 'error');
  protected readonly cvs = this._cvs.asReadonly();
  protected readonly isAdmin = computed(() => this.userService.currentUser()?.role === 'ADMIN');

  constructor() {
    this.load();
  }

  protected load(): void {
    this.state.set('loading');
    this.cvService.list().subscribe({
      next: (cvs) => {
        this._cvs.set(cvs);
        this.state.set('loaded');
      },
      error: () => this.state.set('error'),
    });
  }

  protected onUploaded(cv: CvResponse): void {
    this._cvs.update((current) => [cv, ...current]);
  }

  protected onDeleted(id: string): void {
    this._cvs.update((current) => current.filter((cv) => cv.id !== id));
  }
}
