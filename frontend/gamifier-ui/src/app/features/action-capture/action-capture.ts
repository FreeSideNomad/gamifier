import { Component, OnInit, inject, signal, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AudioService } from '../../core/services/audio.service';
import { ApiService } from '../../core/services/api.service';

export interface ActionType {
  id: string;
  name: string;
  description: string;
  points: number;
  category: string;
}

export interface ActionCapture {
  actionTypeId: string;
  description?: string;
  attachments?: File[];
  location?: string;
  additionalNotes?: string;
}

@Component({
  selector: 'app-action-capture',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './action-capture.html',
  styleUrl: './action-capture.scss'
})
export class ActionCaptureComponent implements OnInit {
  private formBuilder = inject(FormBuilder);
  private audioService = inject(AudioService);
  private apiService = inject(ApiService);

  // Signals
  isVisible = signal(false);
  isLoading = signal(false);
  availableActions = signal<ActionType[]>([]);
  selectedFiles = signal<File[]>([]);

  // Outputs
  actionSubmitted = output<ActionCapture>();
  modalClosed = output<void>();

  // Form
  actionForm!: FormGroup;

  ngOnInit(): void {
    this.initializeForm();
    this.loadAvailableActions();
  }

  private initializeForm(): void {
    this.actionForm = this.formBuilder.group({
      actionTypeId: ['', Validators.required],
      description: [''],
      location: [''],
      additionalNotes: ['', Validators.maxLength(500)]
    });
  }

  private loadAvailableActions(): void {
    // Mock data for now - will be replaced with real API call
    const mockActions: ActionType[] = [
      {
        id: '1',
        name: 'Code Review',
        description: 'Performed thorough code review',
        points: 10,
        category: 'Development'
      },
      {
        id: '2',
        name: 'Bug Fix',
        description: 'Fixed critical system bug',
        points: 15,
        category: 'Development'
      },
      {
        id: '3',
        name: 'Mentoring',
        description: 'Mentored team member',
        points: 20,
        category: 'Leadership'
      },
      {
        id: '4',
        name: 'Documentation',
        description: 'Created technical documentation',
        points: 8,
        category: 'Knowledge'
      },
      {
        id: '5',
        name: 'Process Improvement',
        description: 'Improved team processes',
        points: 25,
        category: 'Leadership'
      }
    ];
    this.availableActions.set(mockActions);
  }

  show(): void {
    this.isVisible.set(true);
    this.actionForm.reset();
    this.selectedFiles.set([]);
    this.audioService.playButtonClick();
  }

  hide(): void {
    this.isVisible.set(false);
    this.modalClosed.emit();
    this.audioService.playButtonClick();
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      const files = Array.from(input.files);
      this.selectedFiles.set(files);
      this.audioService.playButtonClick();
    }
  }

  removeFile(index: number): void {
    const currentFiles = this.selectedFiles();
    currentFiles.splice(index, 1);
    this.selectedFiles.set([...currentFiles]);
    this.audioService.playButtonClick();
  }

  getSelectedAction(): ActionType | undefined {
    const actionId = this.actionForm.get('actionTypeId')?.value;
    return this.availableActions().find(action => action.id === actionId);
  }

  playHoverSound(): void {
    this.audioService.playButtonHover();
  }

  onSubmit(): void {
    if (this.actionForm.valid) {
      this.isLoading.set(true);
      this.audioService.playButtonClick();

      const formValue = this.actionForm.value;
      const actionCapture: ActionCapture = {
        actionTypeId: formValue.actionTypeId,
        description: formValue.description,
        location: formValue.location,
        additionalNotes: formValue.additionalNotes,
        attachments: this.selectedFiles()
      };

      // Simulate API call
      setTimeout(() => {
        this.isLoading.set(false);
        this.actionSubmitted.emit(actionCapture);
        this.hide();
        this.audioService.playSuccess();
      }, 1500);
    } else {
      this.audioService.playError();
    }
  }

  // TrackBy functions for performance optimization
  trackByActionId(index: number, action: ActionType): string {
    return action.id;
  }

  trackByFileIndex(index: number, file: File): string {
    return `${index}-${file.name}-${file.size}`;
  }
}
