import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { LucideTriangleAlert } from '@lucide/angular';

export interface CvDeleteDialogData {
  fileName: string;
}

@Component({
  selector: 'app-cv-delete-dialog',
  imports: [MatButtonModule, MatDialogModule, LucideTriangleAlert],
  templateUrl: './cv-delete-dialog.html',
  styleUrl: './cv-delete-dialog.scss',
})
export class CvDeleteDialog {
  protected readonly data = inject<CvDeleteDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<CvDeleteDialog>);

  protected cancel(): void {
    this.dialogRef.close(false);
  }

  protected confirm(): void {
    this.dialogRef.close(true);
  }
}
