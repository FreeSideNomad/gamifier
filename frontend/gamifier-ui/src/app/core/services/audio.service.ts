import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class AudioService {
  private audioContext?: AudioContext;
  private enabled = true;
  private volume = 0.3;

  constructor() {
    // Initialize audio context only when needed (user interaction)
  }

  /**
   * Initialize audio context (must be called after user interaction)
   */
  private initializeAudioContext(): void {
    if (!this.audioContext) {
      this.audioContext = new (window.AudioContext || (window as any).webkitAudioContext)();
    }
  }

  /**
   * Generate LCARS-style beep sound
   */
  playBeep(frequency: number = 440, duration: number = 150): void {
    if (!this.enabled) return;

    try {
      this.initializeAudioContext();
      if (!this.audioContext) return;

      const oscillator = this.audioContext.createOscillator();
      const gainNode = this.audioContext.createGain();

      oscillator.connect(gainNode);
      gainNode.connect(this.audioContext.destination);

      oscillator.frequency.setValueAtTime(frequency, this.audioContext.currentTime);
      oscillator.type = 'sine';

      gainNode.gain.setValueAtTime(0, this.audioContext.currentTime);
      gainNode.gain.linearRampToValueAtTime(this.volume, this.audioContext.currentTime + 0.01);
      gainNode.gain.exponentialRampToValueAtTime(0.01, this.audioContext.currentTime + duration / 1000);

      oscillator.start(this.audioContext.currentTime);
      oscillator.stop(this.audioContext.currentTime + duration / 1000);
    } catch (error) {
      console.warn('Audio playback failed:', error);
    }
  }

  /**
   * LCARS button click sound (high-pitched beep)
   */
  playButtonClick(): void {
    this.playBeep(800, 100);
  }

  /**
   * LCARS hover sound (subtle tone)
   */
  playButtonHover(): void {
    this.playBeep(600, 80);
  }

  /**
   * LCARS success sound (ascending tones)
   */
  playSuccess(): void {
    setTimeout(() => this.playBeep(440, 150), 0);
    setTimeout(() => this.playBeep(554, 150), 100);
    setTimeout(() => this.playBeep(659, 200), 200);
  }

  /**
   * LCARS error sound (descending tones)
   */
  playError(): void {
    setTimeout(() => this.playBeep(659, 150), 0);
    setTimeout(() => this.playBeep(554, 150), 100);
    setTimeout(() => this.playBeep(440, 200), 200);
  }

  /**
   * LCARS notification sound (two-tone)
   */
  playNotification(): void {
    setTimeout(() => this.playBeep(523, 120), 0);
    setTimeout(() => this.playBeep(698, 120), 150);
  }

  /**
   * LCARS alert sound (urgent rapid beeps)
   */
  playAlert(): void {
    for (let i = 0; i < 3; i++) {
      setTimeout(() => this.playBeep(880, 100), i * 150);
    }
  }

  /**
   * LCARS system startup sound (ascending sequence)
   */
  playSystemStartup(): void {
    const frequencies = [220, 293, 369, 440, 554, 659, 784, 880];
    frequencies.forEach((freq, index) => {
      setTimeout(() => this.playBeep(freq, 200), index * 100);
    });
  }

  /**
   * Enable or disable audio
   */
  setEnabled(enabled: boolean): void {
    this.enabled = enabled;
  }

  /**
   * Set volume (0.0 to 1.0)
   */
  setVolume(volume: number): void {
    this.volume = Math.max(0, Math.min(1, volume));
  }

  /**
   * Get current enabled status
   */
  isEnabled(): boolean {
    return this.enabled;
  }

  /**
   * Get current volume
   */
  getVolume(): number {
    return this.volume;
  }

  /**
   * Resume audio context if suspended (required for some browsers)
   */
  async resumeAudioContext(): Promise<void> {
    if (this.audioContext && this.audioContext.state === 'suspended') {
      await this.audioContext.resume();
    }
  }
}