import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../core/services/api.service';
import { AudioService } from '../../core/services/audio.service';

export interface DashboardStats {
  totalPoints: number;
  rank: string;
  missionsCompleted: number;
  activeMissions: number;
  leaderboardPosition: number;
  weeklyProgress: number;
}

export interface RecentActivity {
  id: string;
  type: 'MISSION_COMPLETED' | 'POINTS_EARNED' | 'RANK_PROMOTION' | 'ACTION_LOGGED';
  description: string;
  points?: number;
  timestamp: Date;
  icon: string;
}

export interface MissionPreview {
  id: string;
  title: string;
  description: string;
  progress: number;
  totalSteps: number;
  completedSteps: number;
  pointsReward: number;
  deadline?: Date;
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
}

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule],
  templateUrl: './dashboard-new.html',
  styleUrls: ['../../proxima-theme.scss']
})
export class DashboardComponent implements OnInit {
  private apiService = inject(ApiService);
  private audioService = inject(AudioService);

  // Dashboard Data
  stats = signal<DashboardStats>({
    totalPoints: 0,
    rank: 'CADET',
    missionsCompleted: 0,
    activeMissions: 0,
    leaderboardPosition: 0,
    weeklyProgress: 0
  });

  recentActivity = signal<RecentActivity[]>([]);
  activeMissions = signal<MissionPreview[]>([]);
  topLeaderboard = signal<any[]>([]);

  // UI State
  isLoading = signal(true);
  selectedTimeframe = signal<'week' | 'month' | 'year'>('week');

  // Computed values
  progressPercentage = computed(() => {
    const stats = this.stats();
    return Math.min((stats.weeklyProgress / 100) * 100, 100);
  });

  ngOnInit(): void {
    this.loadDashboardData();
  }

  private async loadDashboardData(): Promise<void> {
    try {
      this.isLoading.set(true);

      // Simulate API calls for now - will be replaced with real API calls
      await this.loadUserStats();
      await this.loadRecentActivity();
      await this.loadActiveMissions();
      await this.loadLeaderboardPreview();

      this.isLoading.set(false);
      this.audioService.playSuccess();
    } catch (error) {
      console.error('Failed to load dashboard data:', error);
      this.isLoading.set(false);
      this.audioService.playError();
    }
  }

  private async loadUserStats(): Promise<void> {
    // Mock data - replace with API call
    const mockStats: DashboardStats = {
      totalPoints: 2847,
      rank: 'COMMANDER',
      missionsCompleted: 15,
      activeMissions: 3,
      leaderboardPosition: 7,
      weeklyProgress: 68
    };

    this.stats.set(mockStats);
  }

  private async loadRecentActivity(): Promise<void> {
    // Mock data - replace with API call
    const mockActivity: RecentActivity[] = [
      {
        id: '1',
        type: 'MISSION_COMPLETED',
        description: 'Completed "Data Analysis Protocol"',
        points: 150,
        timestamp: new Date(Date.now() - 2 * 60 * 60 * 1000),
        icon: '🎯'
      },
      {
        id: '2',
        type: 'POINTS_EARNED',
        description: 'Code review contribution',
        points: 25,
        timestamp: new Date(Date.now() - 4 * 60 * 60 * 1000),
        icon: '⭐'
      },
      {
        id: '3',
        type: 'ACTION_LOGGED',
        description: 'Logged daily standup attendance',
        points: 10,
        timestamp: new Date(Date.now() - 6 * 60 * 60 * 1000),
        icon: '📝'
      },
      {
        id: '4',
        type: 'RANK_PROMOTION',
        description: 'Promoted to Commander',
        timestamp: new Date(Date.now() - 24 * 60 * 60 * 1000),
        icon: '🏆'
      }
    ];

    this.recentActivity.set(mockActivity);
  }

  private async loadActiveMissions(): Promise<void> {
    // Mock data - replace with API call
    const mockMissions: MissionPreview[] = [
      {
        id: '1',
        title: 'Security Protocol Review',
        description: 'Review and update security protocols for Deck 7',
        progress: 75,
        totalSteps: 4,
        completedSteps: 3,
        pointsReward: 200,
        deadline: new Date(Date.now() + 3 * 24 * 60 * 60 * 1000),
        difficulty: 'MEDIUM'
      },
      {
        id: '2',
        title: 'Engineering Efficiency Analysis',
        description: 'Analyze warp core efficiency metrics',
        progress: 30,
        totalSteps: 5,
        completedSteps: 1,
        pointsReward: 300,
        difficulty: 'HARD'
      },
      {
        id: '3',
        title: 'Team Coordination Exercise',
        description: 'Coordinate bridge crew training simulation',
        progress: 90,
        totalSteps: 3,
        completedSteps: 2,
        pointsReward: 150,
        deadline: new Date(Date.now() + 1 * 24 * 60 * 60 * 1000),
        difficulty: 'EASY'
      }
    ];

    this.activeMissions.set(mockMissions);
  }

  private async loadLeaderboardPreview(): Promise<void> {
    // Mock data - replace with API call
    const mockLeaderboard = [
      { rank: 1, name: 'Captain Picard', points: 4250, change: 0 },
      { rank: 2, name: 'Lt. Commander Data', points: 3890, change: 1 },
      { rank: 3, name: 'Commander Riker', points: 3654, change: -1 },
      { rank: 4, name: 'Lt. Worf', points: 3420, change: 2 },
      { rank: 5, name: 'Lt. Commander Geordi', points: 3180, change: 0 }
    ];

    this.topLeaderboard.set(mockLeaderboard);
  }

  // Event Handlers
  onTimeframeChange(timeframe: 'week' | 'month' | 'year'): void {
    this.selectedTimeframe.set(timeframe);
    this.audioService.playButtonClick();
    this.loadDashboardData(); // Reload with new timeframe
  }

  onMissionClick(mission: MissionPreview): void {
    this.audioService.playButtonClick();
    // Navigate to mission details - will be implemented with routing
    console.log('Navigate to mission:', mission.id);
  }

  onRefresh(): void {
    this.audioService.playButtonClick();
    this.loadDashboardData();
  }

  getDifficultyColor(difficulty: string): string {
    switch (difficulty) {
      case 'EASY': return 'lcars-text-green';
      case 'MEDIUM': return 'lcars-text-amber';
      case 'HARD': return 'lcars-text-red';
      default: return 'lcars-text-blue';
    }
  }

  getActivityIcon(type: string): string {
    switch (type) {
      case 'MISSION_COMPLETED': return '🎯';
      case 'POINTS_EARNED': return '⭐';
      case 'RANK_PROMOTION': return '🏆';
      case 'ACTION_LOGGED': return '📝';
      default: return '📊';
    }
  }

  getDifficultyAccent(difficulty: string): string {
    switch (difficulty) {
      case 'EASY': return 'green';
      case 'MEDIUM': return 'amber';
      case 'HARD': return 'red';
      default: return 'blue';
    }
  }

  playHoverSound(): void {
    this.audioService.playButtonHover();
  }

  // Track By Functions for Performance
  trackByMissionId(index: number, mission: MissionPreview): string {
    return mission.id;
  }

  trackByActivityId(index: number, activity: RecentActivity): string {
    return activity.id;
  }

  trackByLeaderboardRank(index: number, entry: any): number {
    return entry.rank;
  }

  abs(value: number): number {
    return Math.abs(value);
  }

  // Quick Action Handlers
  onLogAction(): void {
    this.audioService.playButtonClick();
    console.log('Navigate to Log Action page');
    // TODO: Navigate to actions page or show modal
  }

  onViewMissions(): void {
    this.audioService.playButtonClick();
    console.log('Navigate to Missions page');
    // TODO: Navigate to missions page
  }

  onViewLeaderboard(): void {
    this.audioService.playButtonClick();
    console.log('Navigate to Leaderboard page');
    // TODO: Navigate to leaderboards page
  }

  onViewAnalytics(): void {
    this.audioService.playButtonClick();
    console.log('Navigate to Analytics page');
    // TODO: Navigate to analytics/reports page
  }
}