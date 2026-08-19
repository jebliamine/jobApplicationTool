import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { LucideTriangleAlert } from '@lucide/angular';

export interface ApplicationDeleteDialogData {
  jobTitle: string;
}

@Component({
  selector: 'app-application-delete-dialog',
  imports: [MatButtonModule, MatDialogModule, LucideTriangleAlert],
  templateUrl: './application-delete-dialog.html',
  styleUrl: './application-delete-dialog.scss',
})
export class ApplicationDeleteDialog {
  protected readonly data = inject<ApplicationDeleteDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<ApplicationDeleteDialog>);

  protected cancel(): void {
    this.dialogRef.close(false);
  }

  protected confirm(): void {
    this.dialogRef.close(true);
  }
}
