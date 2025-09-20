import { Component, OnInit, OnDestroy, ViewChild, ElementRef, signal, computed, inject } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { interval, Subscription } from 'rxjs';
import { AudioService } from './core/services/audio.service';
import { ApiService } from './core/services/api.service';
import { ThemeService } from './core/services/theme.service';
import { environment } from '../environments/environment';

export interface User {
  id: number;
  name: string;
  email: string;
  rank: string;
  points: number;
  role: string;
}

export interface Notification {
  id: string;
  title: string;
  message: string;
  type: 'success' | 'error' | 'warning' | 'info';
  timestamp: Date;
}

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule],
  templateUrl: './app-new.html',
  styleUrls: ['./proxima-theme.scss']
})
export class App implements OnInit, OnDestroy {
  @ViewChild('audioElement') audioElement!: ElementRef<HTMLAudioElement>;

  private audioService = inject(AudioService);
  private apiService = inject(ApiService);
  private themeService = inject(ThemeService);
  private timeSubscription?: Subscription;

  // App Properties
  appTitle = computed(() => this.themeService.getAppName());
  version = environment.version;

  // Theme Properties
  currentTheme = this.themeService.currentTheme;
  isStarfleetTheme = computed(() => this.themeService.isStarfleetTheme());
  isCorporateTheme = computed(() => this.themeService.isCorporateTheme());

  // User State
  currentUser = signal<User | null>(null);
  isAdmin = computed(() => this.currentUser()?.role === 'ADMIN');

  // UI State
  isLoading = signal(true);
  audioEnabledLocal = signal(environment.audio.enabled);
  audioEnabled = computed(() => this.themeService.isFeatureEnabled('sounds') && this.audioEnabledLocal());
  connectionStatus = signal<'connected' | 'disconnected' | 'connecting'>('connecting');

  // System Status
  onlineUsers = signal(0);
  systemLoad = signal(42); // Default system load percentage
  currentTime = signal(new Date());
  stardate = computed(() => this.calculateStardate());

  // Loading Animation
  loadingBars = Array.from({ length: 8 }, (_, i) => i);
  loadingMessage = signal('Establishing secure connection...');

  // Notifications
  notifications = signal<Notification[]>([]);

  ngOnInit(): void {
    this.initializeApp();
    this.startTimeUpdates();
    this.setupLoadingSequence();
  }

  ngOnDestroy(): void {
    this.timeSubscription?.unsubscribe();
  }

  private async initializeApp(): Promise<void> {
    try {
      // Initialize audio on first user interaction
      await this.audioService.resumeAudioContext();

      // Load user data
      await this.loadCurrentUser();

      // Initialize system status
      this.updateSystemStatus();

      // Complete loading after 2 seconds
      setTimeout(() => {
        this.isLoading.set(false);
        this.audioService.playSystemStartup();
      }, 2000);

    } catch (error) {
      console.error('Failed to initialize app:', error);
      this.isLoading.set(false);
      this.connectionStatus.set('disconnected');
    }
  }

  private setupLoadingSequence(): void {
    const messages = [
      'Establishing secure connection...',
      'Authenticating user credentials...',
      'Loading mission parameters...',
      'Initializing LCARS interface...',
      'Synchronizing with starfleet database...',
      'Access granted. Welcome aboard.'
    ];

    messages.forEach((message, index) => {
      setTimeout(() => {
        this.loadingMessage.set(message);
      }, index * 300);
    });
  }

  private async loadCurrentUser(): Promise<void> {
    if (!environment.mockData.enabled) {
      try {
        const response = await fetch(`${environment.apiUrl}/users/current`);

        if (response.ok) {
          const user = await response.json();
          this.currentUser.set(user);
          this.connectionStatus.set('connected');
          return;
        } else if (response.status === 404 || response.status === 500) {
          // No user found or server error - try to create a default user
          console.log('No current user found, creating default user...');
          await this.createDefaultUser();
          return;
        } else {
          throw new Error(`Failed to load user: ${response.status}`);
        }
      } catch (error) {
        console.error('Failed to load user:', error);
        this.connectionStatus.set('disconnected');
        if (!environment.mockData.fallbackOnError) {
          this.currentUser.set(null);
          return;
        }
      }
    }

    // Mock data - only used if mock data is enabled or as fallback on error
    if (environment.mockData.enabled || environment.mockData.fallbackOnError) {
      const mockUser: User = {
        id: 1,
        name: 'Development User',
        email: 'dev@example.com',
        rank: 'DEV',
        points: 0,
        role: 'ADMIN'
      };
      this.currentUser.set(mockUser);
      this.connectionStatus.set('connected');
    }
  }

  private async createDefaultUser(): Promise<void> {
    try {
      // Create a default organization first
      const orgResponse = await fetch(`${environment.apiUrl}/organizations`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          id: 'default-org',
          name: 'Default Organization',
          description: 'Default organization for testing'
        })
      });

      // Create default user
      const userResponse = await fetch(`${environment.apiUrl}/users`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          id: 'dev-user-001',
          name: 'Development User',
          email: 'dev@example.com',
          role: 'ADMIN',
          organizationId: 'default-org'
        })
      });

      if (userResponse.ok) {
        const user = await userResponse.json();
        this.currentUser.set(user);
        this.connectionStatus.set('connected');
        console.log('Default user created successfully');
      } else {
        throw new Error('Failed to create default user');
      }
    } catch (error) {
      console.error('Failed to create default user:', error);
      // Fall back to mock user
      const mockUser: User = {
        id: 1,
        name: 'Development User',
        email: 'dev@example.com',
        rank: 'DEV',
        points: 0,
        role: 'ADMIN'
      };
      this.currentUser.set(mockUser);
      this.connectionStatus.set('connected');
    }
  }

  private updateSystemStatus(): void {
    // Update online users count (mock data)
    this.onlineUsers.set(Math.floor(Math.random() * 50) + 10);

    // Update system load (mock fluctuation)
    setInterval(() => {
      const currentLoad = this.systemLoad();
      const variation = (Math.random() - 0.5) * 10;
      const newLoad = Math.max(10, Math.min(90, currentLoad + variation));
      this.systemLoad.set(Math.round(newLoad));
    }, 5000);
  }

  private startTimeUpdates(): void {
    this.timeSubscription = interval(1000).subscribe(() => {
      this.currentTime.set(new Date());
    });
  }

  private calculateStardate(): string {
    const now = this.currentTime();
    const year = now.getFullYear();
    const dayOfYear = Math.floor((now.getTime() - new Date(year, 0, 0).getTime()) / (1000 * 60 * 60 * 24));
    const stardate = (year - 2000) * 1000 + (dayOfYear / 365.25) * 1000;
    return stardate.toFixed(1);
  }

  // Audio Event Handlers
  onUserInteraction(): void {
    // Initialize audio context on first user interaction
    this.audioService.resumeAudioContext();
  }

  toggleAudio(): void {
    const newState = !this.audioEnabledLocal();
    this.audioEnabledLocal.set(newState);
    this.audioService.setEnabled(newState && this.themeService.isFeatureEnabled('sounds'));

    if (newState && this.themeService.isFeatureEnabled('sounds')) {
      this.audioService.playSuccess();
    }
  }

  playHoverSound(): void {
    this.audioService.playButtonHover();
  }

  playClickSound(): void {
    this.audioService.playButtonClick();
  }

  // Theme Management
  switchTheme(): void {
    const currentTheme = this.themeService.getCurrentThemeName();
    const newTheme = currentTheme === 'starfleet' ? 'corporate' : 'starfleet';
    this.themeService.setTheme(newTheme);

    if (this.audioEnabled()) {
      this.audioService.playButtonClick();
    }
  }

  getAvailableThemes() {
    return this.themeService.getAvailableThemes();
  }

  // Notification Management
  addNotification(notification: Omit<Notification, 'id' | 'timestamp'>): void {
    const newNotification: Notification = {
      ...notification,
      id: crypto.randomUUID(),
      timestamp: new Date()
    };

    this.notifications.update(notifications => [...notifications, newNotification]);
    this.audioService.playNotification();

    // Auto-dismiss after 5 seconds for non-error notifications
    if (notification.type !== 'error') {
      setTimeout(() => {
        this.dismissNotification(newNotification.id);
      }, 5000);
    }
  }

  dismissNotification(id: string): void {
    this.notifications.update(notifications =>
      notifications.filter(n => n.id !== id)
    );
    this.audioService.playButtonClick();
  }

  trackByNotificationId(index: number, notification: Notification): string {
    return notification.id;
  }

  // Development helpers
  showTestNotification(): void {
    this.addNotification({
      title: 'System Test',
      message: 'LCARS interface is functioning normally.',
      type: 'success'
    });
  }
}
