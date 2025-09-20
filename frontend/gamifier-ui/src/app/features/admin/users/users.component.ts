import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AudioService } from '../../../core/services/audio.service';
import { ApiService } from '../../../core/services/api.service';

export interface User {
  id: string;
  name: string;
  email: string;
  rank: string;
  points: number;
  role: 'USER' | 'ADMIN';
  department: string;
  joinDate: Date;
  lastActive: Date;
  isActive: boolean;
  managerId?: string;
  completedMissions: number;
  pendingActions: number;
}

export interface UserStats {
  totalUsers: number;
  activeUsers: number;
  adminUsers: number;
  newUsersThisMonth: number;
  topPerformers: User[];
}

export interface Department {
  id: string;
  name: string;
  description: string;
  headId?: string;
  userCount: number;
}

@Component({
  selector: 'app-users',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './users.component.html',
  styleUrl: './users.component.scss'
})
export class UsersComponent implements OnInit {
  private formBuilder = inject(FormBuilder);
  private audioService = inject(AudioService);
  private apiService = inject(ApiService);

  // Signals
  isLoading = signal(false);
  currentTab = signal<'users' | 'departments' | 'bulk-import'>('users');
  users = signal<User[]>([]);
  departments = signal<Department[]>([]);
  userStats = signal<UserStats | null>(null);

  // Pagination and filtering
  currentPage = signal(1);
  pageSize = signal(10);
  totalUsers = signal(0);
  searchTerm = signal('');
  selectedDepartment = signal('');
  selectedRole = signal('');
  selectedStatus = signal('');

  // Modal states
  showUserModal = signal(false);
  showDepartmentModal = signal(false);
  showBulkImportModal = signal(false);
  editingUser = signal<User | null>(null);
  editingDepartment = signal<Department | null>(null);

  // Forms
  userForm!: FormGroup;
  departmentForm!: FormGroup;
  bulkImportForm!: FormGroup;

  // Options
  availableRanks = [
    'CADET', 'ENSIGN', 'LIEUTENANT_JR', 'LIEUTENANT',
    'LIEUTENANT_COMMANDER', 'COMMANDER', 'CAPTAIN', 'ADMIRAL'
  ];

  availableRoles = [
    { value: 'USER', label: 'Standard User' },
    { value: 'ADMIN', label: 'Administrator' }
  ];

  ngOnInit(): void {
    this.initializeForms();
    this.loadUserData();
  }

  private initializeForms(): void {
    this.userForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      rank: ['ENSIGN', [Validators.required]],
      role: ['USER', [Validators.required]],
      department: ['', [Validators.required]],
      managerId: [''],
      isActive: [true]
    });

    this.departmentForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      description: ['', [Validators.required]],
      headId: ['']
    });

    this.bulkImportForm = this.formBuilder.group({
      csvFile: [null, [Validators.required]],
      overwriteExisting: [false],
      sendWelcomeEmails: [true]
    });
  }

  private async loadUserData(): Promise<void> {
    this.isLoading.set(true);
    try {
      await Promise.all([
        this.loadUsers(),
        this.loadDepartments(),
        this.loadUserStats()
      ]);
    } catch (error) {
      console.error('Failed to load user data:', error);
    } finally {
      this.isLoading.set(false);
    }
  }

  private async loadUsers(): Promise<void> {
    // Mock user data
    const mockUsers: User[] = [
      {
        id: '1',
        name: 'Jean-Luc Picard',
        email: 'picard@starfleet.fed',
        rank: 'CAPTAIN',
        points: 15420,
        role: 'ADMIN',
        department: 'Command',
        joinDate: new Date('2024-01-15'),
        lastActive: new Date(),
        isActive: true,
        completedMissions: 89,
        pendingActions: 2
      },
      {
        id: '2',
        name: 'William T. Riker',
        email: 'riker@starfleet.fed',
        rank: 'COMMANDER',
        points: 12350,
        role: 'USER',
        department: 'Command',
        joinDate: new Date('2024-02-01'),
        lastActive: new Date(Date.now() - 86400000), // 1 day ago
        isActive: true,
        managerId: '1',
        completedMissions: 67,
        pendingActions: 5
      },
      {
        id: '3',
        name: 'Data',
        email: 'data@starfleet.fed',
        rank: 'LIEUTENANT_COMMANDER',
        points: 11890,
        role: 'USER',
        department: 'Operations',
        joinDate: new Date('2024-01-20'),
        lastActive: new Date(Date.now() - 3600000), // 1 hour ago
        isActive: true,
        completedMissions: 72,
        pendingActions: 1
      },
      {
        id: '4',
        name: 'Geordi La Forge',
        email: 'laforge@starfleet.fed',
        rank: 'LIEUTENANT_COMMANDER',
        points: 9875,
        role: 'USER',
        department: 'Engineering',
        joinDate: new Date('2024-02-10'),
        lastActive: new Date(Date.now() - 7200000), // 2 hours ago
        isActive: true,
        completedMissions: 54,
        pendingActions: 3
      },
      {
        id: '5',
        name: 'Deanna Troi',
        email: 'troi@starfleet.fed',
        rank: 'LIEUTENANT_COMMANDER',
        points: 8234,
        role: 'USER',
        department: 'Medical',
        joinDate: new Date('2024-03-01'),
        lastActive: new Date(Date.now() - 172800000), // 2 days ago
        isActive: false,
        completedMissions: 45,
        pendingActions: 0
      }
    ];

    this.users.set(mockUsers);
    this.totalUsers.set(mockUsers.length);
  }

  private async loadDepartments(): Promise<void> {
    // Mock department data
    const mockDepartments: Department[] = [
      {
        id: '1',
        name: 'Command',
        description: 'Ship command and strategic operations',
        headId: '1',
        userCount: 2
      },
      {
        id: '2',
        name: 'Operations',
        description: 'Operations and tactical systems',
        headId: '3',
        userCount: 1
      },
      {
        id: '3',
        name: 'Engineering',
        description: 'Engineering and technical systems',
        headId: '4',
        userCount: 1
      },
      {
        id: '4',
        name: 'Medical',
        description: 'Medical and counseling services',
        userCount: 1
      },
      {
        id: '5',
        name: 'Security',
        description: 'Security and tactical operations',
        userCount: 0
      }
    ];

    this.departments.set(mockDepartments);
  }

  private async loadUserStats(): Promise<void> {
    const users = this.users();
    const stats: UserStats = {
      totalUsers: users.length,
      activeUsers: users.filter(u => u.isActive).length,
      adminUsers: users.filter(u => u.role === 'ADMIN').length,
      newUsersThisMonth: users.filter(u => {
        const monthAgo = new Date();
        monthAgo.setMonth(monthAgo.getMonth() - 1);
        return u.joinDate > monthAgo;
      }).length,
      topPerformers: users
        .filter(u => u.isActive)
        .sort((a, b) => b.points - a.points)
        .slice(0, 5)
    };

    this.userStats.set(stats);
  }

  // Tab navigation
  switchTab(tab: 'users' | 'departments' | 'bulk-import'): void {
    this.currentTab.set(tab);
    this.playClickSound();
  }

  // User management
  openUserModal(user?: User): void {
    this.editingUser.set(user || null);
    if (user) {
      this.userForm.patchValue({
        name: user.name,
        email: user.email,
        rank: user.rank,
        role: user.role,
        department: user.department,
        managerId: user.managerId || '',
        isActive: user.isActive
      });
    } else {
      this.userForm.reset();
      this.userForm.patchValue({
        rank: 'ENSIGN',
        role: 'USER',
        isActive: true
      });
    }
    this.showUserModal.set(true);
    this.playClickSound();
  }

  async saveUser(): Promise<void> {
    if (this.userForm.valid) {
      this.isLoading.set(true);
      try {
        const formData = this.userForm.value;
        const currentUsers = this.users();

        if (this.editingUser()) {
          // Update existing user
          const updated = currentUsers.map(u =>
            u.id === this.editingUser()?.id
              ? { ...u, ...formData, lastActive: new Date() }
              : u
          );
          this.users.set(updated);
        } else {
          // Create new user
          const newUser: User = {
            id: Date.now().toString(),
            name: formData.name,
            email: formData.email,
            rank: formData.rank,
            role: formData.role,
            department: formData.department,
            managerId: formData.managerId || undefined,
            isActive: formData.isActive,
            points: 0,
            joinDate: new Date(),
            lastActive: new Date(),
            completedMissions: 0,
            pendingActions: 0
          };
          this.users.set([...currentUsers, newUser]);
          this.totalUsers.set(this.totalUsers() + 1);
        }

        await this.loadUserStats(); // Refresh stats
        this.showUserModal.set(false);
        this.playSuccessSound();
      } catch (error) {
        console.error('Failed to save user:', error);
      } finally {
        this.isLoading.set(false);
      }
    }
  }

  async toggleUserStatus(userId: string): Promise<void> {
    const users = this.users();
    const updated = users.map(u =>
      u.id === userId ? { ...u, isActive: !u.isActive } : u
    );
    this.users.set(updated);
    await this.loadUserStats();
    this.playClickSound();
  }

  async deleteUser(userId: string): Promise<void> {
    if (confirm('Are you sure you want to delete this user? This action cannot be undone.')) {
      const updated = this.users().filter(u => u.id !== userId);
      this.users.set(updated);
      this.totalUsers.set(this.totalUsers() - 1);
      await this.loadUserStats();
      this.playClickSound();
    }
  }

  async resetUserPassword(userId: string): Promise<void> {
    if (confirm('Are you sure you want to reset this user\'s password?')) {
      console.log('Resetting password for user:', userId);
      // In real app: await this.apiService.resetUserPassword(userId);
      this.playSuccessSound();
    }
  }

  // Department management
  openDepartmentModal(department?: Department): void {
    this.editingDepartment.set(department || null);
    if (department) {
      this.departmentForm.patchValue({
        name: department.name,
        description: department.description,
        headId: department.headId || ''
      });
    } else {
      this.departmentForm.reset();
    }
    this.showDepartmentModal.set(true);
    this.playClickSound();
  }

  async saveDepartment(): Promise<void> {
    if (this.departmentForm.valid) {
      this.isLoading.set(true);
      try {
        const formData = this.departmentForm.value;
        const currentDepartments = this.departments();

        if (this.editingDepartment()) {
          // Update existing department
          const updated = currentDepartments.map(d =>
            d.id === this.editingDepartment()?.id
              ? { ...d, ...formData }
              : d
          );
          this.departments.set(updated);
        } else {
          // Create new department
          const newDepartment: Department = {
            id: Date.now().toString(),
            name: formData.name,
            description: formData.description,
            headId: formData.headId || undefined,
            userCount: 0
          };
          this.departments.set([...currentDepartments, newDepartment]);
        }

        this.showDepartmentModal.set(false);
        this.playSuccessSound();
      } catch (error) {
        console.error('Failed to save department:', error);
      } finally {
        this.isLoading.set(false);
      }
    }
  }

  async deleteDepartment(departmentId: string): Promise<void> {
    const department = this.departments().find(d => d.id === departmentId);
    if (department && department.userCount > 0) {
      alert('Cannot delete department with active users. Please reassign users first.');
      return;
    }

    if (confirm('Are you sure you want to delete this department?')) {
      const updated = this.departments().filter(d => d.id !== departmentId);
      this.departments.set(updated);
      this.playClickSound();
    }
  }

  // Bulk import
  openBulkImportModal(): void {
    this.bulkImportForm.reset();
    this.bulkImportForm.patchValue({
      overwriteExisting: false,
      sendWelcomeEmails: true
    });
    this.showBulkImportModal.set(true);
    this.playClickSound();
  }

  onFileSelected(event: any): void {
    const file = event.target.files?.[0];
    if (file) {
      this.bulkImportForm.patchValue({ csvFile: file });
      this.playClickSound();
    }
  }

  async processBulkImport(): Promise<void> {
    if (this.bulkImportForm.valid) {
      this.isLoading.set(true);
      try {
        const formData = this.bulkImportForm.value;
        console.log('Processing bulk import:', formData);

        // Simulate processing
        await new Promise(resolve => setTimeout(resolve, 2000));

        // Mock: Add some sample imported users
        const importedUsers: User[] = [
          {
            id: 'import-1',
            name: 'Beverly Crusher',
            email: 'crusher@starfleet.fed',
            rank: 'COMMANDER',
            points: 0,
            role: 'USER',
            department: 'Medical',
            joinDate: new Date(),
            lastActive: new Date(),
            isActive: true,
            completedMissions: 0,
            pendingActions: 0
          }
        ];

        this.users.set([...this.users(), ...importedUsers]);
        this.totalUsers.set(this.totalUsers() + importedUsers.length);
        await this.loadUserStats();

        this.showBulkImportModal.set(false);
        this.playSuccessSound();
      } catch (error) {
        console.error('Failed to process bulk import:', error);
      } finally {
        this.isLoading.set(false);
      }
    }
  }

  // Filtering and pagination
  get filteredUsers(): User[] {
    let filtered = this.users();

    if (this.searchTerm()) {
      const term = this.searchTerm().toLowerCase();
      filtered = filtered.filter(u =>
        u.name.toLowerCase().includes(term) ||
        u.email.toLowerCase().includes(term)
      );
    }

    if (this.selectedDepartment()) {
      filtered = filtered.filter(u => u.department === this.selectedDepartment());
    }

    if (this.selectedRole()) {
      filtered = filtered.filter(u => u.role === this.selectedRole());
    }

    if (this.selectedStatus()) {
      const isActive = this.selectedStatus() === 'active';
      filtered = filtered.filter(u => u.isActive === isActive);
    }

    return filtered;
  }

  get paginatedUsers(): User[] {
    const filtered = this.filteredUsers;
    const startIndex = (this.currentPage() - 1) * this.pageSize();
    return filtered.slice(startIndex, startIndex + this.pageSize());
  }

  get totalPages(): number {
    return Math.ceil(this.filteredUsers.length / this.pageSize());
  }

  changePage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage.set(page);
      this.playClickSound();
    }
  }

  updateSearchTerm(term: string): void {
    this.searchTerm.set(term);
    this.currentPage.set(1); // Reset to first page
  }

  updateDepartmentFilter(department: string): void {
    this.selectedDepartment.set(department);
    this.currentPage.set(1);
    this.playClickSound();
  }

  updateRoleFilter(role: string): void {
    this.selectedRole.set(role);
    this.currentPage.set(1);
    this.playClickSound();
  }

  updateStatusFilter(status: string): void {
    this.selectedStatus.set(status);
    this.currentPage.set(1);
    this.playClickSound();
  }

  // Modal controls
  closeModal(): void {
    this.showUserModal.set(false);
    this.showDepartmentModal.set(false);
    this.showBulkImportModal.set(false);
    this.editingUser.set(null);
    this.editingDepartment.set(null);
    this.playClickSound();
  }

  // Utility methods
  getUserName(userId: string): string {
    return this.users().find(u => u.id === userId)?.name || 'Unknown User';
  }

  getDepartmentName(departmentId: string): string {
    return this.departments().find(d => d.id === departmentId)?.name || 'Unknown Department';
  }

  formatRank(rank: string): string {
    return rank.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase());
  }

  formatDate(date: Date): string {
    return new Date(date).toLocaleDateString();
  }

  formatLastActive(date: Date): string {
    const now = new Date();
    const diff = now.getTime() - new Date(date).getTime();
    const hours = Math.floor(diff / (1000 * 60 * 60));

    if (hours < 1) return 'Just now';
    if (hours < 24) return `${hours}h ago`;

    const days = Math.floor(hours / 24);
    if (days < 7) return `${days}d ago`;

    const weeks = Math.floor(days / 7);
    return `${weeks}w ago`;
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

  // TrackBy functions
  trackByUserId(index: number, user: User): string {
    return user.id;
  }

  trackByDepartmentId(index: number, department: Department): string {
    return department.id;
  }
}