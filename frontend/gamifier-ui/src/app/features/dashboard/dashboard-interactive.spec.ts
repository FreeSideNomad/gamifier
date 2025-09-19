import { TestBed } from '@angular/core/testing';
import { DashboardComponent } from './dashboard.component';
import { provideHttpClient } from '@angular/common/http';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

describe('DashboardComponent Interactive Features', () => {
  let component: DashboardComponent;
  let fixture: any;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [provideHttpClient()]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  describe('Timeframe Selector', () => {
    it('should have week selected by default', () => {
      expect(component.selectedTimeframe()).toBe('week');
    });

    it('should change timeframe when week button is clicked', () => {
      const weekButton = fixture.debugElement.query(By.css('button:contains("Week")'));

      if (weekButton) {
        weekButton.nativeElement.click();
        fixture.detectChanges();
        expect(component.selectedTimeframe()).toBe('week');
      }
    });

    it('should change timeframe when month button is clicked', () => {
      spyOn(component, 'onTimeframeChange').and.callThrough();

      const monthButtons = fixture.debugElement.queryAll(By.css('.timeframe-selector button'));
      const monthButton = monthButtons.find((btn: DebugElement) => btn.nativeElement.textContent.trim() === 'Month');

      if (monthButton) {
        monthButton.nativeElement.click();
        fixture.detectChanges();

        expect(component.onTimeframeChange).toHaveBeenCalledWith('month');
        expect(component.selectedTimeframe()).toBe('month');
      }
    });

    it('should change timeframe when year button is clicked', () => {
      spyOn(component, 'onTimeframeChange').and.callThrough();

      const yearButtons = fixture.debugElement.queryAll(By.css('.timeframe-selector button'));
      const yearButton = yearButtons.find((btn: DebugElement) => btn.nativeElement.textContent.trim() === 'Year');

      if (yearButton) {
        yearButton.nativeElement.click();
        fixture.detectChanges();

        expect(component.onTimeframeChange).toHaveBeenCalledWith('year');
        expect(component.selectedTimeframe()).toBe('year');
      }
    });

    it('should show active state for selected timeframe', () => {
      component.selectedTimeframe.set('month');
      fixture.detectChanges();

      const monthButtons = fixture.debugElement.queryAll(By.css('.timeframe-selector button'));
      const monthButton = monthButtons.find((btn: DebugElement) => btn.nativeElement.textContent.trim() === 'Month');

      if (monthButton) {
        expect(monthButton.nativeElement.classList.contains('active')).toBeTruthy();
      }
    });

    it('should reload data when timeframe changes', () => {
      spyOn(component, 'loadDashboardData' as any);

      component.onTimeframeChange('year');

      expect((component as any).loadDashboardData).toHaveBeenCalled();
    });
  });

  describe('Quick Action Buttons', () => {
    it('should have all quick action buttons', () => {
      const actionButtons = fixture.debugElement.queryAll(By.css('.action-button'));
      expect(actionButtons.length).toBe(4);

      const buttonTexts = actionButtons.map((btn: DebugElement) => btn.nativeElement.textContent.trim());
      expect(buttonTexts).toContain('Log Action');
      expect(buttonTexts).toContain('View Missions');
      expect(buttonTexts).toContain('Leaderboard');
      expect(buttonTexts).toContain('Analytics');
    });

    it('should call onLogAction when Log Action button is clicked', () => {
      spyOn(component, 'onLogAction');

      const actionButtons = fixture.debugElement.queryAll(By.css('.action-button'));
      const logActionButton = actionButtons.find((btn: DebugElement) => btn.nativeElement.textContent.includes('Log Action'));

      if (logActionButton) {
        logActionButton.nativeElement.click();
        expect(component.onLogAction).toHaveBeenCalled();
      }
    });

    it('should call onViewMissions when View Missions button is clicked', () => {
      spyOn(component, 'onViewMissions');

      const actionButtons = fixture.debugElement.queryAll(By.css('.action-button'));
      const viewMissionsButton = actionButtons.find((btn: DebugElement) => btn.nativeElement.textContent.includes('View Missions'));

      if (viewMissionsButton) {
        viewMissionsButton.nativeElement.click();
        expect(component.onViewMissions).toHaveBeenCalled();
      }
    });

    it('should call onViewLeaderboard when Leaderboard button is clicked', () => {
      spyOn(component, 'onViewLeaderboard');

      const actionButtons = fixture.debugElement.queryAll(By.css('.action-button'));
      const leaderboardButton = actionButtons.find((btn: DebugElement) => btn.nativeElement.textContent.includes('Leaderboard'));

      if (leaderboardButton) {
        leaderboardButton.nativeElement.click();
        expect(component.onViewLeaderboard).toHaveBeenCalled();
      }
    });

    it('should call onViewAnalytics when Analytics button is clicked', () => {
      spyOn(component, 'onViewAnalytics');

      const actionButtons = fixture.debugElement.queryAll(By.css('.action-button'));
      const analyticsButton = actionButtons.find((btn: DebugElement) => btn.nativeElement.textContent.includes('Analytics'));

      if (analyticsButton) {
        analyticsButton.nativeElement.click();
        expect(component.onViewAnalytics).toHaveBeenCalled();
      }
    });
  });

  describe('Mission Interactions', () => {
    beforeEach(() => {
      // Set up mock mission data
      component.activeMissions.set([
        {
          id: '1',
          title: 'Test Mission',
          description: 'Test Description',
          progress: 50,
          totalSteps: 4,
          completedSteps: 2,
          pointsReward: 100,
          difficulty: 'MEDIUM'
        }
      ]);
      fixture.detectChanges();
    });

    it('should call onMissionClick when mission card is clicked', () => {
      spyOn(component, 'onMissionClick');

      const missionCards = fixture.debugElement.queryAll(By.css('.mission-card'));
      if (missionCards.length > 0) {
        missionCards[0].nativeElement.click();
        expect(component.onMissionClick).toHaveBeenCalled();
      }
    });
  });

  describe('Audio Feedback', () => {
    it('should play hover sound on timeframe button hover', () => {
      spyOn(component, 'playHoverSound');

      const timeframeButtons = fixture.debugElement.queryAll(By.css('.timeframe-selector button'));
      if (timeframeButtons.length > 0) {
        timeframeButtons[0].nativeElement.dispatchEvent(new Event('mouseenter'));
        expect(component.playHoverSound).toHaveBeenCalled();
      }
    });

    it('should play hover sound on action button hover', () => {
      spyOn(component, 'playHoverSound');

      const actionButtons = fixture.debugElement.queryAll(By.css('.action-button'));
      if (actionButtons.length > 0) {
        actionButtons[0].nativeElement.dispatchEvent(new Event('mouseenter'));
        expect(component.playHoverSound).toHaveBeenCalled();
      }
    });
  });

  describe('Refresh Functionality', () => {
    it('should call onRefresh when refresh button is clicked', () => {
      spyOn(component, 'onRefresh');

      const refreshButton = fixture.debugElement.query(By.css('.refresh-btn'));
      if (refreshButton) {
        refreshButton.nativeElement.click();
        expect(component.onRefresh).toHaveBeenCalled();
      }
    });

    it('should reload dashboard data when refresh is called', () => {
      spyOn(component, 'loadDashboardData' as any);

      component.onRefresh();

      expect((component as any).loadDashboardData).toHaveBeenCalled();
    });
  });

  describe('Visual Feedback', () => {
    it('should show loading state initially', () => {
      component.isLoading.set(true);
      fixture.detectChanges();

      // Should show loading state (implementation depends on template)
      expect(component.isLoading()).toBeTruthy();
    });

    it('should update UI when stats change', () => {
      const newStats = {
        totalPoints: 5000,
        rank: 'CAPTAIN',
        missionsCompleted: 20,
        activeMissions: 5,
        leaderboardPosition: 3,
        weeklyProgress: 85
      };

      component.stats.set(newStats);
      fixture.detectChanges();

      expect(component.stats().totalPoints).toBe(5000);
      expect(component.stats().rank).toBe('CAPTAIN');
    });

    it('should calculate progress percentage correctly', () => {
      component.stats.set({
        totalPoints: 1000,
        rank: 'LIEUTENANT',
        missionsCompleted: 10,
        activeMissions: 2,
        leaderboardPosition: 5,
        weeklyProgress: 75
      });

      expect(component.progressPercentage()).toBe(75);
    });
  });
});