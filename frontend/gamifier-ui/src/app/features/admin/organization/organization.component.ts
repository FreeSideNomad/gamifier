import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormArray } from '@angular/forms';
import { AudioService } from '../../../core/services/audio.service';
import { ApiService } from '../../../core/services/api.service';

export interface Organization {
  id: string;
  name: string;
  description: string;
  domain: string;
  settings: OrganizationSettings;
}

export interface OrganizationSettings {
  pointsPerAction: number;
  requireApproval: boolean;
  allowSelfReporting: boolean;
  notificationEmails: string[];
}

export interface ActionType {
  id: string;
  name: string;
  description: string;
  points: number;
  category: string;
  requiresApproval: boolean;
  isActive: boolean;
}

export interface MissionType {
  id: string;
  name: string;
  description: string;
  requirements: MissionRequirement[];
  rewardPoints: number;
  badgeIcon: string;
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
  isActive: boolean;
}

export interface MissionRequirement {
  actionTypeId: string;
  requiredCount: number;
  description: string;
}

export interface Rank {
  id: string;
  name: string;
  minPoints: number;
  maxPoints: number;
  insignia: string;
  color: string;
  benefits: string[];
  order: number;
}

@Component({
  selector: 'app-organization',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './organization.component.html',
  styleUrl: './organization.component.scss'
})
export class OrganizationComponent implements OnInit {
  private formBuilder = inject(FormBuilder);
  private audioService = inject(AudioService);
  private apiService = inject(ApiService);

  // Signals
  isLoading = signal(false);
  currentTab = signal<'organization' | 'actions' | 'missions' | 'ranks'>('organization');
  organization = signal<Organization | null>(null);
  actionTypes = signal<ActionType[]>([]);
  missionTypes = signal<MissionType[]>([]);
  ranks = signal<Rank[]>([]);

  // Modal states
  showActionModal = signal(false);
  showMissionModal = signal(false);
  showRankModal = signal(false);
  editingItem = signal<any>(null);

  // Forms
  organizationForm!: FormGroup;
  actionForm!: FormGroup;
  missionForm!: FormGroup;
  rankForm!: FormGroup;

  // Categories for action types
  actionCategories = [
    'Development',
    'Leadership',
    'Knowledge',
    'Innovation',
    'Collaboration'
  ];

  // Difficulties for missions
  missionDifficulties = [
    { value: 'EASY', label: 'Ensign Level' },
    { value: 'MEDIUM', label: 'Lieutenant Level' },
    { value: 'HARD', label: 'Captain Level' }
  ];

  ngOnInit(): void {
    this.initializeForms();
    this.loadOrganizationData();
  }

  private initializeForms(): void {
    this.organizationForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', [Validators.required]]
    });

    this.actionForm = this.formBuilder.group({
      name: ['', [Validators.required]],
      description: ['', [Validators.required]],
      points: [10, [Validators.required, Validators.min(1)]],
      category: ['', [Validators.required]],
      requiresApproval: [false],
      isActive: [true]
    });

    this.missionForm = this.formBuilder.group({
      name: ['', [Validators.required]],
      description: ['', [Validators.required]],
      rewardPoints: [100, [Validators.required, Validators.min(1)]],
      difficulty: ['MEDIUM', [Validators.required]],
      badgeIcon: ['star'],
      requirements: this.formBuilder.array([]),
      isActive: [true]
    });

    this.rankForm = this.formBuilder.group({
      name: ['', [Validators.required]],
      minPoints: [0, [Validators.required, Validators.min(0)]],
      maxPoints: [999999, [Validators.required]],
      insignia: [''],
      color: ['#FF9900', [Validators.required]],
      benefits: this.formBuilder.array([]),
      order: [0, [Validators.required]]
    });
  }

  private async loadOrganizationData(): Promise<void> {
    this.isLoading.set(true);
    try {
      // Load organization data
      const orgData = await this.loadOrganization();
      if (orgData) {
        this.organization.set(orgData);
        this.updateOrganizationForm(orgData);
      } else {
        // No organization exists yet - this is fine for new setups
        console.log('No organization found - ready to create new organization');
      }

      // Load configuration data
      await Promise.all([
        this.loadActionTypes(),
        this.loadMissionTypes(),
        this.loadRanks()
      ]);
    } catch (error) {
      console.error('Failed to load organization data:', error);
    } finally {
      this.isLoading.set(false);
    }
  }

  private async loadOrganization(): Promise<Organization | null> {
    try {
      const response = await fetch('http://localhost:9080/api/organization');
      if (response.ok) {
        const organizations = await response.json();
        if (organizations.length > 0) {
          return organizations[0]; // Get the first organization
        } else {
          console.log('No organizations found');
          return null;
        }
      } else {
        console.error('Failed to load organization:', response.status);
        return null;
      }
    } catch (error) {
      console.error('Failed to load organization:', error);
      return null;
    }
  }

  private async loadActionTypes(): Promise<void> {
    try {
      // Need to get organization first to get action types
      const orgs = await fetch('http://localhost:9080/api/organization');
      if (orgs.ok) {
        const orgList = await orgs.json();
        if (orgList.length > 0) {
          const orgId = orgList[0].id;
          const response = await fetch(`http://localhost:9080/api/organization/${orgId}/action-types`);
      if (response.ok) {
        const data = await response.json();
        this.actionTypes.set(data);
      } else {
        console.error('Failed to load action types:', response.status);
        this.actionTypes.set([]);
      }
    } catch (error) {
      console.error('Failed to load action types:', error);
      this.actionTypes.set([]);
    }
  }

  private async loadMissionTypes(): Promise<void> {
    try {
      // Need to get organization first to get mission types
      const orgs = await fetch('http://localhost:9080/api/organization');
      if (orgs.ok) {
        const orgList = await orgs.json();
        if (orgList.length > 0) {
          const orgId = orgList[0].id;
          const response = await fetch(`http://localhost:9080/api/organization/${orgId}/mission-types`);
      if (response.ok) {
        const data = await response.json();
        this.missionTypes.set(data);
      } else {
        console.error('Failed to load mission types:', response.status);
        this.missionTypes.set([]);
      }
    } catch (error) {
      console.error('Failed to load mission types:', error);
      this.missionTypes.set([]);
    }
  }

  private async loadRanks(): Promise<void> {
    try {
      // Need to get organization first to get ranks
      const orgs = await fetch('http://localhost:9080/api/organization');
      if (orgs.ok) {
        const orgList = await orgs.json();
        if (orgList.length > 0) {
          const orgId = orgList[0].id;
          const response = await fetch(`http://localhost:9080/api/organization/${orgId}/ranks`);
      if (response.ok) {
        const data = await response.json();
        this.ranks.set(data);
      } else {
        console.error('Failed to load ranks:', response.status);
        this.ranks.set([]);
      }
    } catch (error) {
      console.error('Failed to load ranks:', error);
      this.ranks.set([]);
    }
  }

  private updateOrganizationForm(org: Organization): void {
    this.organizationForm.patchValue({
      name: org.name,
      description: org.description || ''
    });
  }

  // Tab navigation
  switchTab(tab: 'organization' | 'actions' | 'missions' | 'ranks'): void {
    this.currentTab.set(tab);
    this.playClickSound();
  }

  // Organization management
  async saveOrganization(): Promise<void> {
    if (this.organizationForm.valid) {
      this.isLoading.set(true);
      try {
        const formData = this.organizationForm.value;

        // Auto-generate federationId from organization name
        const federationId = this.generateFederationId(formData.name);

        const organizationData = {
          name: formData.name,
          federationId: federationId,
          description: formData.description || ''
        };

        const response = await fetch('http://localhost:9080/api/organization', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(organizationData)
        });

        if (response.ok) {
          const newOrg = await response.json();
          this.organization.set(newOrg);
          this.updateOrganizationForm(newOrg);
          this.playSuccessSound();
          console.log('Organization created successfully:', newOrg);
        } else {
          const errorText = await response.text();
          throw new Error(`Failed to create organization: ${response.status} - ${errorText}`);
        }
      } catch (error) {
        console.error('Failed to save organization:', error);
        alert(`Error creating organization: ${error.message}`);
      } finally {
        this.isLoading.set(false);
      }
    } else {
      alert('Please fill in all required fields correctly.');
    }
  }

  private generateFederationId(name: string): string {
    // Convert name to uppercase, replace spaces/special chars with dashes, take first 8 chars
    const baseId = name.toUpperCase()
      .replace(/[^A-Z0-9]/g, '-')
      .replace(/--+/g, '-')
      .replace(/^-+|-+$/g, '')
      .substring(0, 8);

    // Add timestamp to ensure uniqueness
    const timestamp = Date.now().toString().slice(-3);
    return `${baseId}-${timestamp}`;
  }

  // Action type management
  openActionModal(action?: ActionType): void {
    this.editingItem.set(action || null);
    if (action) {
      this.actionForm.patchValue(action);
    } else {
      this.actionForm.reset();
      this.actionForm.patchValue({ points: 10, isActive: true });
    }
    this.showActionModal.set(true);
    this.playClickSound();
  }

  async saveAction(): Promise<void> {
    if (this.actionForm.valid) {
      this.isLoading.set(true);
      try {
        const formData = this.actionForm.value;
        const currentActions = this.actionTypes();

        if (this.editingItem()) {
          // Update existing
          const updated = currentActions.map(a =>
            a.id === this.editingItem()?.id ? { ...a, ...formData } : a
          );
          this.actionTypes.set(updated);
        } else {
          // Add new
          const newAction: ActionType = {
            id: Date.now().toString(),
            ...formData
          };
          this.actionTypes.set([...currentActions, newAction]);
        }

        this.showActionModal.set(false);
        this.playSuccessSound();
      } catch (error) {
        console.error('Failed to save action:', error);
      } finally {
        this.isLoading.set(false);
      }
    }
  }

  deleteAction(actionId: string): void {
    const updated = this.actionTypes().filter(a => a.id !== actionId);
    this.actionTypes.set(updated);
    this.playClickSound();
  }

  // Mission type management
  openMissionModal(mission?: MissionType): void {
    this.editingItem.set(mission || null);
    if (mission) {
      this.missionForm.patchValue(mission);
      this.updateMissionRequirements(mission.requirements);
    } else {
      this.missionForm.reset();
      this.missionForm.patchValue({ difficulty: 'MEDIUM', badgeIcon: 'star', rewardPoints: 100, isActive: true });
      this.updateMissionRequirements([]);
    }
    this.showMissionModal.set(true);
    this.playClickSound();
  }

  private updateMissionRequirements(requirements: MissionRequirement[]): void {
    const reqArray = this.missionForm.get('requirements') as FormArray;
    reqArray.clear();
    requirements.forEach(req => {
      reqArray.push(this.formBuilder.group({
        actionTypeId: [req.actionTypeId, [Validators.required]],
        requiredCount: [req.requiredCount, [Validators.required, Validators.min(1)]],
        description: [req.description, [Validators.required]]
      }));
    });
  }

  addMissionRequirement(): void {
    const reqArray = this.missionForm.get('requirements') as FormArray;
    reqArray.push(this.formBuilder.group({
      actionTypeId: ['', [Validators.required]],
      requiredCount: [1, [Validators.required, Validators.min(1)]],
      description: ['', [Validators.required]]
    }));
    this.playClickSound();
  }

  removeMissionRequirement(index: number): void {
    const reqArray = this.missionForm.get('requirements') as FormArray;
    reqArray.removeAt(index);
    this.playClickSound();
  }

  async saveMission(): Promise<void> {
    if (this.missionForm.valid) {
      this.isLoading.set(true);
      try {
        const formData = this.missionForm.value;
        const currentMissions = this.missionTypes();

        if (this.editingItem()) {
          // Update existing
          const updated = currentMissions.map(m =>
            m.id === this.editingItem()?.id ? { ...m, ...formData } : m
          );
          this.missionTypes.set(updated);
        } else {
          // Add new
          const newMission: MissionType = {
            id: Date.now().toString(),
            ...formData
          };
          this.missionTypes.set([...currentMissions, newMission]);
        }

        this.showMissionModal.set(false);
        this.playSuccessSound();
      } catch (error) {
        console.error('Failed to save mission:', error);
      } finally {
        this.isLoading.set(false);
      }
    }
  }

  deleteMission(missionId: string): void {
    const updated = this.missionTypes().filter(m => m.id !== missionId);
    this.missionTypes.set(updated);
    this.playClickSound();
  }

  // Rank management
  openRankModal(rank?: Rank): void {
    this.editingItem.set(rank || null);
    if (rank) {
      this.rankForm.patchValue(rank);
      this.updateRankBenefits(rank.benefits);
    } else {
      this.rankForm.reset();
      this.rankForm.patchValue({ minPoints: 0, maxPoints: 999999, color: '#FF9900', order: 0 });
      this.updateRankBenefits([]);
    }
    this.showRankModal.set(true);
    this.playClickSound();
  }

  private updateRankBenefits(benefits: string[]): void {
    const benefitArray = this.rankForm.get('benefits') as FormArray;
    benefitArray.clear();
    benefits.forEach(benefit => {
      benefitArray.push(this.formBuilder.control(benefit, [Validators.required]));
    });
  }

  addRankBenefit(): void {
    const benefitArray = this.rankForm.get('benefits') as FormArray;
    benefitArray.push(this.formBuilder.control('', [Validators.required]));
    this.playClickSound();
  }

  removeRankBenefit(index: number): void {
    const benefitArray = this.rankForm.get('benefits') as FormArray;
    benefitArray.removeAt(index);
    this.playClickSound();
  }

  async saveRank(): Promise<void> {
    if (this.rankForm.valid) {
      this.isLoading.set(true);
      try {
        const formData = this.rankForm.value;
        const currentRanks = this.ranks();

        if (this.editingItem()) {
          // Update existing
          const updated = currentRanks.map(r =>
            r.id === this.editingItem()?.id ? { ...r, ...formData } : r
          );
          this.ranks.set(updated);
        } else {
          // Add new
          const newRank: Rank = {
            id: Date.now().toString(),
            ...formData
          };
          this.ranks.set([...currentRanks, newRank].sort((a, b) => a.order - b.order));
        }

        this.showRankModal.set(false);
        this.playSuccessSound();
      } catch (error) {
        console.error('Failed to save rank:', error);
      } finally {
        this.isLoading.set(false);
      }
    }
  }

  deleteRank(rankId: string): void {
    const updated = this.ranks().filter(r => r.id !== rankId);
    this.ranks.set(updated);
    this.playClickSound();
  }

  // Email management
  get notificationEmails(): FormArray {
    return this.organizationForm.get('notificationEmails') as FormArray;
  }

  addEmail(): void {
    this.notificationEmails.push(this.formBuilder.control('', [Validators.email]));
    this.playClickSound();
  }

  removeEmail(index: number): void {
    this.notificationEmails.removeAt(index);
    this.playClickSound();
  }

  // Mission requirements getters
  get missionRequirements(): FormArray {
    return this.missionForm.get('requirements') as FormArray;
  }

  get rankBenefits(): FormArray {
    return this.rankForm.get('benefits') as FormArray;
  }

  // Modal controls
  closeModal(): void {
    this.showActionModal.set(false);
    this.showMissionModal.set(false);
    this.showRankModal.set(false);
    this.editingItem.set(null);
    this.playClickSound();
  }

  // Audio feedback
  playHoverSound(): void {
    this.audioService.playButtonHover();
  }

  playClickSound(): void {
    this.audioService.playButtonClick();
  }

  private playSuccessSound(): void {
    this.audioService.playSuccess();
  }

  // Utility methods
  trackByActionId(index: number, action: ActionType): string {
    return action.id;
  }

  trackByMissionId(index: number, mission: MissionType): string {
    return mission.id;
  }

  trackByRankId(index: number, rank: Rank): string {
    return rank.id;
  }

  getActionTypeName(actionTypeId: string): string {
    return this.actionTypes().find(a => a.id === actionTypeId)?.name || 'Unknown Action';
  }
}