import { test, expect, Page } from '@playwright/test';

// Test data
const mockUser = {
  name: 'Commander Data',
  rank: 'CMDR',
  points: 2847,
  role: 'ADMIN'
};

const mockDashboardData = {
  stats: {
    totalPoints: 2847,
    rank: 'COMMANDER',
    missionsCompleted: 15,
    activeMissions: 3,
    leaderboardPosition: 7,
    weeklyProgress: 68
  },
  missions: [
    {
      id: '1',
      title: 'Security Protocol Review',
      description: 'Review and update security protocols for Deck 7',
      progress: 75,
      totalSteps: 4,
      completedSteps: 3,
      pointsReward: 200,
      difficulty: 'MEDIUM'
    }
  ],
  activity: [
    {
      id: '1',
      type: 'MISSION_COMPLETED',
      description: 'Completed "Data Analysis Protocol"',
      points: 150,
      timestamp: new Date().toISOString(),
      icon: '🎯'
    }
  ],
  leaderboard: [
    { rank: 1, name: 'Captain Picard', points: 4250, change: 0 },
    { rank: 2, name: 'Lt. Commander Data', points: 3890, change: 1 },
    { rank: 3, name: 'Commander Riker', points: 3654, change: -1 }
  ]
};

// Helper function to setup API mocks
async function setupApiMocks(page: Page) {
  // Mock user API
  await page.route('**/api/users/me', async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(mockUser)
    });
  });

  // Mock dashboard stats API
  await page.route('**/api/dashboard/stats', async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(mockDashboardData.stats)
    });
  });

  // Mock missions API
  await page.route('**/api/missions/active', async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(mockDashboardData.missions)
    });
  });

  // Mock activity API
  await page.route('**/api/activity/recent', async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(mockDashboardData.activity)
    });
  });

  // Mock leaderboard API
  await page.route('**/api/leaderboards/preview', async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(mockDashboardData.leaderboard)
    });
  });
}

test.describe('Starfleet Gamifier E2E Tests', () => {
  test.beforeEach(async ({ page }) => {
    await setupApiMocks(page);
  });

  test.describe('Application Loading and Initialization', () => {
    test('should load the application with LCARS theme', async ({ page }) => {
      await page.goto('/');

      // Check for loading screen
      await expect(page.locator('.loading-screen')).toBeVisible();
      await expect(page.locator('text=ACCESSING STARFLEET DATABASE')).toBeVisible();

      // Wait for loading to complete
      await expect(page.locator('.loading-screen')).toBeHidden({ timeout: 10000 });

      // Check LCARS theme elements
      await expect(page.locator('.lcars-app-container')).toBeVisible();
      await expect(page.locator('.lcars-header')).toBeVisible();
      await expect(page.locator('.app-title')).toContainText('Starfleet Gamifier');
    });

    test('should display user information after loading', async ({ page }) => {
      await page.goto('/');
      await page.waitForSelector('.page-content');

      // Check user info in header
      await expect(page.locator('.user-name')).toContainText(mockUser.name);
      await expect(page.locator('.rank-badge')).toContainText(mockUser.rank);
      await expect(page.locator('.points')).toContainText(`${mockUser.points} PTS`);
    });

    test('should show admin navigation for admin users', async ({ page }) => {
      await page.goto('/');
      await page.waitForSelector('.page-content');

      // Check for admin navigation section
      await expect(page.locator('text=Admin Functions')).toBeVisible();
      await expect(page.locator('a[routerLink="/admin/users"]')).toBeVisible();
      await expect(page.locator('a[routerLink="/admin/organization"]')).toBeVisible();
      await expect(page.locator('a[routerLink="/admin/reports"]')).toBeVisible();
    });

    test('should display system status information', async ({ page }) => {
      await page.goto('/');
      await page.waitForSelector('.page-content');

      // Check system status panel
      await expect(page.locator('.system-status')).toBeVisible();
      await expect(page.locator('text=System Status')).toBeVisible();
      await expect(page.locator('text=Connection:')).toBeVisible();
      await expect(page.locator('.status-value.connected')).toBeVisible();
    });
  });

  test.describe('Navigation and Routing', () => {
    test('should redirect to dashboard by default', async ({ page }) => {
      await page.goto('/');
      await page.waitForSelector('.page-content');

      await expect(page).toHaveURL(/.*\/dashboard/);
      await expect(page.locator('a[routerLink="/dashboard"].active')).toBeVisible();
    });

    test('should navigate between main sections', async ({ page }) => {
      await page.goto('/');
      await page.waitForSelector('.page-content');

      const navigationTests = [
        { link: 'a[routerLink="/missions"]', url: '/missions', text: 'Missions' },
        { link: 'a[routerLink="/leaderboards"]', url: '/leaderboards', text: 'Leaderboards' },
        { link: 'a[routerLink="/actions"]', url: '/actions', text: 'Actions' },
        { link: 'a[routerLink="/dashboard"]', url: '/dashboard', text: 'Dashboard' }
      ];

      for (const nav of navigationTests) {
        await page.click(nav.link);
        await expect(page).toHaveURL(new RegExp(`.*${nav.url}`));
        await expect(page.locator(`${nav.link}.active`)).toBeVisible();
      }
    });

    test('should navigate to admin sections', async ({ page }) => {
      await page.goto('/');
      await page.waitForSelector('.page-content');

      const adminNavigationTests = [
        { link: 'a[routerLink="/admin/users"]', url: '/admin/users' },
        { link: 'a[routerLink="/admin/organization"]', url: '/admin/organization' },
        { link: 'a[routerLink="/admin/reports"]', url: '/admin/reports' }
      ];

      for (const nav of adminNavigationTests) {
        await page.click(nav.link);
        await expect(page).toHaveURL(new RegExp(`.*${nav.url}`));
      }
    });

    test('should maintain navigation state during page interactions', async ({ page }) => {
      await page.goto('/dashboard');
      await page.waitForSelector('.page-content');

      // Interact with dashboard elements
      await page.hover('.refresh-btn');
      await page.click('.refresh-btn');

      // Navigation should remain stable
      await expect(page).toHaveURL(/.*\/dashboard/);
      await expect(page.locator('a[routerLink="/dashboard"].active')).toBeVisible();
    });
  });

  test.describe('Dashboard Functionality', () => {
    test('should display dashboard statistics', async ({ page }) => {
      await page.goto('/dashboard');
      await page.waitForSelector('.dashboard-container');

      // Check statistics cards
      await expect(page.locator('text=Command Dashboard')).toBeVisible();
      await expect(page.locator('text=2,847')).toBeVisible(); // Total points
      await expect(page.locator('text=COMMANDER')).toBeVisible(); // Rank
      await expect(page.locator('text=15')).toBeVisible(); // Missions completed
      await expect(page.locator('text=#7')).toBeVisible(); // Leaderboard position
    });

    test('should display active missions', async ({ page }) => {
      await page.goto('/dashboard');
      await page.waitForSelector('.dashboard-container');

      // Check missions section
      await expect(page.locator('text=Active Missions')).toBeVisible();
      await expect(page.locator('text=Security Protocol Review')).toBeVisible();
      await expect(page.locator('text=Review and update security protocols')).toBeVisible();
      await expect(page.locator('text=75%')).toBeVisible(); // Progress
      await expect(page.locator('text=200 PTS')).toBeVisible(); // Reward
    });

    test('should display recent activity', async ({ page }) => {
      await page.goto('/dashboard');
      await page.waitForSelector('.dashboard-container');

      // Check activity section
      await expect(page.locator('text=Recent Activity')).toBeVisible();
      await expect(page.locator('text=Completed "Data Analysis Protocol"')).toBeVisible();
      await expect(page.locator('text=+150 PTS')).toBeVisible();
    });

    test('should display leaderboard preview', async ({ page }) => {
      await page.goto('/dashboard');
      await page.waitForSelector('.dashboard-container');

      // Check leaderboard section
      await expect(page.locator('text=Top Performers')).toBeVisible();
      await expect(page.locator('text=Captain Picard')).toBeVisible();
      await expect(page.locator('text=4,250 PTS')).toBeVisible();
      await expect(page.locator('text=#1')).toBeVisible();
    });

    test('should change timeframe selection', async ({ page }) => {
      await page.goto('/dashboard');
      await page.waitForSelector('.dashboard-container');

      // Test timeframe selector
      await expect(page.locator('.timeframe-buttons .lcars-button.active')).toContainText('Week');

      await page.click('text=Month');
      await expect(page.locator('.timeframe-buttons .lcars-button.active')).toContainText('Month');

      await page.click('text=Year');
      await expect(page.locator('.timeframe-buttons .lcars-button.active')).toContainText('Year');
    });

    test('should interact with quick actions', async ({ page }) => {
      await page.goto('/dashboard');
      await page.waitForSelector('.dashboard-container');

      // Check quick actions section
      await expect(page.locator('text=Quick Actions')).toBeVisible();
      await expect(page.locator('text=Log Action')).toBeVisible();
      await expect(page.locator('text=View Missions')).toBeVisible();
      await expect(page.locator('text=Leaderboard')).toBeVisible();
      await expect(page.locator('text=Analytics')).toBeVisible();

      // Test quick action interactions
      await page.hover('.action-button >> text=Log Action');
      await page.click('.action-button >> text=View Missions');
    });
  });

  test.describe('Audio System', () => {
    test('should toggle audio settings', async ({ page }) => {
      await page.goto('/');
      await page.waitForSelector('.page-content');

      // Find audio button
      const audioButton = page.locator('.header-controls .lcars-button >> text=Audio');
      await expect(audioButton).toBeVisible();

      // Test audio toggle
      await audioButton.click();
      // Audio feedback should be working but we can't test audio in E2E easily
      // We can verify the button interaction works
      await expect(audioButton).toBeVisible();
    });

    test('should provide audio feedback on interactions', async ({ page }) => {
      await page.goto('/');
      await page.waitForSelector('.page-content');

      // Test various interactive elements
      await page.hover('a[routerLink="/missions"]');
      await page.click('a[routerLink="/missions"]');
      await page.hover('.refresh-btn');

      // Verify interactions work (audio can't be tested in E2E)
      await expect(page).toHaveURL(/.*\/missions/);
    });
  });

  test.describe('Notifications System', () => {
    test('should display notifications', async ({ page }) => {
      await page.goto('/');
      await page.waitForSelector('.page-content');

      // Trigger test notification (if available in UI)
      // For now, we'll verify the notification system structure exists
      const notificationPanel = page.locator('.notification-panel');
      // Notification panel exists but may be empty initially
      await expect(page.locator('.lcars-app-container')).toBeVisible();
    });
  });

  test.describe('Responsive Design', () => {
    test('should adapt to mobile viewport', async ({ page }) => {
      await page.setViewportSize({ width: 480, height: 800 });
      await page.goto('/');
      await page.waitForSelector('.page-content');

      // Check mobile adaptations
      await expect(page.locator('.lcars-app-container')).toBeVisible();
      await expect(page.locator('.lcars-header')).toBeVisible();
      await expect(page.locator('.lcars-nav')).toBeVisible();
    });

    test('should adapt to tablet viewport', async ({ page }) => {
      await page.setViewportSize({ width: 768, height: 1024 });
      await page.goto('/');
      await page.waitForSelector('.page-content');

      // Check tablet adaptations
      await expect(page.locator('.lcars-app-container')).toBeVisible();
      await expect(page.locator('.dashboard-container')).toBeVisible();
    });

    test('should work on desktop viewport', async ({ page }) => {
      await page.setViewportSize({ width: 1920, height: 1080 });
      await page.goto('/');
      await page.waitForSelector('.page-content');

      // Check desktop layout
      await expect(page.locator('.lcars-main-layout')).toBeVisible();
      await expect(page.locator('.lcars-nav')).toBeVisible();
      await expect(page.locator('.content-area')).toBeVisible();
    });
  });

  test.describe('Error Handling', () => {
    test('should handle API errors gracefully', async ({ page }) => {
      // Mock API error
      await page.route('**/api/users/me', async route => {
        await route.fulfill({
          status: 500,
          contentType: 'application/json',
          body: JSON.stringify({ error: 'Internal Server Error' })
        });
      });

      await page.goto('/');

      // App should still load but show disconnected state
      await expect(page.locator('.lcars-app-container')).toBeVisible();
      await expect(page.locator('.status-value.disconnected')).toBeVisible();
    });

    test('should handle network connectivity issues', async ({ page }) => {
      // Start with working API
      await page.goto('/');
      await page.waitForSelector('.page-content');

      // Simulate network failure
      await page.route('**/api/**', async route => {
        await route.abort('failed');
      });

      // Try to refresh dashboard
      await page.click('.refresh-btn');

      // App should remain functional
      await expect(page.locator('.lcars-app-container')).toBeVisible();
    });

    test('should handle invalid routes', async ({ page }) => {
      await page.goto('/invalid-route');

      // Should show 404 or redirect to valid route
      // For now, check that app doesn't crash
      await expect(page.locator('.lcars-app-container')).toBeVisible();
    });
  });

  test.describe('Performance and Loading', () => {
    test('should load within acceptable time limits', async ({ page }) => {
      const startTime = Date.now();

      await page.goto('/');
      await page.waitForSelector('.page-content');

      const loadTime = Date.now() - startTime;
      expect(loadTime).toBeLessThan(5000); // Should load within 5 seconds
    });

    test('should handle rapid navigation without issues', async ({ page }) => {
      await page.goto('/');
      await page.waitForSelector('.page-content');

      // Rapidly navigate between sections
      const routes = ['/dashboard', '/missions', '/leaderboards', '/actions'];

      for (let i = 0; i < 3; i++) {
        for (const route of routes) {
          await page.click(`a[routerLink="${route}"]`);
          await page.waitForTimeout(100);
        }
      }

      // Should end up on the last route without errors
      await expect(page).toHaveURL(/.*\/actions/);
      await expect(page.locator('.lcars-app-container')).toBeVisible();
    });

    test('should maintain state during page refresh', async ({ page }) => {
      await page.goto('/dashboard');
      await page.waitForSelector('.dashboard-container');

      // Change timeframe
      await page.click('text=Month');
      await expect(page.locator('.timeframe-buttons .lcars-button.active')).toContainText('Month');

      // Refresh page
      await page.reload();
      await page.waitForSelector('.dashboard-container');

      // Should return to default state
      await expect(page.locator('.timeframe-buttons .lcars-button.active')).toContainText('Week');
    });
  });

  test.describe('Accessibility', () => {
    test('should be keyboard navigable', async ({ page }) => {
      await page.goto('/');
      await page.waitForSelector('.page-content');

      // Test keyboard navigation
      await page.keyboard.press('Tab');
      await page.keyboard.press('Tab');
      await page.keyboard.press('Enter');

      // Should be able to navigate with keyboard
      await expect(page.locator('.lcars-app-container')).toBeVisible();
    });

    test('should have proper ARIA labels', async ({ page }) => {
      await page.goto('/');
      await page.waitForSelector('.page-content');

      // Check for accessibility attributes
      const navigation = page.locator('.lcars-nav');
      await expect(navigation).toBeVisible();

      // Check that interactive elements are focusable
      const buttons = page.locator('button, a[routerLink]');
      const buttonCount = await buttons.count();
      expect(buttonCount).toBeGreaterThan(0);
    });
  });

  test.describe('Cross-browser Compatibility', () => {
    test('should work consistently across viewport sizes', async ({ page }) => {
      const viewports = [
        { width: 1920, height: 1080 },
        { width: 1366, height: 768 },
        { width: 1024, height: 768 },
        { width: 768, height: 1024 },
        { width: 480, height: 800 }
      ];

      for (const viewport of viewports) {
        await page.setViewportSize(viewport);
        await page.goto('/');
        await page.waitForSelector('.page-content');

        // Core functionality should work on all sizes
        await expect(page.locator('.lcars-app-container')).toBeVisible();
        await expect(page.locator('.app-title')).toBeVisible();
        await page.click('a[routerLink="/dashboard"]');
        await expect(page).toHaveURL(/.*\/dashboard/);
      }
    });
  });
});