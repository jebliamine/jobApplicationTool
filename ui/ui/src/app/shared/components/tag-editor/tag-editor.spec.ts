import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { TagResponse } from '../../../core/tags/tag.models';
import { TagEditor } from './tag-editor';

const REMOTE: TagResponse = { id: '11111111-1111-1111-1111-111111111111', name: 'Remote' };
const REFERRAL: TagResponse = { id: '22222222-2222-2222-2222-222222222222', name: 'Referral' };
const DREAM_JOB: TagResponse = { id: '33333333-3333-3333-3333-333333333333', name: 'Dream job' };

describe('TagEditor', () => {
  let fixture: ComponentFixture<TagEditor>;
  let component: TagEditor;

  function setup(tags: TagResponse[], availableTags: TagResponse[]) {
    TestBed.configureTestingModule({
      imports: [TagEditor, NoopAnimationsModule],
    });

    fixture = TestBed.createComponent(TagEditor);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('tags', tags);
    fixture.componentRef.setInput('availableTags', availableTags);
    fixture.detectChanges();
  }

  it('excludes already-assigned tags from the "add existing" list', () => {
    setup([REMOTE], [REMOTE, REFERRAL, DREAM_JOB]);

    expect(component['unassignedTags']()).toEqual([REFERRAL, DREAM_JOB]);
  });

  it('emits remove with the tag id when a chip is removed', () => {
    setup([REMOTE, REFERRAL], [REMOTE, REFERRAL]);
    const emitted: string[] = [];
    component.remove.subscribe((id) => emitted.push(id));

    component['onRemove'](REMOTE.id);

    expect(emitted).toEqual([REMOTE.id]);
  });

  it('emits add with the selected tag id and resets the select', () => {
    setup([], [REMOTE]);
    const emitted: string[] = [];
    component.add.subscribe((id) => emitted.push(id));
    component['addTagControl'].setValue(REMOTE.id);

    component['onAddSelected']();

    expect(emitted).toEqual([REMOTE.id]);
    expect(component['addTagControl'].value).toBeNull();
  });

  it('emits create with the trimmed name and clears the input', () => {
    setup([], []);
    const emitted: string[] = [];
    component.create.subscribe((name) => emitted.push(name));
    component['newTagNameControl'].setValue('  Dream job  ');

    component['submitNewTag']();

    expect(emitted).toEqual(['Dream job']);
    expect(component['newTagNameControl'].value).toBe('');
  });

  it('does not emit create for a blank name', () => {
    setup([], []);
    const emitted: string[] = [];
    component.create.subscribe((name) => emitted.push(name));
    component['newTagNameControl'].setValue('   ');

    component['submitNewTag']();

    expect(emitted).toEqual([]);
  });

  // Exercises the actual template wiring (typing + a real click), not just the method in
  // isolation — this is the level a missing [formGroup]/(ngSubmit) mismatch would have been
  // caught at (the earlier direct-method tests above didn't touch the template at all).
  it('creates a tag from real keyboard input and a real button click', () => {
    setup([], []);
    const emitted: string[] = [];
    component.create.subscribe((name) => emitted.push(name));

    const input: HTMLInputElement = fixture.nativeElement.querySelector('input');
    input.value = 'Dream job';
    input.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    const button: HTMLButtonElement = fixture.nativeElement.querySelector('button[aria-label="Add new tag"]');
    button.click();

    expect(emitted).toEqual(['Dream job']);
  });

  it('creates a tag by pressing Enter in the input', () => {
    setup([], []);
    const emitted: string[] = [];
    component.create.subscribe((name) => emitted.push(name));

    const input: HTMLInputElement = fixture.nativeElement.querySelector('input');
    input.value = 'Referral';
    input.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    input.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));

    expect(emitted).toEqual(['Referral']);
  });
});
