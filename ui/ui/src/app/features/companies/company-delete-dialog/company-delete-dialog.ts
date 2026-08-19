import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { LucideTriangleAlert } from '@lucide/angular';

export interface CompanyDeleteDialogData {
  companyName: string;
}

@Component({
  selector: 'app-company-delete-dialog',
  imports: [MatButtonModule, MatDialogModule, LucideTriangleAlert],
  templateUrl: './company-delete-dialog.html',
  styleUrl: './company-delete-dialog.scss',
})
export class CompanyDeleteDialog {
  protected readonly data = inject<CompanyDeleteDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<CompanyDeleteDialog>);

  protected cancel(): void {
    this.dialogRef.close(false);
  }

  protected confirm(): void {
    this.dialogRef.close(true);
  }
}
