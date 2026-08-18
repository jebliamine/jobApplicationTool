import { HttpErrorResponse } from '@angular/common/http';
import { Component, ElementRef, inject, output, signal, viewChild } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { LucideCircleAlert, LucideUpload } from '@lucide/angular';
import { finalize } from 'rxjs';
import { describeCvError } from '../cv-error';
import { CvResponse } from '../cv.models';
import { CvService } from '../cv.service';

const ALLOWED_EXTENSIONS = ['pdf', 'doc', 'docx'];
const MAX_SIZE_BYTES = 10 * 1024 * 1024;

interface UploadForm {
  title: FormControl<string>;
}

@Component({
  selector: 'app-cv-upload',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    LucideCircleAlert,
    LucideUpload,
  ],
  templateUrl: './cv-upload.html',
  styleUrl: './cv-upload.scss',
})
export class CvUpload {
  private readonly cvService = inject(CvService);
  private readonly snackBar = inject(MatSnackBar);

  readonly uploaded = output<CvResponse>();

  private readonly fileInput = viewChild<ElementRef<HTMLInputElement>>('fileInput');

  protected readonly form = new FormGroup<UploadForm>({
    title: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  protected readonly selectedFile = signal<File | null>(null);
  protected readonly fileError = signal<string | null>(null);
  protected readonly submitting = signal(false);
  protected readonly serverError = signal<string | null>(null);

  protected onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.fileError.set(null);

    if (file) {
      const validationError = this.validateFile(file);
      if (validationError) {
        this.fileError.set(validationError);
        this.selectedFile.set(null);
        input.value = '';
        return;
      }
    }

    this.selectedFile.set(file);
  }

  protected reset(): void {
    this.form.reset();
    this.selectedFile.set(null);
    this.fileError.set(null);
    this.serverError.set(null);
    const input = this.fileInput();
    if (input) {
      input.nativeElement.value = '';
    }
  }

  protected submit(): void {
    const file = this.selectedFile();

    if (this.form.invalid || !file || this.submitting()) {
      this.form.markAllAsTouched();
      if (!file) {
        this.fileError.set('Select a CV file to upload.');
      }
      return;
    }

    this.submitting.set(true);
    this.serverError.set(null);

    this.cvService
      .upload(file, this.form.getRawValue().title)
      .pipe(finalize(() => this.submitting.set(false)))
      .subscribe({
        next: (cv) => {
          this.uploaded.emit(cv);
          this.reset();
          this.snackBar.open('CV uploaded.', 'Dismiss', { duration: 4000 });
        },
        error: (error: HttpErrorResponse) => this.serverError.set(describeCvError(error)),
      });
  }

  private validateFile(file: File): string | null {
    if (file.size === 0) {
      return 'The selected file is empty.';
    }
    if (file.size > MAX_SIZE_BYTES) {
      return 'File exceeds the 10 MB limit.';
    }
    const extension = file.name.split('.').pop()?.toLowerCase() ?? '';
    if (!ALLOWED_EXTENSIONS.includes(extension)) {
      return 'Unsupported file type. Allowed formats: PDF, DOC, DOCX.';
    }
    return null;
  }
}
