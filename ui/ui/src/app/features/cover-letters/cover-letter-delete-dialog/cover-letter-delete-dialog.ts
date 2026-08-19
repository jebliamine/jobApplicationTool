import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { LucideTriangleAlert } from '@lucide/angular';

export interface CoverLetterDeleteDialogData {
  jobTitle: string;
}

@Component({
  selector: 'app-cover-letter-delete-dialog',
  imports: [MatButtonModule, MatDialogModule, LucideTriangleAlert],
  templateUrl: './cover-letter-delete-dialog.html',
  styleUrl: './cover-letter-delete-dialog.scss',
})
export class CoverLetterDeleteDialog {
  protected readonly data = inject<CoverLetterDeleteDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<CoverLetterDeleteDialog>);

  protected cancel(): void {
    this.dialogRef.close(false);
  }

  protected confirm(): void {
    this.dialogRef.close(true);
  }
}
