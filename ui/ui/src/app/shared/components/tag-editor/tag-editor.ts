import { Component, computed, input, output } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { LucidePlus, LucideX } from '@lucide/angular';
import { TagResponse } from '../../../core/tags/tag.models';

/**
 * Presentational tag editor reused on Job and Application detail pages — knows nothing about
 * either domain. The host page owns persistence: it calls the relevant *.service `setTags()`
 * after `add`/`remove`, and `TagService.create()` (then `add`) after `create`.
 */
@Component({
  selector: 'app-tag-editor',
  imports: [ReactiveFormsModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule, LucidePlus, LucideX],
  templateUrl: './tag-editor.html',
  styleUrl: './tag-editor.scss',
})
export class TagEditor {
  readonly tags = input.required<TagResponse[]>();
  readonly availableTags = input.required<TagResponse[]>();
  readonly saving = input(false);

  readonly add = output<string>();
  readonly remove = output<string>();
  readonly create = output<string>();

  protected readonly addTagControl = new FormControl<string | null>(null);
  protected readonly newTagNameControl = new FormControl('', { nonNullable: true });

  protected readonly unassignedTags = computed(() => {
    const assignedIds = new Set(this.tags().map((tag) => tag.id));
    return this.availableTags().filter((tag) => !assignedIds.has(tag.id));
  });

  protected onRemove(tagId: string): void {
    this.remove.emit(tagId);
  }

  protected onAddSelected(): void {
    const tagId = this.addTagControl.value;
    if (tagId) {
      this.add.emit(tagId);
    }
    this.addTagControl.setValue(null);
  }

  protected submitNewTag(): void {
    const name = this.newTagNameControl.value.trim();
    if (!name) {
      return;
    }
    this.create.emit(name);
    this.newTagNameControl.setValue('');
  }
}
