import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { LucideTriangleAlert } from '@lucide/angular';

export interface JobDeleteDialogData {
  jobTitle: string;
}

@Component({
  selector: 'app-job-delete-dialog',
  imports: [MatButtonModule, MatDialogModule, LucideTriangleAlert],
  templateUrl: './job-delete-dialog.html',
  styleUrl: './job-delete-dialog.scss',
})
export class JobDeleteDialog {
  protected readonly data = inject<JobDeleteDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<JobDeleteDialog>);

  protected cancel(): void {
    this.dialogRef.close(false);
  }

  protected confirm(): void {
    this.dialogRef.close(true);
  }
}
