import { Injectable, signal, effect } from '@angular/core';
import { environment } from '../../../environments/environment';

export type ThemeName = 'starfleet' | 'corporate';

export interface ThemeConfig {
  name: ThemeName;
  enableSounds: boolean;
  enableAnimations: boolean;
  enableEffects: boolean;
  appName: string;
}

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private readonly THEME_STORAGE_KEY = 'gamifier-theme';

  // Default theme from environment
  private readonly defaultTheme: ThemeConfig = {
    name: environment.theme.name as ThemeName,
    enableSounds: environment.theme.enableSounds,
    enableAnimations: environment.theme.enableAnimations,
    enableEffects: environment.theme.enableEffects,
    appName: environment.appName
  };

  // Available themes configuration
  private readonly themes: Record<ThemeName, ThemeConfig> = {
    starfleet: {
      name: 'starfleet',
      enableSounds: true,
      enableAnimations: true,
      enableEffects: true,
      appName: 'Starfleet Gamifier'
    },
    corporate: {
      name: 'corporate',
      enableSounds: false,
      enableAnimations: true,
      enableEffects: false,
      appName: 'Corporate Gamifier'
    }
  };

  // Current theme signal
  public currentTheme = signal<ThemeConfig>(this.defaultTheme);

  constructor() {
    // Load saved theme from localStorage or use default
    this.loadSavedTheme();

    // Apply theme changes to DOM
    effect(() => {
      this.applyThemeToDOM(this.currentTheme());
    });
  }

  /**
   * Switch to a different theme
   */
  setTheme(themeName: ThemeName): void {
    const theme = this.themes[themeName];
    if (theme) {
      this.currentTheme.set(theme);
      this.saveTheme(theme);
    }
  }

  /**
   * Get available theme names
   */
  getAvailableThemes(): ThemeName[] {
    return Object.keys(this.themes) as ThemeName[];
  }

  /**
   * Check if current theme has specific feature enabled
   */
  isFeatureEnabled(feature: 'sounds' | 'animations' | 'effects'): boolean {
    const theme = this.currentTheme();
    switch (feature) {
      case 'sounds':
        return theme.enableSounds;
      case 'animations':
        return theme.enableAnimations;
      case 'effects':
        return theme.enableEffects;
      default:
        return false;
    }
  }

  /**
   * Get current theme name
   */
  getCurrentThemeName(): ThemeName {
    return this.currentTheme().name;
  }

  /**
   * Get current app name based on theme
   */
  getAppName(): string {
    return this.currentTheme().appName;
  }

  /**
   * Check if current theme is corporate
   */
  isCorporateTheme(): boolean {
    return this.currentTheme().name === 'corporate';
  }

  /**
   * Check if current theme is starfleet
   */
  isStarfleetTheme(): boolean {
    return this.currentTheme().name === 'starfleet';
  }

  private loadSavedTheme(): void {
    try {
      const saved = localStorage.getItem(this.THEME_STORAGE_KEY);
      if (saved) {
        const themeName = JSON.parse(saved) as ThemeName;
        const theme = this.themes[themeName];
        if (theme) {
          this.currentTheme.set(theme);
          return;
        }
      }
    } catch (error) {
      console.warn('Failed to load saved theme, using default:', error);
    }

    // Use default theme if no saved theme or loading failed
    this.currentTheme.set(this.defaultTheme);
  }

  private saveTheme(theme: ThemeConfig): void {
    try {
      localStorage.setItem(this.THEME_STORAGE_KEY, JSON.stringify(theme.name));
    } catch (error) {
      console.warn('Failed to save theme preference:', error);
    }
  }

  private applyThemeToDOM(theme: ThemeConfig): void {
    const body = document.body;

    // Remove all theme classes
    body.classList.remove('theme-starfleet', 'theme-corporate');

    // Add current theme class
    body.classList.add(`theme-${theme.name}`);

    // Update document title
    document.title = theme.appName;

    // Set CSS custom properties for theme
    const root = document.documentElement;
    if (theme.name === 'corporate') {
      root.style.setProperty('--theme-primary', '#2563eb');
      root.style.setProperty('--theme-secondary', '#64748b');
      root.style.setProperty('--theme-background', '#ffffff');
      root.style.setProperty('--theme-surface', '#f8fafc');
      root.style.setProperty('--theme-text-primary', '#0f172a');
      root.style.setProperty('--theme-text-secondary', '#475569');
      root.style.setProperty('--theme-border', '#e2e8f0');
      root.style.setProperty('--theme-border-radius', '0.375rem');
      root.style.setProperty('--theme-shadow', '0 1px 3px 0 rgb(0 0 0 / 0.1)');
    } else {
      // Starfleet theme (LCARS colors)
      root.style.setProperty('--theme-primary', '#FF9900');
      root.style.setProperty('--theme-secondary', '#CC6666');
      root.style.setProperty('--theme-background', '#000000');
      root.style.setProperty('--theme-surface', '#111111');
      root.style.setProperty('--theme-text-primary', '#CCCCCC');
      root.style.setProperty('--theme-text-secondary', '#999999');
      root.style.setProperty('--theme-border', '#333333');
      root.style.setProperty('--theme-border-radius', '0');
      root.style.setProperty('--theme-shadow', '0 0 10px rgba(255, 153, 0, 0.3)');
    }
  }
}