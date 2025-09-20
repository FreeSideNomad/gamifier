import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AudioService } from '../../../core/services/audio.service';
import { ApiService } from '../../../core/services/api.service';

export interface ReportData {
  userActivity: UserActivityReport;
  systemStats: SystemStatsReport;
  departmentPerformance: DepartmentPerformanceReport[];
  missionProgress: MissionProgressReport[];
  pointsDistribution: PointsDistributionReport;
  leaderboardSnapshot: LeaderboardReport[];
}

export interface UserActivityReport {
  totalUsers: number;
  activeUsers: number;
  newUsersThisMonth: number;
  averagePointsPerUser: number;
  mostActiveUsers: Array<{
    id: string;
    name: string;
    actionsCount: number;
    pointsEarned: number;
  }>;
  userActivityTrend: Array<{
    date: string;
    activeUsers: number;
    newUsers: number;
  }>;
}

export interface SystemStatsReport {
  totalActions: number;
  actionsThisMonth: number;
  totalMissions: number;
  completedMissions: number;
  totalPoints: number;
  averagePointsPerAction: number;
  systemUptime: string;
  lastDataUpdate: Date;
}

export interface DepartmentPerformanceReport {
  departmentName: string;
  userCount: number;
  totalPoints: number;
  averagePointsPerUser: number;
  completedMissions: number;
  activeUsers: number;
  topPerformer: string;
}

export interface MissionProgressReport {
  missionName: string;
  totalAssigned: number;
  completed: number;
  inProgress: number;
  completionRate: number;
  averageCompletionTime: number;
  difficulty: string;
}

export interface PointsDistributionReport {
  byActionType: Array<{
    actionType: string;
    totalPoints: number;
    actionCount: number;
    percentage: number;
  }>;
  byTimeframe: Array<{
    period: string;
    points: number;
    actions: number;
  }>;
  byDepartment: Array<{
    department: string;
    points: number;
    userCount: number;
  }>;
}

export interface LeaderboardReport {
  rank: number;
  userId: string;
  name: string;
  department: string;
  points: number;
  missionsCompleted: number;
  lastActive: Date;
}

export interface ReportFilter {
  dateRange: 'week' | 'month' | 'quarter' | 'year' | 'custom';
  startDate?: Date;
  endDate?: Date;
  departments: string[];
  userRoles: string[];
  includeInactive: boolean;
}

@Component({
  selector: 'app-reports',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './reports.component.html',
  styleUrl: './reports.component.scss'
})
export class ReportsComponent implements OnInit {
  private formBuilder = inject(FormBuilder);
  private audioService = inject(AudioService);
  private apiService = inject(ApiService);

  // Signals
  isLoading = signal(false);
  currentTab = signal<'overview' | 'users' | 'departments' | 'missions' | 'export'>('overview');
  reportData = signal<ReportData | null>(null);
  selectedTimeframe = signal<'week' | 'month' | 'quarter' | 'year'>('month');

  // Filter form
  filterForm!: FormGroup;

  // Available options
  timeframeOptions = [
    { value: 'week', label: 'Last 7 Days' },
    { value: 'month', label: 'Last 30 Days' },
    { value: 'quarter', label: 'Last 3 Months' },
    { value: 'year', label: 'Last 12 Months' }
  ];

  availableDepartments = [
    'Command', 'Operations', 'Engineering', 'Medical', 'Security'
  ];

  exportFormats = [
    { value: 'pdf', label: 'PDF Report' },
    { value: 'excel', label: 'Excel Spreadsheet' },
    { value: 'csv', label: 'CSV Data' }
  ];

  ngOnInit(): void {
    this.initializeFilterForm();
    this.loadReportData();
  }

  private initializeFilterForm(): void {
    this.filterForm = this.formBuilder.group({
      timeframe: [this.selectedTimeframe(), [Validators.required]],
      startDate: [''],
      endDate: [''],
      departments: [[]],
      userRoles: [[]],
      includeInactive: [false]
    });

    // Watch for timeframe changes
    this.filterForm.get('timeframe')?.valueChanges.subscribe(value => {
      this.selectedTimeframe.set(value);
      this.loadReportData();
    });
  }

  private async loadReportData(): Promise<void> {
    this.isLoading.set(true);
    try {
      // Mock report data generation
      const mockData: ReportData = {
        userActivity: {
          totalUsers: 147,
          activeUsers: 132,
          newUsersThisMonth: 23,
          averagePointsPerUser: 2847,
          mostActiveUsers: [
            { id: '1', name: 'Jean-Luc Picard', actionsCount: 89, pointsEarned: 1420 },
            { id: '2', name: 'William Riker', actionsCount: 67, pointsEarned: 1235 },
            { id: '3', name: 'Data', actionsCount: 72, pointsEarned: 1189 },
            { id: '4', name: 'Geordi La Forge', actionsCount: 54, pointsEarned: 987 },
            { id: '5', name: 'Deanna Troi', actionsCount: 45, pointsEarned: 823 }
          ],
          userActivityTrend: this.generateActivityTrend()
        },
        systemStats: {
          totalActions: 12567,
          actionsThisMonth: 1834,
          totalMissions: 45,
          completedMissions: 1267,
          totalPoints: 418529,
          averagePointsPerAction: 33.3,
          systemUptime: '99.7%',
          lastDataUpdate: new Date()
        },
        departmentPerformance: [
          {
            departmentName: 'Command',
            userCount: 23,
            totalPoints: 67890,
            averagePointsPerUser: 2952,
            completedMissions: 234,
            activeUsers: 22,
            topPerformer: 'Jean-Luc Picard'
          },
          {
            departmentName: 'Operations',
            userCount: 45,
            totalPoints: 89234,
            averagePointsPerUser: 1983,
            completedMissions: 456,
            activeUsers: 41,
            topPerformer: 'Data'
          },
          {
            departmentName: 'Engineering',
            userCount: 38,
            totalPoints: 78345,
            averagePointsPerUser: 2062,
            completedMissions: 378,
            activeUsers: 35,
            topPerformer: 'Geordi La Forge'
          },
          {
            departmentName: 'Medical',
            userCount: 29,
            totalPoints: 56789,
            averagePointsPerUser: 1958,
            completedMissions: 267,
            activeUsers: 26,
            topPerformer: 'Beverly Crusher'
          },
          {
            departmentName: 'Security',
            userCount: 12,
            totalPoints: 34567,
            averagePointsPerUser: 2881,
            completedMissions: 189,
            activeUsers: 11,
            topPerformer: 'Worf'
          }
        ],
        missionProgress: [
          {
            missionName: 'Developer Excellence',
            totalAssigned: 89,
            completed: 67,
            inProgress: 22,
            completionRate: 75.3,
            averageCompletionTime: 14.5,
            difficulty: 'MEDIUM'
          },
          {
            missionName: 'Leadership Challenge',
            totalAssigned: 34,
            completed: 23,
            inProgress: 11,
            completionRate: 67.6,
            averageCompletionTime: 21.2,
            difficulty: 'HARD'
          },
          {
            missionName: 'Team Collaboration',
            totalAssigned: 156,
            completed: 134,
            inProgress: 22,
            completionRate: 85.9,
            averageCompletionTime: 8.7,
            difficulty: 'EASY'
          }
        ],
        pointsDistribution: {
          byActionType: [
            { actionType: 'Code Review', totalPoints: 89234, actionCount: 567, percentage: 35.2 },
            { actionType: 'Team Leadership', totalPoints: 67890, actionCount: 234, percentage: 26.8 },
            { actionType: 'Knowledge Sharing', totalPoints: 45678, actionCount: 345, percentage: 18.0 },
            { actionType: 'Innovation', totalPoints: 34567, actionCount: 123, percentage: 13.6 },
            { actionType: 'Collaboration', totalPoints: 16789, actionCount: 156, percentage: 6.4 }
          ],
          byTimeframe: this.generateTimeframeData(),
          byDepartment: [
            { department: 'Operations', points: 89234, userCount: 45 },
            { department: 'Engineering', points: 78345, userCount: 38 },
            { department: 'Command', points: 67890, userCount: 23 },
            { department: 'Medical', points: 56789, userCount: 29 },
            { department: 'Security', points: 34567, userCount: 12 }
          ]
        },
        leaderboardSnapshot: [
          { rank: 1, userId: '1', name: 'Jean-Luc Picard', department: 'Command', points: 15420, missionsCompleted: 89, lastActive: new Date() },
          { rank: 2, userId: '2', name: 'William Riker', department: 'Command', points: 12350, missionsCompleted: 67, lastActive: new Date(Date.now() - 86400000) },
          { rank: 3, userId: '3', name: 'Data', department: 'Operations', points: 11890, missionsCompleted: 72, lastActive: new Date(Date.now() - 3600000) },
          { rank: 4, userId: '4', name: 'Geordi La Forge', department: 'Engineering', points: 9875, missionsCompleted: 54, lastActive: new Date(Date.now() - 7200000) },
          { rank: 5, userId: '5', name: 'Worf', department: 'Security', points: 9234, missionsCompleted: 61, lastActive: new Date(Date.now() - 10800000) }
        ]
      };

      // Simulate loading delay
      await new Promise(resolve => setTimeout(resolve, 1000));
      this.reportData.set(mockData);
    } catch (error) {
      console.error('Failed to load report data:', error);
    } finally {
      this.isLoading.set(false);
    }
  }

  private generateActivityTrend(): Array<{ date: string; activeUsers: number; newUsers: number }> {
    const data = [];
    const now = new Date();

    for (let i = 29; i >= 0; i--) {
      const date = new Date(now);
      date.setDate(date.getDate() - i);

      data.push({
        date: date.toISOString().split('T')[0],
        activeUsers: Math.floor(Math.random() * 30) + 100,
        newUsers: Math.floor(Math.random() * 5)
      });
    }

    return data;
  }

  private generateTimeframeData(): Array<{ period: string; points: number; actions: number }> {
    const timeframe = this.selectedTimeframe();
    const data = [];

    if (timeframe === 'week') {
      for (let i = 6; i >= 0; i--) {
        const date = new Date();
        date.setDate(date.getDate() - i);
        data.push({
          period: date.toLocaleDateString('en-US', { weekday: 'short' }),
          points: Math.floor(Math.random() * 5000) + 1000,
          actions: Math.floor(Math.random() * 100) + 20
        });
      }
    } else if (timeframe === 'month') {
      for (let i = 29; i >= 0; i--) {
        const date = new Date();
        date.setDate(date.getDate() - i);
        data.push({
          period: date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
          points: Math.floor(Math.random() * 3000) + 500,
          actions: Math.floor(Math.random() * 80) + 10
        });
      }
    }

    return data;
  }

  // Tab navigation
  switchTab(tab: string): void {
    this.currentTab.set(tab as 'overview' | 'users' | 'departments' | 'missions' | 'export');
    this.playClickSound();
  }

  // Filter management
  applyFilters(): void {
    if (this.filterForm.valid) {
      this.loadReportData();
      this.playClickSound();
    }
  }

  resetFilters(): void {
    this.filterForm.reset();
    this.filterForm.patchValue({
      timeframe: 'month',
      departments: [],
      userRoles: [],
      includeInactive: false
    });
    this.selectedTimeframe.set('month');
    this.loadReportData();
    this.playClickSound();
  }

  // Export functionality
  async exportReport(format: string): Promise<void> {
    this.isLoading.set(true);
    try {
      console.log(`Exporting report in ${format} format...`);

      // Simulate export processing
      await new Promise(resolve => setTimeout(resolve, 2000));

      // In real app: this would trigger a download
      const filename = `starfleet-report-${new Date().toISOString().split('T')[0]}.${format}`;
      console.log(`Report exported as: ${filename}`);

      this.playSuccessSound();
    } catch (error) {
      console.error('Failed to export report:', error);
    } finally {
      this.isLoading.set(false);
    }
  }

  async refreshData(): Promise<void> {
    await this.loadReportData();
    this.playSuccessSound();
  }

  // Utility methods
  formatNumber(num: number): string {
    return new Intl.NumberFormat().format(num);
  }

  formatPercentage(num: number): string {
    return `${num.toFixed(1)}%`;
  }

  formatDate(date: Date): string {
    return new Date(date).toLocaleDateString();
  }

  formatDateTime(date: Date): string {
    return new Date(date).toLocaleString();
  }

  calculateGrowth(current: number, previous: number): { value: number; isPositive: boolean } {
    const growth = ((current - previous) / previous) * 100;
    return {
      value: Math.abs(growth),
      isPositive: growth >= 0
    };
  }

  getDepartmentColor(index: number): string {
    const colors = ['#FF9900', '#9999FF', '#99CCFF', '#66CCCC', '#FFCC99'];
    return colors[index % colors.length];
  }

  getMissionDifficultyColor(difficulty: string): string {
    switch (difficulty) {
      case 'EASY': return '#99CC99';
      case 'MEDIUM': return '#FFCC99';
      case 'HARD': return '#FF9999';
      default: return '#9999FF';
    }
  }

  // Chart data preparation
  get departmentChartData(): Array<{ name: string; value: number; color: string }> {
    const report = this.reportData();
    if (!report) return [];

    return report.departmentPerformance.map((dept, index) => ({
      name: dept.departmentName,
      value: dept.totalPoints,
      color: this.getDepartmentColor(index)
    }));
  }

  get actionTypeChartData(): Array<{ name: string; value: number; percentage: number }> {
    const report = this.reportData();
    if (!report) return [];

    return report.pointsDistribution.byActionType.map(item => ({
      name: item.actionType,
      value: item.totalPoints,
      percentage: item.percentage
    }));
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
}