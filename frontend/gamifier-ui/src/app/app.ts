import { Component, OnInit, OnDestroy, ViewChild, ElementRef, signal, computed, inject } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { interval, Subscription } from 'rxjs';
import { AudioService } from './core/services/audio.service';
import { ApiService } from './core/services/api.service';
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
  templateUrl: './app.html',
  styleUrls: ['./app.scss', './styles-improvements.scss']
})
export class App implements OnInit, OnDestroy {
  @ViewChild('audioElement') audioElement!: ElementRef<HTMLAudioElement>;

  private audioService = inject(AudioService);
  private apiService = inject(ApiService);
  private timeSubscription?: Subscription;

  // App Properties
  appTitle = environment.appName;
  version = environment.version;

  // User State
  currentUser = signal<User | null>(null);
  isAdmin = computed(() => this.currentUser()?.role === 'ADMIN');

  // UI State
  isLoading = signal(true);
  audioEnabled = signal(environment.audio.enabled);
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
    try {
      // Mock user data for now - will be replaced with real API call
      const mockUser: User = {
        id: 1,
        name: 'Commander Data',
        email: 'data@starfleet.gov',
        rank: 'CMDR',
        points: 2847,
        role: 'ADMIN'
      };

      this.currentUser.set(mockUser);
      this.connectionStatus.set('connected');
    } catch (error) {
      console.error('Failed to load user:', error);
      this.connectionStatus.set('disconnected');
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
    const newState = !this.audioEnabled();
    this.audioEnabled.set(newState);
    this.audioService.setEnabled(newState);

    if (newState) {
      this.audioService.playSuccess();
    }
  }

  playHoverSound(): void {
    this.audioService.playButtonHover();
  }

  playClickSound(): void {
    this.audioService.playButtonClick();
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
