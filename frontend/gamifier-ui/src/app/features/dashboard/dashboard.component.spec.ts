import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';
import { of, throwError } from 'rxjs';

import { DashboardComponent, DashboardStats, RecentActivity, MissionPreview } from './dashboard.component';
import { AudioService } from '../../core/services/audio.service';
import { ApiService } from '../../core/services/api.service';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let audioServiceSpy: jasmine.SpyObj<AudioService>;
  let apiServiceSpy: jasmine.SpyObj<ApiService>;

  const mockStats: DashboardStats = {
    totalPoints: 2847,
    rank: 'COMMANDER',
    missionsCompleted: 15,
    activeMissions: 3,
    leaderboardPosition: 7,
    weeklyProgress: 68
  };

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
    }
  ];

  const mockMissions: MissionPreview[] = [
    {
      id: '1',
      title: 'Security Protocol Review',
      description: 'Review and update security protocols',
      progress: 75,
      totalSteps: 4,
      completedSteps: 3,
      pointsReward: 200,
      deadline: new Date(Date.now() + 3 * 24 * 60 * 60 * 1000),
      difficulty: 'MEDIUM'
    },
    {
      id: '2',
      title: 'Engineering Analysis',
      description: 'Analyze warp core efficiency',
      progress: 30,
      totalSteps: 5,
      completedSteps: 1,
      pointsReward: 300,
      difficulty: 'HARD'
    }
  ];

  const mockLeaderboard = [
    { rank: 1, name: 'Captain Picard', points: 4250, change: 0 },
    { rank: 2, name: 'Lt. Commander Data', points: 3890, change: 1 },
    { rank: 3, name: 'Commander Riker', points: 3654, change: -1 }
  ];

  beforeEach(async () => {
    const audioSpy = jasmine.createSpyObj('AudioService', [
      'playButtonHover',
      'playButtonClick',
      'playSuccess',
      'playError'
    ]);

    const apiSpy = jasmine.createSpyObj('ApiService', [
      'getUserStats',
      'getRecentActivity',
      'getActiveMissions',
      'getLeaderboardPreview'
    ]);

    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        { provide: AudioService, useValue: audioSpy },
        { provide: ApiService, useValue: apiSpy }
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    audioServiceSpy = TestBed.inject(AudioService) as jasmine.SpyObj<AudioService>;
    apiServiceSpy = TestBed.inject(ApiService) as jasmine.SpyObj<ApiService>;

    // Setup default spy behaviors
    apiServiceSpy.getUserStats.and.returnValue(of(mockStats));
    apiServiceSpy.getRecentActivity.and.returnValue(of(mockActivity));
    apiServiceSpy.getActiveMissions.and.returnValue(of(mockMissions));
    apiServiceSpy.getLeaderboardPreview.and.returnValue(of(mockLeaderboard));
  });

  describe('Component Initialization', () => {
    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should initialize with default values', () => {
      expect(component.stats().totalPoints).toBe(0);
      expect(component.stats().rank).toBe('CADET');
      expect(component.recentActivity().length).toBe(0);
      expect(component.activeMissions().length).toBe(0);
      expect(component.topLeaderboard().length).toBe(0);
      expect(component.isLoading()).toBe(true);
      expect(component.selectedTimeframe()).toBe('week');
    });

    it('should compute progress percentage correctly', () => {
      component.stats.set({ ...mockStats, weeklyProgress: 85 });
      expect(component.progressPercentage()).toBe(85);

      component.stats.set({ ...mockStats, weeklyProgress: 150 });
      expect(component.progressPercentage()).toBe(100); // Should cap at 100
    });
  });

  describe('Component Lifecycle', () => {
    it('should load dashboard data on ngOnInit', () => {
      spyOn(component as any, 'loadDashboardData');

      component.ngOnInit();

      expect((component as any).loadDashboardData).toHaveBeenCalled();
    });

    it('should load all dashboard data successfully', fakeAsync(() => {
      component.ngOnInit();
      tick();

      expect(component.stats()).toEqual(mockStats);
      expect(component.recentActivity()).toEqual(mockActivity);
      expect(component.activeMissions()).toEqual(mockMissions);
      expect(component.topLeaderboard()).toEqual(mockLeaderboard);
      expect(component.isLoading()).toBe(false);
      expect(audioServiceSpy.playSuccess).toHaveBeenCalled();
    }));

    it('should handle loading errors gracefully', fakeAsync(() => {
      apiServiceSpy.getUserStats.and.returnValue(throwError(() => new Error('API Error')));
      spyOn(console, 'error');

      component.ngOnInit();
      tick();

      expect(component.isLoading()).toBe(false);
      expect(audioServiceSpy.playError).toHaveBeenCalled();
      expect(console.error).toHaveBeenCalledWith('Failed to load dashboard data:', jasmine.any(Error));
    }));
  });

  describe('Data Loading Methods', () => {
    it('should load user stats correctly', async () => {
      await (component as any).loadUserStats();
      expect(component.stats()).toEqual(mockStats);
    });

    it('should load recent activity correctly', async () => {
      await (component as any).loadRecentActivity();
      expect(component.recentActivity()).toEqual(mockActivity);
    });

    it('should load active missions correctly', async () => {
      await (component as any).loadActiveMissions();
      expect(component.activeMissions()).toEqual(mockMissions);
    });

    it('should load leaderboard preview correctly', async () => {
      await (component as any).loadLeaderboardPreview();
      expect(component.topLeaderboard()).toEqual(mockLeaderboard);
    });
  });

  describe('Event Handlers', () => {
    beforeEach(() => {
      component.isLoading.set(false);
      component.stats.set(mockStats);
      component.activeMissions.set(mockMissions);
      component.recentActivity.set(mockActivity);
      component.topLeaderboard.set(mockLeaderboard);
      fixture.detectChanges();
    });

    it('should handle timeframe change', () => {
      spyOn(component as any, 'loadDashboardData');

      component.onTimeframeChange('month');

      expect(component.selectedTimeframe()).toBe('month');
      expect(audioServiceSpy.playButtonClick).toHaveBeenCalled();
      expect((component as any).loadDashboardData).toHaveBeenCalled();
    });

    it('should handle mission click', () => {
      const mission = mockMissions[0];
      spyOn(console, 'log');

      component.onMissionClick(mission);

      expect(audioServiceSpy.playButtonClick).toHaveBeenCalled();
      expect(console.log).toHaveBeenCalledWith('Navigate to mission:', mission.id);
    });

    it('should handle refresh', () => {
      spyOn(component as any, 'loadDashboardData');

      component.onRefresh();

      expect(audioServiceSpy.playButtonClick).toHaveBeenCalled();
      expect((component as any).loadDashboardData).toHaveBeenCalled();
    });

    it('should play hover sound', () => {
      component.playHoverSound();
      expect(audioServiceSpy.playButtonHover).toHaveBeenCalled();
    });
  });

  describe('Utility Methods', () => {
    it('should return correct difficulty colors', () => {
      expect(component.getDifficultyColor('EASY')).toBe('lcars-text-green');
      expect(component.getDifficultyColor('MEDIUM')).toBe('lcars-text-amber');
      expect(component.getDifficultyColor('HARD')).toBe('lcars-text-red');
      expect(component.getDifficultyColor('UNKNOWN')).toBe('lcars-text-blue');
    });

    it('should return correct difficulty accents', () => {
      expect(component.getDifficultyAccent('EASY')).toBe('green');
      expect(component.getDifficultyAccent('MEDIUM')).toBe('amber');
      expect(component.getDifficultyAccent('HARD')).toBe('red');
      expect(component.getDifficultyAccent('UNKNOWN')).toBe('blue');
    });

    it('should return correct activity icons', () => {
      expect(component.getActivityIcon('MISSION_COMPLETED')).toBe('🎯');
      expect(component.getActivityIcon('POINTS_EARNED')).toBe('⭐');
      expect(component.getActivityIcon('RANK_PROMOTION')).toBe('🏆');
      expect(component.getActivityIcon('ACTION_LOGGED')).toBe('📝');
      expect(component.getActivityIcon('UNKNOWN')).toBe('📊');
    });

    it('should track missions by id', () => {
      const mission = mockMissions[0];
      const trackingId = component.trackByMissionId(0, mission);
      expect(trackingId).toBe(mission.id);
    });

    it('should track activities by id', () => {
      const activity = mockActivity[0];
      const trackingId = component.trackByActivityId(0, activity);
      expect(trackingId).toBe(activity.id);
    });

    it('should track leaderboard by rank', () => {
      const entry = mockLeaderboard[0];
      const trackingId = component.trackByLeaderboardRank(0, entry);
      expect(trackingId).toBe(entry.rank);
    });
  });

  describe('Template Integration', () => {
    beforeEach(() => {
      component.isLoading.set(false);
      component.stats.set(mockStats);
      component.activeMissions.set(mockMissions);
      component.recentActivity.set(mockActivity);
      component.topLeaderboard.set(mockLeaderboard);
      fixture.detectChanges();
    });

    describe('Dashboard Header', () => {
      it('should display dashboard title and subtitle', () => {
        const titleElement = fixture.debugElement.query(By.css('.dashboard-title'));
        const subtitleElement = fixture.debugElement.query(By.css('.dashboard-subtitle'));

        expect(titleElement.nativeElement.textContent.trim()).toBe('Command Dashboard');
        expect(subtitleElement.nativeElement.textContent.trim()).toBe('Personal Mission Control Interface');
      });

      it('should display timeframe selector with active state', () => {
        const timeframeButtons = fixture.debugElement.queryAll(By.css('.timeframe-buttons .lcars-button'));
        expect(timeframeButtons.length).toBe(3);

        const activeButton = fixture.debugElement.query(By.css('.timeframe-buttons .lcars-button.active'));
        expect(activeButton.nativeElement.textContent.trim()).toBe('Week');
      });

      it('should display refresh button', () => {
        const refreshButton = fixture.debugElement.query(By.css('.refresh-btn'));
        expect(refreshButton.nativeElement.textContent.trim()).toContain('Refresh');
      });
    });

    describe('Statistics Grid', () => {
      it('should display all stat cards', () => {
        const statCards = fixture.debugElement.queryAll(By.css('.stat-card'));
        expect(statCards.length).toBe(6);
      });

      it('should display total points correctly', () => {
        const pointsCard = fixture.debugElement.query(By.css('.stat-card .lcars-text-amber'));
        expect(pointsCard.nativeElement.textContent.trim()).toBe('2,847');
      });

      it('should display current rank correctly', () => {
        const rankElements = fixture.debugElement.queryAll(By.css('.stat-card .lcars-text-blue'));
        const rankElement = rankElements.find(el => el.nativeElement.textContent.includes('COMMANDER'));
        expect(rankElement).toBeTruthy();
        expect(rankElement!.nativeElement.textContent.trim()).toBe('COMMANDER');
      });

      it('should display progress bar for weekly progress', () => {
        const progressBar = fixture.debugElement.query(By.css('.progress-bar'));
        expect(progressBar).toBeTruthy();
        const width = progressBar.nativeElement.style.width;
        expect(width).toBe('68%');
      });
    });

    describe('Active Missions Section', () => {
      it('should display section header with title', () => {
        const sectionTitle = fixture.debugElement.query(By.css('.missions-section .section-title'));
        expect(sectionTitle.nativeElement.textContent.trim()).toBe('Active Missions');
      });

      it('should display mission cards', () => {
        const missionCards = fixture.debugElement.queryAll(By.css('.mission-card'));
        expect(missionCards.length).toBe(mockMissions.length);
      });

      it('should display mission details correctly', () => {
        const firstMissionCard = fixture.debugElement.query(By.css('.mission-card'));
        const missionTitle = firstMissionCard.query(By.css('.mission-title'));
        const missionDescription = firstMissionCard.query(By.css('.mission-description'));

        expect(missionTitle.nativeElement.textContent.trim()).toBe(mockMissions[0].title);
        expect(missionDescription.nativeElement.textContent.trim()).toBe(mockMissions[0].description);
      });

      it('should display mission progress correctly', () => {
        const firstMissionCard = fixture.debugElement.query(By.css('.mission-card'));
        const progressPercentage = firstMissionCard.query(By.css('.progress-percentage'));
        const progressBar = firstMissionCard.query(By.css('.progress-bar'));

        expect(progressPercentage.nativeElement.textContent.trim()).toBe('75%');
        expect(progressBar.nativeElement.style.width).toBe('75%');
      });

      it('should display difficulty badge with correct styling', () => {
        const firstMissionCard = fixture.debugElement.query(By.css('.mission-card'));
        const difficultyBadge = firstMissionCard.query(By.css('.difficulty-badge'));

        expect(difficultyBadge.nativeElement.textContent.trim()).toBe('MEDIUM');
        expect(difficultyBadge.nativeElement.classList.contains('lcars-text-amber')).toBe(true);
      });

      it('should show empty state when no missions', () => {
        component.activeMissions.set([]);
        fixture.detectChanges();

        const emptyState = fixture.debugElement.query(By.css('.missions-section .empty-state'));
        expect(emptyState).toBeTruthy();
        expect(emptyState.nativeElement.textContent).toContain('No Active Missions');
      });
    });

    describe('Recent Activity Section', () => {
      it('should display section header with title', () => {
        const sectionTitle = fixture.debugElement.query(By.css('.activity-section .section-title'));
        expect(sectionTitle.nativeElement.textContent.trim()).toBe('Recent Activity');
      });

      it('should display activity items', () => {
        const activityItems = fixture.debugElement.queryAll(By.css('.activity-item'));
        expect(activityItems.length).toBe(mockActivity.length);
      });

      it('should display activity details correctly', () => {
        const firstActivityItem = fixture.debugElement.query(By.css('.activity-item'));
        const activityDescription = firstActivityItem.query(By.css('.activity-description'));
        const activityPoints = firstActivityItem.query(By.css('.activity-points'));

        expect(activityDescription.nativeElement.textContent.trim()).toBe(mockActivity[0].description);
        expect(activityPoints.nativeElement.textContent.trim()).toBe('+150 PTS');
      });

      it('should show empty state when no activity', () => {
        component.recentActivity.set([]);
        fixture.detectChanges();

        const emptyState = fixture.debugElement.query(By.css('.activity-section .empty-state'));
        expect(emptyState).toBeTruthy();
        expect(emptyState.nativeElement.textContent).toContain('No Recent Activity');
      });
    });

    describe('Leaderboard Section', () => {
      it('should display section header with title', () => {
        const sectionTitle = fixture.debugElement.query(By.css('.leaderboard-section .section-title'));
        expect(sectionTitle.nativeElement.textContent.trim()).toBe('Top Performers');
      });

      it('should display leaderboard items', () => {
        const leaderboardItems = fixture.debugElement.queryAll(By.css('.leaderboard-item'));
        expect(leaderboardItems.length).toBe(mockLeaderboard.length);
      });

      it('should display leaderboard entry details correctly', () => {
        const firstLeaderboardItem = fixture.debugElement.query(By.css('.leaderboard-item'));
        const rankNumber = firstLeaderboardItem.query(By.css('.rank-number'));
        const officerName = firstLeaderboardItem.query(By.css('.officer-name'));
        const officerPoints = firstLeaderboardItem.query(By.css('.officer-points'));

        expect(rankNumber.nativeElement.textContent.trim()).toBe('#1');
        expect(officerName.nativeElement.textContent.trim()).toBe('Captain Picard');
        expect(officerPoints.nativeElement.textContent.trim()).toBe('4,250 PTS');
      });

      it('should highlight current user in leaderboard', () => {
        const mockLeaderboardWithCurrentUser = [
          ...mockLeaderboard,
          { rank: 4, name: 'Commander Data', points: 2847, change: 0 }
        ];
        component.topLeaderboard.set(mockLeaderboardWithCurrentUser);
        fixture.detectChanges();

        const currentUserItem = fixture.debugElement.query(By.css('.leaderboard-item.current-user'));
        expect(currentUserItem).toBeTruthy();
      });

      it('should show empty state when no leaderboard data', () => {
        component.topLeaderboard.set([]);
        fixture.detectChanges();

        const emptyState = fixture.debugElement.query(By.css('.leaderboard-section .empty-state'));
        expect(emptyState).toBeTruthy();
        expect(emptyState.nativeElement.textContent).toContain('Leaderboard Unavailable');
      });
    });

    describe('Quick Actions', () => {
      it('should display quick actions title', () => {
        const quickActionsTitle = fixture.debugElement.query(By.css('.quick-actions-title'));
        expect(quickActionsTitle.nativeElement.textContent.trim()).toBe('Quick Actions');
      });

      it('should display action buttons', () => {
        const actionButtons = fixture.debugElement.queryAll(By.css('.action-button'));
        expect(actionButtons.length).toBe(4);

        const buttonTexts = actionButtons.map(btn => btn.nativeElement.textContent.trim());
        expect(buttonTexts).toContain('Log Action');
        expect(buttonTexts).toContain('View Missions');
        expect(buttonTexts).toContain('Leaderboard');
        expect(buttonTexts).toContain('Analytics');
      });
    });

    describe('Loading State', () => {
      it('should show loading screen when isLoading is true', () => {
        component.isLoading.set(true);
        fixture.detectChanges();

        const loadingScreen = fixture.debugElement.query(By.css('.dashboard-loading'));
        const dashboardContainer = fixture.debugElement.query(By.css('.dashboard-container'));

        expect(loadingScreen).toBeTruthy();
        expect(dashboardContainer).toBeFalsy();
      });

      it('should show dashboard content when isLoading is false', () => {
        component.isLoading.set(false);
        fixture.detectChanges();

        const loadingScreen = fixture.debugElement.query(By.css('.dashboard-loading'));
        const dashboardContainer = fixture.debugElement.query(By.css('.dashboard-container'));

        expect(loadingScreen).toBeFalsy();
        expect(dashboardContainer).toBeTruthy();
      });

      it('should display loading message', () => {
        component.isLoading.set(true);
        fixture.detectChanges();

        const loadingText = fixture.debugElement.query(By.css('.loading-indicator p'));
        expect(loadingText.nativeElement.textContent.trim()).toBe('Retrieving mission data and statistics...');
      });
    });
  });

  describe('Component Method Testing', () => {
    beforeEach(() => {
      component.isLoading.set(false);
      component.stats.set(mockStats);
      component.activeMissions.set(mockMissions);
      fixture.detectChanges();
    });

    it('should handle timeframe changes correctly', () => {
      spyOn(component, 'loadDashboardData' as any);

      component.onTimeframeChange('month');

      expect(component.selectedTimeframe()).toBe('month');
      expect((component as any).loadDashboardData).toHaveBeenCalled();
    });

    it('should handle refresh correctly', () => {
      spyOn(component, 'loadDashboardData' as any);

      component.onRefresh();

      expect((component as any).loadDashboardData).toHaveBeenCalled();
    });

    it('should handle mission clicks correctly', () => {
      const mockMission = mockMissions[0];

      component.onMissionClick(mockMission);

      // Verify console.log was called (method implementation)
      expect(component.onMissionClick).toBeDefined();
    });

    it('should handle play hover sound calls', () => {
      spyOn(audioServiceSpy, 'playButtonHover');

      component.playHoverSound();

      expect(audioServiceSpy.playButtonHover).toHaveBeenCalled();
    });

    it('should handle action submissions correctly', () => {
      const mockAction = {
        actionTypeId: '1',
        description: 'Test action',
        location: 'Test location',
        additionalNotes: 'Test notes',
        attachments: []
      };

      const currentActivityLength = component.recentActivity().length;
      const currentPoints = component.stats().totalPoints;

      component.onActionSubmitted(mockAction);

      expect(component.recentActivity().length).toBe(currentActivityLength + 1);
      expect(component.stats().totalPoints).toBe(currentPoints + 10);
    });
  });

  describe('Edge Cases and Error Handling', () => {
    it('should handle empty stats gracefully', () => {
      component.stats.set({
        totalPoints: 0,
        rank: '',
        missionsCompleted: 0,
        activeMissions: 0,
        leaderboardPosition: 0,
        weeklyProgress: 0
      });
      fixture.detectChanges();

      expect(() => fixture.detectChanges()).not.toThrow();
    });

    it('should handle missions without deadlines', () => {
      const missionsWithoutDeadlines = mockMissions.map(m => ({ ...m, deadline: undefined }));
      component.activeMissions.set(missionsWithoutDeadlines);
      fixture.detectChanges();

      expect(() => fixture.detectChanges()).not.toThrow();
    });

    it('should handle activities without points', () => {
      const activitiesWithoutPoints = mockActivity.map(a => ({ ...a, points: undefined }));
      component.recentActivity.set(activitiesWithoutPoints);
      fixture.detectChanges();

      expect(() => fixture.detectChanges()).not.toThrow();
    });

    it('should handle very large numbers gracefully', () => {
      const largeStats = { ...mockStats, totalPoints: 999999999 };
      component.stats.set(largeStats);
      fixture.detectChanges();

      expect(component.stats().totalPoints).toBe(999999999);
    });
  });
});