import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { InterviewStageResponse } from '../application.models';
import { InterviewStageEditor } from './interview-stage-editor';

const PHONE_SCREEN: InterviewStageResponse = {
  id: '11111111-1111-1111-1111-111111111111',
  title: 'Phone Screen',
  scheduledDate: '2026-02-01',
  notes: null,
  completed: false,
};

describe('InterviewStageEditor', () => {
  let fixture: ComponentFixture<InterviewStageEditor>;
  let component: InterviewStageEditor;

  function setup(stages: InterviewStageResponse[]) {
    TestBed.configureTestingModule({
      imports: [InterviewStageEditor, NoopAnimationsModule],
    });

    fixture = TestBed.createComponent(InterviewStageEditor);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('stages', stages);
    fixture.detectChanges();
  }

  it('does not emit add when the title is blank', () => {
    setup([]);
    const emitted: unknown[] = [];
    component.add.subscribe((request) => emitted.push(request));

    component['submitNewStage']();

    expect(emitted).toEqual([]);
    expect(component['form'].controls.title.touched).toBe(true);
  });

  it('emits add with the trimmed title and date, then resets the form', () => {
    setup([]);
    const emitted: unknown[] = [];
    component.add.subscribe((request) => emitted.push(request));
    component['form'].setValue({ title: '  Onsite  ', scheduledDate: '2026-03-01' });

    component['submitNewStage']();

    expect(emitted).toEqual([{ title: 'Onsite', scheduledDate: '2026-03-01', notes: null, completed: false }]);
    expect(component['form'].controls.title.value).toBe('');
  });

  it('emits add with a null scheduledDate when no date is entered', () => {
    setup([]);
    const emitted: unknown[] = [];
    component.add.subscribe((request) => emitted.push(request));
    component['form'].setValue({ title: 'Onsite', scheduledDate: '' });

    component['submitNewStage']();

    expect(emitted).toEqual([{ title: 'Onsite', scheduledDate: null, notes: null, completed: false }]);
  });

  it('emits toggleCompleted with the flipped completed value', () => {
    setup([PHONE_SCREEN]);
    const emitted: unknown[] = [];
    component.toggleCompleted.subscribe((event) => emitted.push(event));

    component['onToggleCompleted'](PHONE_SCREEN);

    expect(emitted).toEqual([{ stageId: PHONE_SCREEN.id, completed: true }]);
  });

  it('emits remove with the stage id', () => {
    setup([PHONE_SCREEN]);
    const emitted: string[] = [];
    component.remove.subscribe((id) => emitted.push(id));

    component['onRemove'](PHONE_SCREEN.id);

    expect(emitted).toEqual([PHONE_SCREEN.id]);
  });
});
