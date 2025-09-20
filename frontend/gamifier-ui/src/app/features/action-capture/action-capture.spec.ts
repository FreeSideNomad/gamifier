import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

import { ActionCaptureComponent, ActionType, ActionCapture } from './action-capture';
import { AudioService } from '../../core/services/audio.service';
import { ApiService } from '../../core/services/api.service';

describe('ActionCaptureComponent', () => {
  let component: ActionCaptureComponent;
  let fixture: ComponentFixture<ActionCaptureComponent>;
  let mockAudioService: jasmine.SpyObj<AudioService>;
  let mockApiService: jasmine.SpyObj<ApiService>;

  beforeEach(async () => {
    const audioServiceSpy = jasmine.createSpyObj('AudioService', [
      'playButtonClick',
      'playButtonHover',
      'playSuccess',
      'playError'
    ]);

    const apiServiceSpy = jasmine.createSpyObj('ApiService', ['get', 'post']);

    await TestBed.configureTestingModule({
      imports: [
        ActionCaptureComponent,
        ReactiveFormsModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: AudioService, useValue: audioServiceSpy },
        { provide: ApiService, useValue: apiServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ActionCaptureComponent);
    component = fixture.componentInstance;
    mockAudioService = TestBed.inject(AudioService) as jasmine.SpyObj<AudioService>;
    mockApiService = TestBed.inject(ApiService) as jasmine.SpyObj<ApiService>;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with default values', () => {
    expect(component.isVisible()).toBeFalse();
    expect(component.isLoading()).toBeFalse();
    expect(component.selectedFiles()).toEqual([]);
    expect(component.availableActions().length).toBeGreaterThan(0);
  });

  it('should load available actions on init', () => {
    const actions = component.availableActions();
    expect(actions.length).toBe(5);
    expect(actions[0].name).toBe('Code Review');
    expect(actions[0].points).toBe(10);
    expect(actions[0].category).toBe('Development');
  });

  it('should initialize form with correct validators', () => {
    const form = component.actionForm;
    expect(form.get('actionTypeId')?.hasError('required')).toBeTrue();
    expect(form.get('additionalNotes')?.hasError('maxlength')).toBeFalse();

    // Test maxlength validator
    const longText = 'a'.repeat(501);
    form.get('additionalNotes')?.setValue(longText);
    expect(form.get('additionalNotes')?.hasError('maxlength')).toBeTrue();
  });

  describe('Modal visibility', () => {
    it('should show modal when show() is called', () => {
      component.show();
      expect(component.isVisible()).toBeTrue();
      expect(mockAudioService.playButtonClick).toHaveBeenCalled();
    });

    it('should hide modal when hide() is called', () => {
      component.show();
      component.hide();
      expect(component.isVisible()).toBeFalse();
      expect(mockAudioService.playButtonClick).toHaveBeenCalledTimes(2);
    });

    it('should reset form when showing modal', () => {
      component.actionForm.get('actionTypeId')?.setValue('1');
      component.selectedFiles.set([new File(['test'], 'test.txt')]);

      component.show();

      expect(component.actionForm.get('actionTypeId')?.value).toBe(null);
      expect(component.selectedFiles()).toEqual([]);
    });
  });

  describe('File handling', () => {
    it('should handle file selection', () => {
      const file = new File(['test content'], 'test.txt', { type: 'text/plain' });
      const event = {
        target: {
          files: [file]
        }
      } as any;

      component.onFileSelected(event);

      expect(component.selectedFiles()).toEqual([file]);
      expect(mockAudioService.playButtonClick).toHaveBeenCalled();
    });

    it('should handle multiple file selection', () => {
      const file1 = new File(['test1'], 'test1.txt');
      const file2 = new File(['test2'], 'test2.pdf');
      const event = {
        target: {
          files: [file1, file2]
        }
      } as any;

      component.onFileSelected(event);

      expect(component.selectedFiles()).toEqual([file1, file2]);
    });

    it('should remove file when removeFile() is called', () => {
      const file1 = new File(['test1'], 'test1.txt');
      const file2 = new File(['test2'], 'test2.pdf');
      component.selectedFiles.set([file1, file2]);

      component.removeFile(0);

      expect(component.selectedFiles()).toEqual([file2]);
      expect(mockAudioService.playButtonClick).toHaveBeenCalled();
    });

    it('should handle empty file input', () => {
      const event = {
        target: {
          files: null
        }
      } as any;

      component.onFileSelected(event);

      expect(component.selectedFiles()).toEqual([]);
    });
  });

  describe('Action selection', () => {
    it('should return selected action when actionTypeId is set', () => {
      component.actionForm.get('actionTypeId')?.setValue('1');

      const selectedAction = component.getSelectedAction();

      expect(selectedAction?.id).toBe('1');
      expect(selectedAction?.name).toBe('Code Review');
    });

    it('should return undefined when no action is selected', () => {
      const selectedAction = component.getSelectedAction();
      expect(selectedAction).toBeUndefined();
    });

    it('should return undefined for invalid actionTypeId', () => {
      component.actionForm.get('actionTypeId')?.setValue('invalid');

      const selectedAction = component.getSelectedAction();
      expect(selectedAction).toBeUndefined();
    });
  });

  describe('Form submission', () => {
    beforeEach(() => {
      jasmine.clock().install();
    });

    afterEach(() => {
      jasmine.clock().uninstall();
    });

    it('should submit valid form successfully', () => {
      component.actionForm.patchValue({
        actionTypeId: '1',
        description: 'Test description',
        location: 'Test location',
        additionalNotes: 'Test notes'
      });

      const file = new File(['test'], 'test.txt');
      component.selectedFiles.set([file]);

      spyOn(component.actionSubmitted, 'emit');
      spyOn(component, 'hide');

      component.onSubmit();

      expect(component.isLoading()).toBeTrue();
      expect(mockAudioService.playButtonClick).toHaveBeenCalled();

      // Fast-forward time to simulate async operation
      jasmine.clock().tick(1500);

      expect(component.isLoading()).toBeFalse();
      expect(component.actionSubmitted.emit).toHaveBeenCalledWith({
        actionTypeId: '1',
        description: 'Test description',
        location: 'Test location',
        additionalNotes: 'Test notes',
        attachments: [file]
      });
      expect(component.hide).toHaveBeenCalled();
      expect(mockAudioService.playSuccess).toHaveBeenCalled();
    });

    it('should not submit invalid form', () => {
      spyOn(component.actionSubmitted, 'emit');

      component.onSubmit();

      expect(component.isLoading()).toBeFalse();
      expect(component.actionSubmitted.emit).not.toHaveBeenCalled();
      expect(mockAudioService.playError).toHaveBeenCalled();
    });

    it('should submit minimal valid form', () => {
      component.actionForm.get('actionTypeId')?.setValue('2');

      spyOn(component.actionSubmitted, 'emit');
      spyOn(component, 'hide');

      component.onSubmit();
      jasmine.clock().tick(1500);

      expect(component.actionSubmitted.emit).toHaveBeenCalledWith({
        actionTypeId: '2',
        description: '',
        location: '',
        additionalNotes: '',
        attachments: []
      });
    });
  });

  describe('Audio interactions', () => {
    it('should play hover sound when playHoverSound() is called', () => {
      component.playHoverSound();
      expect(mockAudioService.playButtonHover).toHaveBeenCalled();
    });
  });

  describe('TrackBy functions', () => {
    it('should track actions by id', () => {
      const action: ActionType = {
        id: 'test-id',
        name: 'Test Action',
        description: 'Test',
        points: 10,
        category: 'Test'
      };

      const result = component.trackByActionId(0, action);
      expect(result).toBe('test-id');
    });

    it('should track files by index, name and size', () => {
      const file = new File(['test'], 'test.txt', { type: 'text/plain' });

      const result = component.trackByFileIndex(1, file);
      expect(result).toBe('1-test.txt-4');
    });
  });

  describe('Component Integration', () => {
    it('should emit modalClosed when hide() is called', () => {
      spyOn(component.modalClosed, 'emit');

      component.hide();

      expect(component.modalClosed.emit).toHaveBeenCalled();
    });

    it('should handle form reset properly', () => {
      component.actionForm.patchValue({
        actionTypeId: '1',
        description: 'test',
        location: 'test',
        additionalNotes: 'test'
      });
      component.selectedFiles.set([new File(['test'], 'test.txt')]);

      component.show(); // This should reset the form

      expect(component.actionForm.get('actionTypeId')?.value).toBe(null);
      expect(component.actionForm.get('description')?.value).toBe(null);
      expect(component.selectedFiles()).toEqual([]);
    });
  });
});
