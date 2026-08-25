import { DatePipe } from '@angular/common';
import { Component, input, output } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { LucidePlus, LucideX } from '@lucide/angular';
import { InterviewStageRequest, InterviewStageResponse } from '../application.models';

interface StageFormControls {
  title: FormControl<string>;
  scheduledDate: FormControl<string>;
}

/**
 * Presentational multi-round interview pipeline editor for the Application detail page. The host
 * page owns persistence: it calls ApplicationService's addInterviewStage/updateInterviewStage/
 * removeInterviewStage after `add`/`toggleCompleted`/`remove`, same division of responsibility as
 * TagEditor.
 */
@Component({
  selector: 'app-interview-stage-editor',
  imports: [DatePipe, ReactiveFormsModule, MatButtonModule, MatCheckboxModule, MatFormFieldModule, MatInputModule, LucidePlus, LucideX],
  templateUrl: './interview-stage-editor.html',
  styleUrl: './interview-stage-editor.scss',
})
export class InterviewStageEditor {
  readonly stages = input.required<InterviewStageResponse[]>();
  readonly saving = input(false);

  readonly add = output<InterviewStageRequest>();
  readonly remove = output<string>();
  readonly toggleCompleted = output<{ stageId: string; completed: boolean }>();

  protected readonly form = new FormGroup<StageFormControls>({
    title: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    scheduledDate: new FormControl('', { nonNullable: true }),
  });

  protected submitNewStage(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const raw = this.form.getRawValue();
    this.add.emit({
      title: raw.title.trim(),
      scheduledDate: raw.scheduledDate || null,
      notes: null,
      completed: false,
    });
    this.form.reset({ title: '', scheduledDate: '' });
  }

  protected onToggleCompleted(stage: InterviewStageResponse): void {
    this.toggleCompleted.emit({ stageId: stage.id, completed: !stage.completed });
  }

  protected onRemove(stageId: string): void {
    this.remove.emit(stageId);
  }
}
